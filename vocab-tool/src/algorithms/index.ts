import type { AlgorithmId, EstimationResult, FullResult, WordEntry, StudentType } from "./types";
import { estimateFrequencyBand } from "./frequencyBand";
import { estimateSimpleProportion } from "./simpleProportion";
import { estimateIrtLogistic } from "./irtModel";
import { estimateThreshold } from "./thresholdMethod";
import { StudentTypeLabels } from "./types";

const estimators: Record<AlgorithmId, (entries: WordEntry[]) => EstimationResult> = {
  frequencyBand: estimateFrequencyBand,
  simpleProportion: estimateSimpleProportion,
  irtLogistic: estimateIrtLogistic,
  threshold: estimateThreshold,
};

export function runEstimation(
  entries: WordEntry[],
  algorithms: AlgorithmId[],
): FullResult {
  const knownCount = entries.filter((e) => e.known).length;
  const wordCount = entries.length;
  const overallRate = wordCount > 0 ? knownCount / wordCount : 0;

  const estimates = algorithms.map((alg) => estimators[alg](entries));

  // Determine best-matched student type (mode across algorithms)
  const typeCounts: Record<StudentType, number> = { primary: 0, junior: 0, senior: 0, college: 0 };
  for (const est of estimates) {
    typeCounts[est.studentTypeMatch]++;
  }
  let matchedType: StudentType = "primary";
  let maxCount = 0;
  for (const [type, count] of Object.entries(typeCounts)) {
    if (count > maxCount) {
      maxCount = count;
      matchedType = type as StudentType;
    }
  }

  return {
    wordCount,
    knownCount,
    overallRate,
    estimates,
    matchedType,
  };
}

// Human-readable student type with vocab range
export function getStudentTypeLabel(type: StudentType): string {
  return StudentTypeLabels[type];
}
