import { PrismaClient } from '@prisma/client';
import { env } from '../config/env.js';

/**
 * One client for the process. Prisma pools connections internally; constructing a second client
 * doubles the pool and will exhaust a free-tier Postgres connection limit surprisingly fast.
 */
export const prisma = new PrismaClient({
  log: env.NODE_ENV === 'development' ? ['warn', 'error'] : ['error'],
});
