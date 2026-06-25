package com.vocab.estimator.algorithm;

import org.springframework.stereotype.Component;
import java.util.*;

/**
 * Algorithm validation module
 * Compares our algorithm results against testyourvocab.com standard results
 * Computes error, bias, and Pearson correlation
 */
@Component
public class AlgorithmValidator {

    /**
     * Validate algorithm against reference data
     * @param validationData list of {knownWords, unknownWords, standardEstimate}
     * @param estimator the algorithm to validate
     * @return validation results with statistics
     */
    public ValidationReport validate(List<Map<String, Object>> validationData, VocabEstimator estimator) {
        List<ValidationItem> items = new ArrayList<>();
        
        for (Map<String, Object> data : validationData) {
            @SuppressWarnings("unchecked")
            List<String> knownWords = (List<String>) data.get("knownWords");
            @SuppressWarnings("unchecked")
            List<String> unknownWords = (List<String>) data.get("unknownWords");
            int standardEstimate = (int) data.get("standardEstimate");
            
            // Build word results with default difficulty/frequency
            List<Map<String, Object>> wordResults = new ArrayList<>();
            for (String w : knownWords) {
                Map<String, Object> item = new HashMap<>();
                item.put("word", w);
                item.put("known", true);
                item.put("difficulty", guessDifficulty(w));
                item.put("frequency", 0.5);
                wordResults.add(item);
            }
            for (String w : unknownWords) {
                Map<String, Object> item = new HashMap<>();
                item.put("word", w);
                item.put("known", false);
                item.put("difficulty", guessDifficulty(w));
                item.put("frequency", 0.5);
                wordResults.add(item);
            }
            
            AlgorithmResult result = estimator.estimate(wordResults);
            int diff = result.getEstimate() - standardEstimate;
            
            items.add(new ValidationItem(
                knownWords, unknownWords, standardEstimate, result.getEstimate(), diff
            ));
        }
        
        // Compute statistics
        double meanError = items.stream().mapToInt(i -> Math.abs(i.getDiff())).average().orElse(0);
        double meanBias = items.stream().mapToInt(ValidationItem::getDiff).average().orElse(0);
        double correlation = computeCorrelation(items);
        
        return new ValidationReport(items, meanError, meanBias, correlation, items.size());
    }

    private double computeCorrelation(List<ValidationItem> items) {
        int n = items.size();
        if (n < 2) return 0;
        
        double sumStd = items.stream().mapToInt(ValidationItem::getStandardEstimate).sum();
        double sumAlg = items.stream().mapToInt(ValidationItem::getAlgorithmEstimate).sum();
        double meanStd = sumStd / n;
        double meanAlg = sumAlg / n;
        
        double cov = 0, varStd = 0, varAlg = 0;
        for (ValidationItem item : items) {
            double dStd = item.getStandardEstimate() - meanStd;
            double dAlg = item.getAlgorithmEstimate() - meanAlg;
            cov += dStd * dAlg;
            varStd += dStd * dStd;
            varAlg += dAlg * dAlg;
        }
        
        double denom = Math.sqrt(varStd * varAlg);
        return denom > 0 ? cov / denom : 0;
    }

    /**
     * Guess difficulty level by word length (simplified heuristic)
     * In production, look up from vocabulary database
     */
    private String guessDifficulty(String word) {
        int len = word.length();
        if (len <= 4) return "K";
        if (len <= 6) return "P";
        if (len <= 8) return "F";
        return "C";
    }

    // Inner classes
    public static class ValidationItem {
        private List<String> knownWords;
        private List<String> unknownWords;
        private int standardEstimate;
        private int algorithmEstimate;
        private int diff;
        
        public ValidationItem() {}
        public ValidationItem(List<String> kw, List<String> uw, int std, int alg, int d) {
            this.knownWords = kw; this.unknownWords = uw;
            this.standardEstimate = std; this.algorithmEstimate = alg; this.diff = d;
        }
        
        public List<String> getKnownWords() { return knownWords; }
        public List<String> getUnknownWords() { return unknownWords; }
        public int getStandardEstimate() { return standardEstimate; }
        public int getAlgorithmEstimate() { return algorithmEstimate; }
        public int getDiff() { return diff; }
    }

    public static class ValidationReport {
        private List<ValidationItem> items;
        private double meanError;
        private double meanBias;
        private double correlation;
        private int sampleCount;
        
        public ValidationReport() {}
        public ValidationReport(List<ValidationItem> items, double me, double mb, double corr, int sc) {
            this.items = items; this.meanError = me; this.meanBias = mb;
            this.correlation = corr; this.sampleCount = sc;
        }
        
        public List<ValidationItem> getItems() { return items; }
        public double getMeanError() { return meanError; }
        public double getMeanBias() { return meanBias; }
        public double getCorrelation() { return correlation; }
        public int getSampleCount() { return sampleCount; }
    }
}
