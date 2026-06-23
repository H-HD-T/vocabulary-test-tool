package com.vocab.service;

import com.vocab.data.WordListData;
import com.vocab.data.WordListData.Band;
import com.vocab.model.QuizWord;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuizService {
    private static final int BATCH_SIZE = 10;
    private static final int TOTAL_QUIZ_WORDS = 60;

    public List<List<QuizWord>> sampleAllBatches() {
        List<QuizWord> pool = new ArrayList<>();
        double totalWeight = WordListData.BANDS.stream().mapToInt(b -> b.sampleCount()).sum();

        for (Band band : WordListData.BANDS) {
            int count = Math.max(3, (int) Math.round(band.sampleCount() / totalWeight * TOTAL_QUIZ_WORDS));
            List<String> shuffled = new ArrayList<>(band.words());
            Collections.shuffle(shuffled);
            for (int i = 0; i < Math.min(count, shuffled.size()); i++) {
                pool.add(new QuizWord(shuffled.get(i), band.name()));
            }
        }

        Collections.shuffle(pool);
        List<List<QuizWord>> batches = new ArrayList<>();
        for (int i = 0; i < pool.size(); i += BATCH_SIZE) {
            batches.add(pool.subList(i, Math.min(i + BATCH_SIZE, pool.size())));
        }
        return batches;
    }

    public boolean hasMoreBatches(int currentBatch) {
        int maxBatches = (int) Math.ceil((double) TOTAL_QUIZ_WORDS / BATCH_SIZE);
        return currentBatch < maxBatches - 1;
    }
}
