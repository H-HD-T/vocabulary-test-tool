package com.vocab.service;

import com.vocab.model.*;
import com.vocab.service.estimator.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EstimationService {
    private final FrequencyBandEstimator frequencyBand = new FrequencyBandEstimator();
    private final SimpleProportionEstimator simpleProportion = new SimpleProportionEstimator();
    private final IrtLogisticEstimator irtLogistic = new IrtLogisticEstimator();
    private final ThresholdEstimator threshold = new ThresholdEstimator();

    public FullResult estimateAll(List<WordEntry> entries) {
        List<EstimationResult> estimates = List.of(
            frequencyBand.estimate(entries),
            simpleProportion.estimate(entries),
            irtLogistic.estimate(entries),
            threshold.estimate(entries)
        );

        long knownCount = entries.stream().filter(e -> e.known).count();
        double overallRate = entries.isEmpty() ? 0 : (double) knownCount / entries.size();

        // Mode across algorithms for matched type
        Map<String, Integer> typeCounts = new HashMap<>();
        for (EstimationResult est : estimates) {
            typeCounts.merge(est.studentTypeMatch, 1, Integer::sum);
        }
        String matchedType = typeCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("primary");

        FullResult result = new FullResult();
        result.wordCount = entries.size();
        result.knownCount = (int) knownCount;
        result.overallRate = overallRate;
        result.estimates = estimates;
        result.matchedType = matchedType;
        return result;
    }
}
