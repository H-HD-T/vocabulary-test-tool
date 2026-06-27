package com.vocab.model;

import java.util.List;

public class BandResult {
    public String band;
    public int known;
    public int total;
    public double rate;

    public BandResult() {}

    public BandResult(String band, int known, int total, double rate) {
        this.band = band;
        this.known = known;
        this.total = total;
        this.rate = rate;
    }
}
