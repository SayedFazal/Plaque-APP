/**
 * AI provider abstraction for gum health analysis (Module 5).
 *
 * Allows swapping the underlying AI implementation (mock, Claude, OpenAI, AWS Rekognition, etc.)
 * without touching the routes or service layer. The [AnalysisResult] payload is standardized
 * regardless of provider, so the client and database schema stay stable across provider changes.
 */

export interface AnalysisResult {
  bleeding: number; // 0-100 (%)
  inflammation: number; // 0-100 (%)
  plaque: number; // 0-100 (%)
  overall_score: number; // 0-100 (%)
  recommendations: string[];
  provider: string; // "mock", "claude", "openai", etc.
  modelVersion: string; // e.g. "1.0", "gpt-4-vision"
}

export interface AIProvider {
  /**
   * Analyze a gum scan image and return health metrics.
   *
   * @param imageBytes The JPEG bytes (as sent to the server in Module 4).
   * @param mimeType The image MIME type (e.g. "image/jpeg").
   * @throws If the analysis fails (network, API limit, invalid image, etc.).
   */
  analyzeScan(imageBytes: Buffer, mimeType: string): Promise<AnalysisResult>;
}

/**
 * Mock AI provider for development and testing.
 *
 * Returns realistic sample analysis values (randomized slightly so each call is different,
 * but within reasonable ranges for a gum health assessment). Replace with a real provider
 * (Claude, OpenAI, etc.) in production.
 */
export class MockAIProvider implements AIProvider {
  async analyzeScan(): Promise<AnalysisResult> {
    // Simulate a small delay (API call would take time).
    await new Promise((r) => setTimeout(r, 500));

    // Return realistic but varied sample metrics.
    const bleeding = Math.floor(Math.random() * 40) + 5; // 5-45%
    const inflammation = Math.floor(Math.random() * 50) + 10; // 10-60%
    const plaque = Math.floor(Math.random() * 60) + 10; // 10-70%

    // Overall score is a weighted average.
    const overall_score = Math.round((bleeding + inflammation + plaque * 1.2) / 3.2);

    const recommendations = this.generateRecommendations(bleeding, inflammation, plaque);

    return {
      bleeding,
      inflammation,
      plaque,
      overall_score: Math.min(100, overall_score),
      recommendations,
      provider: 'mock',
      modelVersion: '1.0-demo',
    };
  }

  private generateRecommendations(bleeding: number, inflammation: number, plaque: number): string[] {
    const recs: string[] = [];

    if (bleeding > 30) {
      recs.push('Reduce pressure when brushing; use a soft-bristled toothbrush.');
    }
    if (bleeding > 50) {
      recs.push('Consider scheduling a professional cleaning with your dentist.');
    }

    if (inflammation > 40) {
      recs.push('Increase flossing frequency to twice daily.');
    }
    if (inflammation > 60) {
      recs.push('Rinse with an anti-inflammatory mouthwash; consult your dentist if inflammation persists.');
    }

    if (plaque > 40) {
      recs.push('Brush longer and pay special attention to the gum line.');
    }
    if (plaque > 60) {
      recs.push('A professional scaling may be needed; contact your dentist.');
    }

    if (recs.length === 0) {
      recs.push('Your gum health looks good! Continue your current oral care routine.');
    }

    return recs;
  }
}
