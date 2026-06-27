package com.vocab.service.estimator;

import com.vocab.data.WordListData;
import com.vocab.data.WordListData.Band;
import com.vocab.model.*;

import java.util.*;

public class ThresholdEstimator {
    private static final double THRESHOLD = 0.6;

    public EstimationResult estimate(List<WordEntry> entries) {
        Set<String> knownSet = new HashSet<>();
        Set<String> unknownSet = new HashSet<>();
        for (WordEntry e : entries) {
            (e.known ? knownSet : unknownSet).add(e.word.toLowerCase());
        }

        List<BandResult> bandResults = new ArrayList<>();
        int cutoffRank = 0;

        for (Band band : WordListData.BANDS) {
            int known = 0, total = 0;
            for (String word : band.words()) {
                String w = word.toLowerCase();
                if (knownSet.contains(w)) { known++; total++; }
                else if (unknownSet.contains(w)) { total++; }
            }
            double rate = total > 0 ? (double) known / total : 0;
            bandResults.add(new BandResult(band.label(), known, total, rate));

            if (rate >= THRESHOLD) {
                cutoffRank = band.rankMax();
            }
        }

        if (cutoffRank == 10000) {
            BandResult awl = bandResults.get(bandResults.size() - 1);
            if (awl != null && awl.rate >= THRESHOLD) {
                cutoffRank = 10570;
                List<BandResult> lastFew = bandResults.subList(Math.max(0, bandResults.size() - 4), bandResults.size());
                if (lastFew.size() >= 2) {
                    double avgDrop = (lastFew.get(0).rate - lastFew.get(lastFew.size() - 1).rate) / lastFew.size();
                    if (avgDrop > 0) {
                        cutoffRank += (int) ((1.0 - THRESHOLD) / avgDrop * 1000);
                    }
                }
            }
        }

        int margin = (int) Math.round(cutoffRank * 0.25);

        EstimationResult r = new EstimationResult();
        r.algorithm = "threshold";
        r.estimatedVocab = cutoffRank;
        r.confidenceLow = Math.max(0, cutoffRank - margin);
        r.confidenceHigh = cutoffRank + margin;
        r.bandResults = bandResults;
        r.studentTypeMatch = StudentType.fromVocab(cutoffRank).name().toLowerCase();
        return r;
    }
}
