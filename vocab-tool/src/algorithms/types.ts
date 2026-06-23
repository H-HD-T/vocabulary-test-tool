// Word entry from user input: "word，认识" or "word，不认识"
export interface WordEntry {
  word: string;
  known: boolean;
}

// Student type / proficiency level
export type StudentType = "primary" | "junior" | "senior" | "college";

export const StudentTypeLabels: Record<StudentType, string> = {
  primary: "小学生",
  junior: "初中生",
  senior: "高中生",
  college: "大学生/成人",
};

export const StudentTypeVocabRange: Record<StudentType, [number, number]> = {
  primary: [0, 1000],
  junior: [1000, 2500],
  senior: [2500, 4000],
  college: [4000, 20000],
};

// Frequency band definition
export interface FrequencyBand {
  name: string;
  label: string;
  rankMin: number;
  rankMax: number;
  /** Words in this band available for testing */
  words: string[];
  /** Target sample count per band */
  sampleCount: number;
}

// Estimation algorithm identifiers
export type AlgorithmId = "frequencyBand" | "simpleProportion" | "irtLogistic" | "threshold";

export const AlgorithmLabels: Record<AlgorithmId, string> = {
  frequencyBand: "频段比例法",
  simpleProportion: "简单比例法",
  irtLogistic: "IRT逻辑回归法",
  threshold: "阈值截断法",
};

export const AlgorithmDescriptions: Record<AlgorithmId, string> = {
  frequencyBand: "按词频分段采样，根据各频段正确率加权估算总词汇量。模拟 Nation VST 测试原理。",
  simpleProportion: "总词汇量 = 认识词数 / 测试词数 × 语料库总词数。简单直观。",
  irtLogistic: "使用 Logistic 回归拟合词频-认知概率曲线，通过积分估算词汇量。理论精度较高。",
  threshold: "找到用户正确率低于 60% 的频段作为词汇量上限，适用快速粗略估计。",
};

// Result data
export interface BandResult {
  band: string;
  known: number;
  total: number;
  rate: number;
}

export interface EstimationResult {
  algorithm: AlgorithmId;
  estimatedVocab: number;
  confidenceLow: number;
  confidenceHigh: number;
  bandResults: BandResult[];
  studentTypeMatch: StudentType;
}

export interface FullResult {
  wordCount: number;
  knownCount: number;
  overallRate: number;
  estimates: EstimationResult[];
  matchedType: StudentType;
}
