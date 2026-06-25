package com.vocab.estimator.dto;

import java.util.List;

public class ValidationDTO {
    private List<ValidationItem> items;
    private Double meanError;
    private Double meanBias;
    private Double correlation;
    private Integer sampleCount;

    public ValidationDTO() {}
    public ValidationDTO(List<ValidationItem> items, Double meanError,
                        Double meanBias, Double correlation, Integer sampleCount) {
        this.items = items; this.meanError = meanError;
        this.meanBias = meanBias; this.correlation = correlation; this.sampleCount = sampleCount;
    }
    public List<ValidationItem> getItems() { return items; }
    public void setItems(List<ValidationItem> items) { this.items = items; }
    public Double getMeanError() { return meanError; }
    public void setMeanError(Double meanError) { this.meanError = meanError; }
    public Double getMeanBias() { return meanBias; }
    public void setMeanBias(Double meanBias) { this.meanBias = meanBias; }
    public Double getCorrelation() { return correlation; }
    public void setCorrelation(Double correlation) { this.correlation = correlation; }
    public Integer getSampleCount() { return sampleCount; }
    public void setSampleCount(Integer sampleCount) { this.sampleCount = sampleCount; }

    public static class ValidationItem {
        private List<String> knownWords;
        private List<String> unknownWords;
        private Integer standardEstimate;
        private Integer algorithmEstimate;
        private Integer diff;

        public ValidationItem() {}
        public ValidationItem(List<String> knownWords, List<String> unknownWords,
                            Integer standardEstimate, Integer algorithmEstimate, Integer diff) {
            this.knownWords = knownWords; this.unknownWords = unknownWords;
            this.standardEstimate = standardEstimate; this.algorithmEstimate = algorithmEstimate; this.diff = diff;
        }
        public List<String> getKnownWords() { return knownWords; }
        public void setKnownWords(List<String> knownWords) { this.knownWords = knownWords; }
        public List<String> getUnknownWords() { return unknownWords; }
        public void setUnknownWords(List<String> unknownWords) { this.unknownWords = unknownWords; }
        public Integer getStandardEstimate() { return standardEstimate; }
        public void setStandardEstimate(Integer standardEstimate) { this.standardEstimate = standardEstimate; }
        public Integer getAlgorithmEstimate() { return algorithmEstimate; }
        public void setAlgorithmEstimate(Integer algorithmEstimate) { this.algorithmEstimate = algorithmEstimate; }
        public Integer getDiff() { return diff; }
        public void setDiff(Integer diff) { this.diff = diff; }
    }
}
