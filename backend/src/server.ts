import { createApp } from './app.js';
import { env, hasSmtp } from './config/env.js';
import { prisma } from './db/prisma.js';

const app = createApp();

const server = app.listen(env.PORT, () => {
  console.log(`PerioCompliance backend listening on :${env.PORT}  [${env.NODE_ENV}]`);
  if (!hasSmtp) {
    console.log('SMTP not configured — verification and reset emails will be printed here.');
  }
});

// Without this, a container restart cuts live requests mid-flight and leaves Postgres connections
// dangling until they time out.
const shutdown = async (signal: string) => {
  console.log(`${signal} received, shutting down.`);
  server.close(async () => {
    await prisma.$disconnect();
    process.exit(0);
  });
};

process.on('SIGTERM', () => void shutdown('SIGTERM'));
process.on('SIGINT', () => void shutdown('SIGINT'));
