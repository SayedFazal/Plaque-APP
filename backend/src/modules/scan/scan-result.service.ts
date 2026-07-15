import { prisma } from '../../db/prisma.js';
import { AppError } from '../../utils/errors.js';
import type { AIProvider, AnalysisResult } from './ai-provider.js';

/**
 * Analyzes a scan and stores the result.
 *
 * Idempotent by design: if a result already exists for this scan, returns the existing one
 * instead of re-analyzing. This prevents re-running (and paying for) the AI on every request.
 */
export async function analyzeScan(scanId: string, aiProvider: AIProvider): Promise<AnalysisResult> {
  // Check if the scan exists and the user has a verified account (implicitly checked by the route guard).
  const scan = await prisma.scan.findUnique({
    where: { id: scanId },
    include: { image: true },
  });

  if (!scan) {
    throw AppError.validationFailed([{ field: 'scanId', message: 'Scan not found' }]);
  }

  if (!scan.image) {
    throw AppError.validationFailed([{ field: 'image', message: 'Scan has no image; cannot analyze' }]);
  }

  // If we already have a result, return it (idempotent).
  const existing = await prisma.scanResult.findUnique({
    where: { scanId },
  });

  if (existing) {
    return {
      bleeding: existing.bleeding,
      inflammation: existing.inflammation,
      plaque: existing.plaque,
      overall_score: existing.overall_score,
      recommendations: existing.recommendations,
      provider: existing.provider,
      modelVersion: existing.modelVersion,
    };
  }

  // Run the analysis.
  const analysis = await aiProvider.analyzeScan(scan.image.data as Buffer, scan.image.mimeType);

  // Store the result.
  const result = await prisma.scanResult.create({
    data: {
      scanId,
      bleeding: analysis.bleeding,
      inflammation: analysis.inflammation,
      plaque: analysis.plaque,
      overall_score: analysis.overall_score,
      recommendations: analysis.recommendations,
      provider: analysis.provider,
      modelVersion: analysis.modelVersion,
    },
  });

  return {
    bleeding: result.bleeding,
    inflammation: result.inflammation,
    plaque: result.plaque,
    overall_score: result.overall_score,
    recommendations: result.recommendations,
    provider: result.provider,
    modelVersion: result.modelVersion,
  };
}

/**
 * Retrieve a stored analysis result.
 *
 * Returns null if the scan has no analysis (e.g., analysis hasn't been triggered yet,
 * or the scan has no image).
 */
export async function getResult(scanId: string): Promise<AnalysisResult | null> {
  const result = await prisma.scanResult.findUnique({
    where: { scanId },
  });

  if (!result) return null;

  return {
    bleeding: result.bleeding,
    inflammation: result.inflammation,
    plaque: result.plaque,
    overall_score: result.overall_score,
    recommendations: result.recommendations,
    provider: result.provider,
    modelVersion: result.modelVersion,
  };
}
