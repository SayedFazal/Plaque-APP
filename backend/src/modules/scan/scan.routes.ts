import { Router, type Request, type Response, type NextFunction } from 'express';
import multer from 'multer';
import { requireAuth, requireVerified, validateBody } from '../../middleware/index.js';
import { submitScanSchema, summaryQuerySchema, uploadImageSchema } from './scan.schemas.js';
import * as service from './scan.service.js';
import * as resultService from './scan-result.service.js';
import { AppError } from '../../utils/errors.js';
import { MockAIProvider } from './ai-provider.js';

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 10 * 1024 * 1024 }, // 10 MB hard limit
  fileFilter: (req, file, cb) => {
    if (file.mimetype.startsWith('image/')) {
      cb(null, true);
    } else {
      cb(new Error('Only image files are allowed'));
    }
  },
});

export const scanRouter = Router();

/** Wraps an async handler so a rejected promise reaches the error middleware instead of hanging. */
const handle =
  (fn: (req: Request, res: Response) => Promise<void>) =>
  (req: Request, res: Response, next: NextFunction) => {
    fn(req, res).catch(next);
  };

// Every route here is clinical data: requireAuth then requireVerified, exactly as the middleware doc
// prescribes. Auth itself is untouched — this module only consumes the guards it already exposes.
scanRouter.use(requireAuth, requireVerified);

/** Record today's daily scan (idempotent per calendar day) and return the refreshed summary. */
scanRouter.post(
  '/',
  validateBody(submitScanSchema),
  handle(async (req, res) => {
    if (!req.userId) throw AppError.sessionExpired();
    res.status(201).json(await service.recordScan(req.userId, req.body.localDate));
  }),
);

/** The streak / count / compliance numbers the dashboard renders. */
scanRouter.get(
  '/summary',
  handle(async (req, res) => {
    if (!req.userId) throw AppError.sessionExpired();
    const parsed = summaryQuerySchema.safeParse(req.query);
    if (!parsed.success) {
      throw AppError.validationFailed(
        parsed.error.issues.map((i) => ({ field: i.path.join('.'), message: i.message })),
      );
    }
    res.json(await service.getSummary(req.userId, parsed.data.localDate));
  }),
);

/** Recent scans, newest first — the raw list Module 6 (history) will render. */
scanRouter.get(
  '/',
  handle(async (req, res) => {
    if (!req.userId) throw AppError.sessionExpired();
    res.json({ scans: await service.listScans(req.userId) });
  }),
);

/**
 * Upload an image for today's scan (Module 4).
 *
 * Multipart form with image file and localDate. The image is stored in ScanImage; the Scan row
 * is created or touched so the summary reflects a completed scan. Idempotent by design: a retry
 * or re-upload on the same day replaces the old image but does not duplicate the scan record.
 */
scanRouter.post(
  '/image',
  upload.single('image'),
  validateBody(uploadImageSchema),
  handle(async (req, res) => {
    if (!req.userId) throw AppError.sessionExpired();
    if (!req.file) throw AppError.validationFailed([{ field: 'image', message: 'Image file is required' }]);

    const parsed = uploadImageSchema.safeParse({
      localDate: req.body.localDate,
      width: req.body.width ? parseInt(req.body.width, 10) : undefined,
      height: req.body.height ? parseInt(req.body.height, 10) : undefined,
    });

    if (!parsed.success) {
      throw AppError.validationFailed(
        parsed.error.issues.map((i) => ({ field: i.path.join('.'), message: i.message })),
      );
    }

    res.status(201).json(
      await service.recordScanWithImage(
        req.userId,
        parsed.data.localDate,
        req.file.buffer,
        req.file.mimetype,
        parsed.data.width,
        parsed.data.height,
      ),
    );
  }),
);

/**
 * Retrieve a stored scan image (Module 5 AI analysis, Module 6 history view).
 *
 * Access control: the user can only fetch images from their own scans. Returns 404 if the scan
 * does not exist or does not belong to the requesting user.
 */
scanRouter.get(
  '/:scanId/image',
  handle(async (req, res) => {
    if (!req.userId) throw AppError.sessionExpired();

    const image = await service.getImage(req.userId, req.params.scanId || '');
    if (!image) {
      res.status(404).json({ error: 'Image not found' });
    } else {
      res.set('Content-Type', image.mimeType);
      res.set('Content-Length', String(image.byteSize));
      res.send(image.data);
    }
  }),
);

/**
 * Trigger AI analysis for a scan (Module 5).
 *
 * Idempotent: if an analysis already exists for this scan, returns the existing result
 * without re-running the AI. Access control: the scan must belong to the requesting user.
 */
scanRouter.post(
  '/:scanId/analyze',
  handle(async (req, res) => {
    if (!req.userId) throw AppError.sessionExpired();

    const scan = await service.getScanById(req.userId, req.params.scanId || '');
    if (!scan) {
      throw AppError.validationFailed([{ field: 'scanId', message: 'Scan not found' }]);
    }

    const aiProvider = new MockAIProvider();
    const result = await resultService.analyzeScan(req.params.scanId || '', aiProvider);
    res.status(201).json(result);
  }),
);

/**
 * Retrieve an analysis result for a scan (Module 5).
 *
 * Returns 404 if the scan has no analysis (analysis hasn't been triggered yet, or the
 * scan does not exist / does not belong to the user).
 */
scanRouter.get(
  '/:scanId/result',
  handle(async (req, res) => {
    if (!req.userId) throw AppError.sessionExpired();

    const scan = await service.getScanById(req.userId, req.params.scanId || '');
    if (!scan) {
      throw AppError.validationFailed([{ field: 'scanId', message: 'Scan not found' }]);
    }

    const result = await resultService.getResult(req.params.scanId || '');
    if (!result) {
      res.status(404).json({ error: 'Analysis not found' });
    } else {
      res.json(result);
    }
  }),
);
