// API client for Java backend - direct URL to avoid proxy issues
const BASE = "/api";

export interface QuizWord {
  word: string;
  bandName: string;
}

export interface FullResult {
  wordCount: number;
  knownCount: number;
  overallRate: number;
  estimates: EstimationResult[];
  matchedType: string;
}

export interface EstimationResult {
  algorithm: string;
  estimatedVocab: number;
  confidenceLow: number;
  confidenceHigh: number;
  bandResults: BandResult[];
  studentTypeMatch: string;
}

export interface BandResult {
  band: string;
  known: number;
  total: number;
  rate: number;
}

export interface WordEntry {
  word: string;
  known: boolean;
}

export async function fetchQuizBatches(): Promise<QuizWord[][]> {
  const res = await fetch(BASE + "/quiz/words", { method: "POST" });
  if (!res.ok) throw new Error("Failed to fetch quiz words: " + res.status);
  return res.json();
}

export async function estimateVocab(entries: WordEntry[]): Promise<FullResult> {
  const res = await fetch(BASE + "/estimate", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(entries),
  });
  if (!res.ok) throw new Error("Estimation failed: " + res.status);
  return res.json();
}
