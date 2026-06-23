package com.vocab.model;

import java.util.List;

public class FullResult {
    public int wordCount;
    public int knownCount;
    public double overallRate;
    public List<EstimationResult> estimates;
    public String matchedType;

    public FullResult() {}
}
