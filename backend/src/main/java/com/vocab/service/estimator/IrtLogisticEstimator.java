package com.vocab.service.estimator;

import com.vocab.data.WordListData;
import com.vocab.data.WordListData.Band;
import com.vocab.model.*;

import java.util.*;

public class IrtLogisticEstimator {
    public EstimationResult estimate(List<WordEntry> entries) {
        Set<String> knownSet = new HashSet<>();
        Set<String> unknownSet = new HashSet<>();
        for (WordEntry e : entries) {
            (e.known ? knownSet : unknownSet).add(e.word.toLowerCase());
        }

        List<DataPoint> dataPoints = new ArrayList<>();
        for (Band band : WordListData.BANDS) {
            double bandMidRank = (band.rankMin() + band.rankMax()) / 2.0;
            for (String word : band.words()) {
                String w = word.toLowerCase();
                if (knownSet.contains(w)) dataPoints.add(new DataPoint(bandMidRank, 1));
                else if (unknownSet.contains(w)) dataPoints.add(new DataPoint(bandMidRank, 0));
            }
        }

        if (dataPoints.isEmpty()) return emptyResult();

        dataPoints.sort(Comparator.comparingDouble(a -> a.rank));

        int bins = 20;
        double maxRank = 10000;
        double binSize = maxRank / bins;
        List<BinData> binData = new ArrayList<>();

        for (int i = 0; i < bins; i++) {
            double binLow = i * binSize + 1;
            double binHigh = (i + 1) * binSize;
            int known = 0, total = 0;
            for (DataPoint dp : dataPoints) {
                if (dp.rank >= binLow && dp.rank <= binHigh) {
                    known += dp.known;
                    total++;
                }
            }
            if (total >= 3) {
                binData.add(new BinData((binLow + binHigh) / 2, (double) known / total));
            }
        }

        if (binData.size() < 3) {
            long overallKnown = dataPoints.stream().filter(d -> d.known == 1).count();
            double overallRate = (double) overallKnown / dataPoints.size();
            return buildResult(overallRate * maxRank, 0.0005, knownSet, unknownSet);
        }

        double theta = 5000;
        for (int i = 0; i < binData.size() - 1; i++) {
            if (binData.get(i).proportion >= 0.5 && binData.get(i + 1).proportion < 0.5) {
                double p1 = binData.get(i).proportion;
                double p2 = binData.get(i + 1).proportion;
                double r1 = binData.get(i).rank;
                double r2 = binData.get(i + 1).rank;
                theta = r1 + (0.5 - p1) / (p2 - p1) * (r2 - r1);
                break;
            }
        }

        double beta = 0.0008;
        for (int i = 0; i < binData.size() - 1; i++) {
            if (binData.get(i).rank <= theta && binData.get(i + 1).rank >= theta) {
                double dp = (binData.get(i + 1).proportion - binData.get(i).proportion)
                    / (binData.get(i + 1).rank - binData.get(i).rank);
                beta = Math.max(0.0001, Math.abs(dp) * 4);
                break;
            }
        }

        return buildResult(theta, beta, knownSet, unknownSet);
    }

    private EstimationResult buildResult(double theta, double beta, Set<String> knownSet, Set<String> unknownSet) {
        double maxRank = 20000;
        int steps = 20000;
        double stepSize = maxRank / steps;
        double area = 0;

        for (int i = 0; i < steps; i++) {
            double rank = (i + 0.5) * stepSize;
            double p = 1.0 / (1.0 + Math.exp(beta * (rank - theta)));
            area += p * stepSize;
        }

        int estimatedVocab = (int) Math.round(area);
        int margin = (int) Math.round(estimatedVocab * 0.2);

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

        EstimationResult r = new EstimationResult();
        r.algorithm = "irtLogistic";
        r.estimatedVocab = estimatedVocab;
        r.confidenceLow = Math.max(0, estimatedVocab - margin);
        r.confidenceHigh = estimatedVocab + margin;
        r.bandResults = bandResults;
        r.studentTypeMatch = StudentType.fromVocab(estimatedVocab).name().toLowerCase();
        return r;
    }

    private EstimationResult emptyResult() {
        EstimationResult r = new EstimationResult();
        r.algorithm = "irtLogistic";
        r.estimatedVocab = 0;
        r.confidenceLow = 0;
        r.confidenceHigh = 0;
        r.bandResults = List.of();
        r.studentTypeMatch = "primary";
        return r;
    }

    record DataPoint(double rank, int known) {}
    record BinData(double rank, double proportion) {}
}
