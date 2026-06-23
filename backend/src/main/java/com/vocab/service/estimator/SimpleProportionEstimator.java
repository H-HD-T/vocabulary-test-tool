package com.vocab.service.estimator;

import com.vocab.data.WordListData;
import com.vocab.data.WordListData.Band;
import com.vocab.model.*;

import java.util.*;

public class SimpleProportionEstimator {
    public EstimationResult estimate(List<WordEntry> entries) {
        long knownCount = entries.stream().filter(e -> e.known).count();
        int totalTested = entries.size();

        if (totalTested == 0) {
            return emptyResult();
        }

        double rate = (double) knownCount / totalTested;
        int estimatedVocab = (int) Math.round(rate * WordListData.TOTAL_CORPUS_SIZE);

        double z = 1.96;
        double se = Math.sqrt(rate * (1 - rate) / totalTested);
        int margin = (int) Math.round(z * se * WordListData.TOTAL_CORPUS_SIZE);

        Set<String> knownSet = new HashSet<>();
        Set<String> unknownSet = new HashSet<>();
        for (WordEntry e : entries) {
            (e.known ? knownSet : unknownSet).add(e.word.toLowerCase());
        }

        List<BandResult> bandResults = new ArrayList<>();
        for (Band band : WordListData.BANDS) {
            int known = 0, total = 0;
            for (String word : band.words()) {
                String w = word.toLowerCase();
                if (knownSet.contains(w)) { known++; total++; }
                else if (unknownSet.contains(w)) { total++; }
            }
            bandResults.add(new BandResult(band.label(), known, total, total > 0 ? (double) known / total : 0));
        }

        EstimationResult result = new EstimationResult();
        result.algorithm = "simpleProportion";
        result.estimatedVocab = estimatedVocab;
        result.confidenceLow = Math.max(0, estimatedVocab - margin);
        result.confidenceHigh = Math.min(WordListData.TOTAL_CORPUS_SIZE, estimatedVocab + margin);
        result.bandResults = bandResults;
        result.studentTypeMatch = StudentType.fromVocab(estimatedVocab).name().toLowerCase();
        return result;
    }

    private EstimationResult emptyResult() {
        EstimationResult r = new EstimationResult();
        r.algorithm = "simpleProportion";
        r.estimatedVocab = 0;
        r.confidenceLow = 0;
        r.confidenceHigh = 0;
        r.bandResults = List.of();
        r.studentTypeMatch = "primary";
        return r;
    }
}
