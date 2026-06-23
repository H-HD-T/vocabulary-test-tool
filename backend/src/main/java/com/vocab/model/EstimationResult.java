package com.vocab.model;

import java.util.List;

public class EstimationResult {
    public String algorithm;
    public int estimatedVocab;
    public int confidenceLow;
    public int confidenceHigh;
    public List<BandResult> bandResults;
    public String studentTypeMatch;

    public EstimationResult() {}
}
