import { useState, useCallback } from "react";
import Header from "./components/Header";
import QuizCard from "./components/QuizCard";
import ResultsPanel from "./components/ResultsPanel";
import { fetchQuizBatches, estimateVocab } from "./api";
import type { FullResult, WordEntry, QuizWord } from "./api";
import { StudentTypeLabels, StudentTypeVocabRange } from "./algorithms/types";
import type { StudentType } from "./algorithms/types";

type Phase = "intro" | "loading" | "quiz" | "intermediate" | "results";

export default function App() {
  const [phase, setPhase] = useState<Phase>("intro");
  const [allBatches, setAllBatches] = useState<QuizWord[][]>([]);
  const [batchIndex, setBatchIndex] = useState(0);
  const [allResponses, setAllResponses] = useState<WordEntry[]>([]);
  const [finalResult, setFinalResult] = useState<FullResult | null>(null);
  const [currentEstimate, setCurrentEstimate] = useState<{
    estimatedVocab: number;
    confidenceLow: number;
    confidenceHigh: number;
    studentType: StudentType;
  } | null>(null);
  const [error, setError] = useState<string | null>(null);

  const currentBatchWords = allBatches[batchIndex] ?? [];

  const handleStart = useCallback(async () => {
    setPhase("loading");
    setError(null);
    try {
      const batches = await fetchQuizBatches();
      setAllBatches(batches);
      setBatchIndex(0);
      setAllResponses([]);
      setFinalResult(null);
      setCurrentEstimate(null);
      setPhase("quiz");
    } catch {
      setError("无法连接后端服务，请确认 Java 后端已启动 (port 8080)。");
      setPhase("intro");
    }
  }, []);

  const handleBatchComplete = useCallback(
    async (batchResponses: { word: string; known: boolean }[]) => {
      const updated = [...allResponses, ...batchResponses];
      setAllResponses(updated);
      try {
        const result = await estimateVocab(updated);
        const avgVocab = Math.round(
          result.estimates.reduce(function (s, e) { return s + e.estimatedVocab; }, 0) /
          result.estimates.length,
        );
        const low = Math.min(...result.estimates.map(function (e) { return e.confidenceLow; }));
        const high = Math.max(...result.estimates.map(function (e) { return e.confidenceHigh; }));
        setCurrentEstimate({
          estimatedVocab: avgVocab,
          confidenceLow: low,
          confidenceHigh: high,
          studentType: result.matchedType as StudentType,
        });
      } catch { /* non-critical */ }
      setPhase("intermediate");
    },
    [allResponses],
  );

  const handleContinue = useCallback(() => {
    const nextBatch = batchIndex + 1;
    if (nextBatch < allBatches.length) {
      setBatchIndex(nextBatch);
      setPhase("quiz");
    } else {
      handleStop();
    }
  }, [batchIndex, allBatches]);

  const handleStop = useCallback(async () => {
    setPhase("loading");
    try {
      const result = await estimateVocab(allResponses);
      setFinalResult(result);
      setPhase("results");
    } catch {
      setError("估算请求失败，请检查后端是否运行。");
      setPhase("intermediate");
    }
  }, [allResponses]);

  const handleRetry = useCallback(() => {
    handleStart();
  }, [handleStart]);

  const moreAvailable = batchIndex < allBatches.length - 1;

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-lg mx-auto px-4 py-8">
        {error && (
          <div className="mb-4 bg-red-50 border border-red-200 rounded-lg p-4 text-sm text-red-700">
            {error}
          </div>
        )}

        {phase === "intro" && (
          <div className="bg-white rounded-lg border border-gray-200 p-8 text-center">
            <div className="text-5xl mb-4">📖</div>
            <h2 className="text-xl font-bold text-gray-800 mb-2">英语词汇量测试</h2>
            <p className="text-sm text-gray-500 mb-2">每轮测试 10 个单词，完成后可选择继续或查看结果。</p>
            <p className="text-xs text-gray-400 mb-6">测试越多越精确，最多 6 轮共 60 词。后端由 Java Spring Boot 驱动。</p>
            <button
              onClick={handleStart}
              className="px-8 py-3 bg-indigo-600 text-white text-base font-medium rounded-lg hover:bg-indigo-700 transition-colors active:scale-[0.97]"
            >
              开始测试
            </button>
          </div>
        )}

        {phase === "loading" && (
          <div className="bg-white rounded-lg border border-gray-200 p-12 text-center">
            <svg className="animate-spin w-8 h-8 mx-auto text-indigo-500 mb-4" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
            </svg>
            <div className="text-sm text-gray-400">正在连接后端服务…</div>
          </div>
        )}

        {phase === "quiz" && currentBatchWords.length > 0 && (
          <QuizCard words={currentBatchWords} batchIndex={batchIndex} onBatchComplete={handleBatchComplete} />
        )}

        {phase === "intermediate" && currentEstimate && (
          <div className="space-y-5">
            <div className={"rounded-lg border p-6 text-center " + (
              currentEstimate.studentType === "college" ? "bg-purple-50 border-purple-200" :
              currentEstimate.studentType === "senior" ? "bg-blue-50 border-blue-200" :
              currentEstimate.studentType === "junior" ? "bg-green-50 border-green-200" :
              "bg-orange-50 border-orange-200"
            )}>
              <div className="text-xs text-gray-500 mb-1">
                当前估算 · 精度: {allResponses.length < 20 ? "较粗略" : allResponses.length < 40 ? "一般" : "较精确"}
              </div>
              <div className="text-4xl font-bold text-gray-900 mb-1">
                {currentEstimate.estimatedVocab.toLocaleString()}
                <span className="text-base font-normal text-gray-500 ml-1">词</span>
              </div>
              <div className="text-sm text-gray-500">
                区间 {currentEstimate.confidenceLow.toLocaleString()} – {currentEstimate.confidenceHigh.toLocaleString()} 词
              </div>
              <span className={"inline-block mt-2 text-xs font-medium px-3 py-1 rounded-full " + (
                currentEstimate.studentType === "college" ? "bg-purple-100 text-purple-700" :
                currentEstimate.studentType === "senior" ? "bg-blue-100 text-blue-700" :
                currentEstimate.studentType === "junior" ? "bg-green-100 text-green-700" :
                "bg-orange-100 text-orange-700"
              )}>
                {StudentTypeLabels[currentEstimate.studentType]}水平
              </span>
              <div className="text-xs text-gray-400 mt-1">
                典型范围 {StudentTypeVocabRange[currentEstimate.studentType][0].toLocaleString()} – {StudentTypeVocabRange[currentEstimate.studentType][1].toLocaleString()} 词
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="bg-white border border-gray-200 rounded-lg p-4 text-center">
                <div className="text-xs text-gray-400 mb-1">已测单词</div>
                <div className="text-xl font-bold text-gray-800">{allResponses.length}</div>
              </div>
              <div className="bg-white border border-gray-200 rounded-lg p-4 text-center">
                <div className="text-xs text-gray-400 mb-1">认识率</div>
                <div className="text-xl font-bold text-gray-800">
                  {allResponses.length > 0 ? (allResponses.filter(function (r) { return r.known; }).length / allResponses.length * 100).toFixed(0) + "%" : "-"}
                </div>
              </div>
            </div>
            <div className="bg-white rounded-lg border border-gray-200 p-5 text-center">
              <p className="text-sm text-gray-600 mb-4">
                {moreAvailable ? "继续测试可以提高估算精度，再测 10 个词？" : "已用完全部单词，点击查看最终结果。"}
              </p>
              <div className="flex gap-3 justify-center">
                {moreAvailable && (
                  <button onClick={handleContinue} className="px-6 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition-colors active:scale-[0.97]">
                    继续测试（更精确）
                  </button>
                )}
                <button onClick={handleStop} className={"px-6 py-2.5 text-sm font-medium rounded-lg transition-colors active:scale-[0.97] " + (moreAvailable ? "bg-white border border-gray-200 text-gray-700 hover:bg-gray-50" : "bg-indigo-600 text-white hover:bg-indigo-700")}>
                  查看最终结果
                </button>
              </div>
            </div>
          </div>
        )}

        {phase === "results" && finalResult && (
          <ResultsPanel result={finalResult as any} onRetry={handleRetry} />
        )}
      </main>
    </div>
  );
}
