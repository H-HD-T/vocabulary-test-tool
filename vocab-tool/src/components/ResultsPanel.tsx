import type { StudentType } from "../algorithms/types";
import { StudentTypeLabels, StudentTypeVocabRange } from "../algorithms/types";

// Accept any estimate shape with string algorithm id (from API)
type ApiEstimate = {
  algorithm: string;
  estimatedVocab: number;
  confidenceLow: number;
  confidenceHigh: number;
  bandResults: { band: string; known: number; total: number; rate: number }[];
  studentTypeMatch: string;
};

type ApiResult = {
  wordCount: number;
  knownCount: number;
  overallRate: number;
  estimates: ApiEstimate[];
  matchedType: StudentType;
};

interface ResultsPanelProps {
  result: ApiResult;
  onRetry: () => void;
}

const typeColors: Record<StudentType, string> = {
  primary: "bg-orange-50 border-orange-200 text-orange-700",
  junior: "bg-green-50 border-green-200 text-green-700",
  senior: "bg-blue-50 border-blue-200 text-blue-700",
  college: "bg-purple-50 border-purple-200 text-purple-700",
};

const typeBadgeColors: Record<StudentType, string> = {
  primary: "bg-orange-100 text-orange-700",
  junior: "bg-green-100 text-green-700",
  senior: "bg-blue-100 text-blue-700",
  college: "bg-purple-100 text-purple-700",
};

function barColor(rate: number): string {
  if (rate >= 0.8) return "bg-emerald-500";
  if (rate >= 0.6) return "bg-green-400";
  if (rate >= 0.4) return "bg-yellow-400";
  if (rate > 0) return "bg-red-400";
  return "bg-gray-200";
}

// Algorithm name map
const algoLabel: Record<string, string> = {
  frequencyBand: "频段比例法",
  simpleProportion: "简单比例法",
  irtLogistic: "IRT逻辑回归法",
  threshold: "阈值截断法",
};

export default function ResultsPanel({ result, onRetry }: ResultsPanelProps) {
  const { wordCount, knownCount, overallRate, estimates, matchedType } = result;
  const [minV, maxV] = StudentTypeVocabRange[matchedType];
  const avgEstimate = Math.round(estimates.reduce(function (s: number, e: ApiEstimate) { return s + e.estimatedVocab; }, 0) / estimates.length);

  return (
    <div className="space-y-5">
      {/* Hero result card */}
      <div className={"rounded-lg border p-6 text-center " + typeColors[matchedType]}>
        <div className="text-xs font-medium uppercase tracking-wide mb-2 opacity-70">
          估算词汇量
        </div>
        <div className="text-5xl font-bold mb-1">
          {avgEstimate.toLocaleString()}
          <span className="text-lg font-normal opacity-60 ml-1">词</span>
        </div>
        <div className="text-sm opacity-70 mt-2">
          区间 {Math.min(...estimates.map(function (e: ApiEstimate) { return e.confidenceLow; })).toLocaleString()} – {Math.max(...estimates.map(function (e: ApiEstimate) { return e.confidenceHigh; })).toLocaleString()} 词
        </div>
        <span className={"inline-block mt-3 text-xs font-medium px-3 py-1 rounded-full " + typeBadgeColors[matchedType]}>
          {StudentTypeLabels[matchedType]}水平
        </span>
        <div className="text-xs opacity-60 mt-2">
          典型范围 {minV.toLocaleString()} – {maxV.toLocaleString()} 词
        </div>
      </div>

      {/* Stats row */}
      <div className="grid grid-cols-3 gap-3">
        <div className="bg-white border border-gray-200 rounded-lg p-4 text-center">
          <div className="text-xs text-gray-400 mb-1">测试单词</div>
          <div className="text-xl font-bold text-gray-800">{wordCount}</div>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg p-4 text-center">
          <div className="text-xs text-gray-400 mb-1">认识</div>
          <div className="text-xl font-bold text-gray-800">{knownCount}</div>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg p-4 text-center">
          <div className="text-xs text-gray-400 mb-1">正确率</div>
          <div className="text-xl font-bold text-gray-800">{(overallRate * 100).toFixed(0)}%</div>
        </div>
      </div>

      {/* Band breakdown */}
      {estimates.length > 0 && estimates[0].bandResults.length > 0 && (
        <div className="bg-white border border-gray-200 rounded-lg p-5">
          <h3 className="text-sm font-semibold text-gray-700 mb-3">各频段表现</h3>
          <div className="space-y-2">
            {estimates[0].bandResults.map(function (br: { band: string; known: number; total: number; rate: number }) {
              const pct = (br.rate * 100).toFixed(0);
              return (
                <div key={br.band} className="flex items-center gap-2">
                  <span className="text-xs text-gray-500 w-28 flex-shrink-0">{br.band}</span>
                  <div className="flex-1 h-4 bg-gray-100 rounded-full overflow-hidden">
                    <div
                      className={"h-full rounded-full transition-all duration-700 " + barColor(br.rate)}
                      style={{ width: Math.max(br.rate * 100, 2) + "%" }}
                    />
                  </div>
                  <span className="text-xs text-gray-400 w-14 text-right flex-shrink-0">
                    {br.total > 0 ? pct + "%" : "-"}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Per-algorithm details (collapsible) */}
      <details className="bg-white border border-gray-200 rounded-lg p-5 group">
        <summary className="text-sm font-semibold text-gray-700 cursor-pointer list-none flex items-center justify-between">
          各算法详细结果
          <svg className="w-4 h-4 text-gray-400 group-open:rotate-180 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
          </svg>
        </summary>
        <div className="mt-3 space-y-2">
          {estimates.map(function (est: ApiEstimate) {
            return (
              <div key={est.algorithm} className="flex items-center justify-between py-1.5 border-b border-gray-50 last:border-0">
                <span className="text-xs text-gray-600">{algoLabel[est.algorithm] ?? est.algorithm}</span>
                <span className="text-xs font-medium text-gray-800">
                  {est.estimatedVocab.toLocaleString()} 词
                  <span className="text-gray-400 ml-1">
                    ({est.confidenceLow.toLocaleString()}–{est.confidenceHigh.toLocaleString()})
                  </span>
                </span>
              </div>
            );
          })}
        </div>
      </details>

      {/* Retry */}
      <div className="text-center">
        <button
          onClick={onRetry}
          className="px-5 py-2 bg-indigo-600 text-white text-sm font-medium rounded-md hover:bg-indigo-700 transition-colors"
        >
          重新测试
        </button>
      </div>
    </div>
  );
}
