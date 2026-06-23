import type { EstimationResult, BandResult, WordEntry, StudentType } from "./types";
import { frequencyBands } from "../data/wordLists";

// Threshold method: find the highest frequency band where the user still
// scores >= 60% correct, and estimate vocabulary at the midpoint of next band
export function estimateThreshold(entries: WordEntry[]): EstimationResult {
  const knownSet = new Set(entries.filter((e) => e.known).map((e) => e.word.toLowerCase()));
  const unknownSet = new Set(entries.filter((e) => !e.known).map((e) => e.word.toLowerCase()));

  const THRESHOLD = 0.6;
  const bandResults: BandResult[] = [];
  let cutoffRank = 0;

  for (const band of frequencyBands) {
    let known = 0;
    let total = 0;
    for (const word of band.words) {
      const w = word.toLowerCase();
      if (knownSet.has(w)) { known++; total++; }
      else if (unknownSet.has(w)) { total++; }
    }
    const rate = total > 0 ? known / total : 0;
    bandResults.push({ band: band.label, known, total, rate });

    if (rate >= THRESHOLD) {
      cutoffRank = band.rankMax;
    }
  }

  // If user knows all tested bands well, extend beyond
  if (cutoffRank === 10000) {
    // Check if AWL also passes
    const awlBand = bandResults[bandResults.length - 1];
    if (awlBand && awlBand.rate >= THRESHOLD) {
      cutoffRank = 10570;
      // Estimate beyond based on trend
      const lastRates = bandResults.slice(-4).map((b) => b.rate);
      const avgDrop = lastRates.length >= 2
        ? (lastRates[0] - lastRates[lastRates.length - 1]) / lastRates.length
        : 0.05;
      if (avgDrop > 0) {
        const extraBands = Math.floor((1 - THRESHOLD) / avgDrop);
        cutoffRank += extraBands * 1000;
      }
    }
  }

  // Estimate vocabulary at the cutoff
  const estimatedVocab = cutoffRank;
  const margin = Math.round(estimatedVocab * 0.25);

  return {
    algorithm: "threshold",
    estimatedVocab,
    confidenceLow: Math.max(0, estimatedVocab - margin),
    confidenceHigh: estimatedVocab + margin,
    bandResults,
    studentTypeMatch: getStudentType(estimatedVocab),
  };
}

function getStudentType(vocab: number): StudentType {
  if (vocab < 1000) return "primary";
  if (vocab < 2500) return "junior";
  if (vocab < 4000) return "senior";
  return "college";
}
