import type { EstimationResult, BandResult, WordEntry, StudentType } from "./types";
import { frequencyBands } from "../data/wordLists";

// IRT-style Logistic model:
// P(knows | rank) = 1 / (1 + exp(beta * (rank - theta)))
// where theta = difficulty threshold (rank at which P=0.5)
//       beta = discrimination/slope parameter
//
// We estimate theta by finding the rank where knowledge probability drops below 0.5
export function estimateIrtLogistic(entries: WordEntry[]): EstimationResult {
  const knownSet = new Set(entries.filter((e) => e.known).map((e) => e.word.toLowerCase()));
  const unknownSet = new Set(entries.filter((e) => !e.known).map((e) => e.word.toLowerCase()));

  // Build data points: (rank, known=1/0) for all tested words
  const dataPoints: { rank: number; known: number }[] = [];

  for (const band of frequencyBands) {
    const bandMidRank = (band.rankMin + band.rankMax) / 2;
    for (const word of band.words) {
      const w = word.toLowerCase();
      if (knownSet.has(w)) {
        dataPoints.push({ rank: bandMidRank, known: 1 });
      } else if (unknownSet.has(w)) {
        dataPoints.push({ rank: bandMidRank, known: 0 });
      }
    }
  }

  if (dataPoints.length === 0) {
    return emptyResult();
  }

  // Sort by rank
  dataPoints.sort((a, b) => a.rank - b.rank);

  // Estimate theta (threshold) and beta (slope) using logistic regression approximation
  // We group into rank bins and compute observed proportions
  const bins = 20;
  const maxRank = 10000;
  const binSize = maxRank / bins;
  const binData: { rank: number; proportion: number; count: number }[] = [];

  for (let i = 0; i < bins; i++) {
    const binLow = i * binSize + 1;
    const binHigh = (i + 1) * binSize;
    let known = 0;
    let total = 0;
    for (const dp of dataPoints) {
      if (dp.rank >= binLow && dp.rank <= binHigh) {
        known += dp.known;
        total++;
      }
    }
    if (total >= 3) {
      binData.push({ rank: (binLow + binHigh) / 2, proportion: known / total, count: total });
    }
  }

  if (binData.length < 3) {
    // Fallback: simple estimate from overall rate
    const overallKnown = dataPoints.filter((d) => d.known === 1).length;
    const overallRate = overallKnown / dataPoints.length;
    const threshold = overallRate * maxRank;
    return buildResult(threshold, 0.0005, frequencyBands, knownSet, unknownSet);
  }

  // Find theta: the rank where observed proportion crosses 0.5
  let theta = 5000; // default mid
  for (let i = 0; i < binData.length - 1; i++) {
    if (binData[i].proportion >= 0.5 && binData[i + 1].proportion < 0.5) {
      // Linear interpolation
      const p1 = binData[i].proportion;
      const p2 = binData[i + 1].proportion;
      const r1 = binData[i].rank;
      const r2 = binData[i + 1].rank;
      theta = r1 + (0.5 - p1) / (p2 - p1) * (r2 - r1);
      break;
    }
  }

  // Estimate beta from slope around theta
  // Find two bins bracketing theta
  let beta = 0.0008; // default slope
  for (let i = 0; i < binData.length - 1; i++) {
    if (binData[i].rank <= theta && binData[i + 1].rank >= theta) {
      const dp = (binData[i + 1].proportion - binData[i].proportion) /
                 (binData[i + 1].rank - binData[i].rank);
      // beta ≈ -4 * dp (for logistic, max slope at center is beta/4)
      beta = Math.max(0.0001, Math.abs(dp) * 4);
      break;
    }
  }

  return buildResult(theta, beta, frequencyBands, knownSet, unknownSet);
}

function buildResult(
  theta: number,
  beta: number,
  bands: typeof frequencyBands,
  knownSet: Set<string>,
  unknownSet: Set<string>,
): EstimationResult {
  // Integrate logistic curve from 0 to max rank
  const maxRank = 20000;
  const steps = 20000;
  let area = 0;
  const stepSize = maxRank / steps;

  for (let i = 0; i < steps; i++) {
    const rank = (i + 0.5) * stepSize;
    const p = 1 / (1 + Math.exp(beta * (rank - theta)));
    area += p * stepSize;
  }

  const estimatedVocab = Math.round(area);
  const margin = Math.round(estimatedVocab * 0.2);

  const bandResults: BandResult[] = [];
  for (const band of bands) {
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
    algorithm: "irtLogistic",
    estimatedVocab,
    confidenceLow: Math.max(0, estimatedVocab - margin),
    confidenceHigh: estimatedVocab + margin,
    bandResults,
    studentTypeMatch: getStudentType(estimatedVocab),
  };
}

function emptyResult(): EstimationResult {
  return {
    algorithm: "irtLogistic",
    estimatedVocab: 0,
    confidenceLow: 0,
    confidenceHigh: 0,
    bandResults: [],
    studentTypeMatch: "primary",
  };
}

function getStudentType(vocab: number): StudentType {
  if (vocab < 1000) return "primary";
  if (vocab < 2500) return "junior";
  if (vocab < 4000) return "senior";
  return "college";
}
