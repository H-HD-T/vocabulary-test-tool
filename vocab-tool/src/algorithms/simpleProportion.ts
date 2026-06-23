import type { EstimationResult, BandResult, WordEntry, StudentType } from "./types";
import { frequencyBands, TOTAL_CORPUS_SIZE } from "../data/wordLists";

// Simple proportion: vocabulary = (known/total_tested) * total_corpus
export function estimateSimpleProportion(entries: WordEntry[]): EstimationResult {
  const knownCount = entries.filter((e) => e.known).length;
  const totalTested = entries.length;

  if (totalTested === 0) {
    return {
      algorithm: "simpleProportion",
      estimatedVocab: 0,
      confidenceLow: 0,
      confidenceHigh: 0,
      bandResults: [],
      studentTypeMatch: "primary",
    };
  }

  const rate = knownCount / totalTested;
  const estimatedVocab = Math.round(rate * TOTAL_CORPUS_SIZE);

  // Confidence interval using binomial proportion CI (Wilson score, simplified)
  const z = 1.96;
  const p = rate;
  const n = totalTested;
  const se = Math.sqrt((p * (1 - p)) / n);
  const margin = Math.round(z * se * TOTAL_CORPUS_SIZE);

  // Also compute per-band breakdown for consistency
  const bandResults: BandResult[] = [];
  const knownSet = new Set(entries.filter((e) => e.known).map((e) => e.word.toLowerCase()));
  const unknownSet = new Set(entries.filter((e) => !e.known).map((e) => e.word.toLowerCase()));

  for (const band of frequencyBands) {
    let known = 0;
    let total = 0;
    for (const word of band.words) {
      const w = word.toLowerCase();
      if (knownSet.has(w)) { known++; total++; }
      else if (unknownSet.has(w)) { total++; }
    }
    bandResults.push({
      band: band.label,
      known,
      total,
      rate: total > 0 ? known / total : 0,
    });
  }

  return {
    algorithm: "simpleProportion",
    estimatedVocab,
    confidenceLow: Math.max(0, estimatedVocab - margin),
    confidenceHigh: Math.min(TOTAL_CORPUS_SIZE, estimatedVocab + margin),
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
