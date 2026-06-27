import type { EstimationResult, BandResult, WordEntry, StudentType } from "./types";
import { frequencyBands } from "../data/wordLists";

// Map user words to frequency bands and estimate vocabulary
export function estimateFrequencyBand(entries: WordEntry[]): EstimationResult {
  const knownWords = new Set(
    entries.filter((e) => e.known).map((e) => e.word.toLowerCase())
  );
  const unknownWords = new Set(
    entries.filter((e) => !e.known).map((e) => e.word.toLowerCase())
  );

  const bandResults: BandResult[] = [];
  let totalEstimated = 0;

  for (const band of frequencyBands) {
    let known = 0;
    let total = 0;

    for (const word of band.words) {
      const w = word.toLowerCase();
      if (knownWords.has(w)) { known++; total++; }
      else if (unknownWords.has(w)) { total++; }
    }

    const bandSize = band.rankMax - band.rankMin + 1;
    const rate = total > 0 ? known / total : 0;
    const bandEstimate = Math.round(rate * bandSize);

    bandResults.push({
      band: band.label,
      known,
      total,
      rate,
    });

    totalEstimated += bandEstimate;
  }

  // Confidence interval: ±15% of estimate
  const margin = Math.round(totalEstimated * 0.15);

  return {
    algorithm: "frequencyBand",
    estimatedVocab: totalEstimated,
    confidenceLow: totalEstimated - margin,
    confidenceHigh: totalEstimated + margin,
    bandResults,
    studentTypeMatch: getStudentType(totalEstimated),
  };
}

function getStudentType(vocab: number): StudentType {
  if (vocab < 1000) return "primary";
  if (vocab < 2500) return "junior";
  if (vocab < 4000) return "senior";
  return "college";
}
