package com.vocab.service.estimator;

import com.vocab.data.WordListData;
import com.vocab.data.WordListData.Band;
import com.vocab.model.*;

import java.util.*;

public class FrequencyBandEstimator {
    public EstimationResult estimate(List<WordEntry> entries) {
        Set<String> knownSet = new HashSet<>();
        Set<String> unknownSet = new HashSet<>();
        for (WordEntry e : entries) {
            (e.known ? knownSet : unknownSet).add(e.word.toLowerCase());
        }

        List<BandResult> bandResults = new ArrayList<>();
        int totalEstimated = 0;

        for (Band band : WordListData.BANDS) {
            int known = 0, total = 0;
            for (String word : band.words()) {
                String w = word.toLowerCase();
                if (knownSet.contains(w)) { known++; total++; }
                else if (unknownSet.contains(w)) { total++; }
            }
            int bandSize = band.rankMax() - band.rankMin() + 1;
            double rate = total > 0 ? (double) known / total : 0;
            int bandEstimate = (int) Math.round(rate * bandSize);
            bandResults.add(new BandResult(band.label(), known, total, rate));
            totalEstimated += bandEstimate;
        }

        int margin = (int) Math.round(totalEstimated * 0.15);
        EstimationResult result = new EstimationResult();
        result.algorithm = "frequencyBand";
        result.estimatedVocab = totalEstimated;
        result.confidenceLow = totalEstimated - margin;
        result.confidenceHigh = totalEstimated + margin;
        result.bandResults = bandResults;
        result.studentTypeMatch = StudentType.fromVocab(totalEstimated).name().toLowerCase();
        return result;
    }
}
