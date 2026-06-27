import { useState, useEffect, useCallback, useRef } from "react";

export const BATCH_SIZE = 10;

interface QuizWord { word: string; bandName: string }

interface QuizCardProps {
  words: QuizWord[];
  batchIndex: number;
  onBatchComplete: (responses: { word: string; known: boolean }[], batchIndex: number) => void;
}

export default function QuizCard({ words, batchIndex, onBatchComplete }: QuizCardProps) {
  const [index, setIndex] = useState(0);
  const [responses, setResponses] = useState<{ word: string; known: boolean }[]>([]);
  const containerRef = useRef<HTMLDivElement>(null);

  // Reset when batch changes
  useEffect(() => {
    setIndex(0);
    setResponses([]);
    containerRef.current?.focus();
  }, [batchIndex]);

  // Stop rendering if words is empty or index is out of bounds
  if (words.length === 0) {
    return <div className="bg-white rounded-lg border border-gray-200 p-12 text-center"><div className="text-gray-400 text-sm">正在准备单词列表…</div></div>;
  }

  // Guard: don't try to access out-of-bounds index
  const safeIndex = Math.min(index, words.length - 1);
  const currentWord = words[safeIndex];
  const isComplete = index >= words.length;
  const progress = words.length > 0 ? (safeIndex / words.length) * 100 : 0;

  // Keyboard support
  function handleKey(e: KeyboardEvent) {
    if (isComplete) return;
    if (e.key === "ArrowLeft" || e.key === "1") {
      handleAnswer(false);
    } else if (e.key === "ArrowRight" || e.key === "2") {
      handleAnswer(true);
    }
  }

  useEffect(() => {
    window.addEventListener("keydown", handleKey);
    return () => window.removeEventListener("keydown", handleKey);
  });

  const handleAnswer = useCallback((known: boolean) => {
    if (isComplete) return;
    const newResponses = [...responses, { word: currentWord.word, known }];
    setResponses(newResponses);
    const nextIndex = index + 1;
    if (nextIndex >= words.length) {
      onBatchComplete(newResponses, batchIndex);
    }
    setIndex(nextIndex);
    containerRef.current?.focus();
  }, [index, responses, currentWord, words.length, isComplete, onBatchComplete, batchIndex]);

  if (isComplete) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-12 text-center">
        <div className="text-4xl mb-4">✅</div>
        <div className="text-lg font-semibold text-gray-800 mb-2">本轮完成</div>
        <div className="text-sm text-gray-400">正在计算结果…</div>
      </div>
    );
  }

  return (
    <div ref={containerRef} tabIndex={0} className="bg-white rounded-lg border border-gray-200 p-8 focus:outline-none">
      <div className="mb-8">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs text-gray-400">第 {batchIndex + 1} 轮 · {safeIndex + 1} / {words.length} 词</span>
          <span className="text-xs text-gray-400">{Math.round(progress)}%</span>
        </div>
        <div className="h-1.5 bg-gray-100 rounded-full overflow-hidden">
          <div className="h-full bg-indigo-500 rounded-full transition-all duration-300" style={{ width: progress + "%" }} />
        </div>
      </div>

      <div className="text-center mb-10">
        <div className="text-5xl font-bold text-gray-900 mb-2 tracking-tight select-none">{currentWord.word}</div>
        <div className="text-xs text-gray-400">按 ← → 方向键 或 1/2 键快速选择</div>
      </div>

      <div className="flex gap-4 justify-center">
        <button onClick={() => handleAnswer(false)} className="flex-1 max-w-48 py-3 px-6 bg-red-50 text-red-700 border border-red-200 rounded-lg text-lg font-medium hover:bg-red-100 active:scale-[0.97] transition-all select-none">
          ✗ 不认识
        </button>
        <button onClick={() => handleAnswer(true)} className="flex-1 max-w-48 py-3 px-6 bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-lg text-lg font-medium hover:bg-emerald-100 active:scale-[0.97] transition-all select-none">
          ✓ 认识
        </button>
      </div>
    </div>
  );
}
