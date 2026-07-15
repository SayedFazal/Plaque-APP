import { Router, type Request, type Response, type NextFunction } from 'express';
import { requireAuth, requireVerified, validateBody } from '../../middleware/index.js';
import { submitScanSchema, summaryQuerySchema } from './scan.schemas.js';
import * as service from './scan.service.js';
import { AppError } from '../../utils/errors.js';

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
