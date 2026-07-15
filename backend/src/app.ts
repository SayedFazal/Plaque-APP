import cors from 'cors';
import express from 'express';
import helmet from 'helmet';
import { errorHandler, notFound } from './middleware/index.js';
import { authRouter } from './modules/auth/auth.routes.js';

export function createApp() {
  const app = express();

  app.use(helmet());
  app.use(cors());
  // 100kb is generous for an auth payload and small enough that a junk POST cannot fill memory.
  // Module 4 will need a much larger limit for image uploads -- raise it on that route only.
  app.use(express.json({ limit: '100kb' }));

  // Behind Render/Railway the client IP arrives in X-Forwarded-For. Without this the rate limiter
  // sees every request as coming from the proxy and throttles all users as one.
  app.set('trust proxy', 1);

  app.get('/health', (_req, res) => {
    res.json({ ok: true, service: 'periocompliance-backend' });
  });

  app.use('/auth', authRouter);

  app.use(notFound);
  app.use(errorHandler);

  return app;
}
