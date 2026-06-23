package com.vocab.model;

public enum StudentType {
    PRIMARY("小学生", 0, 1000),
    JUNIOR("初中生", 1000, 2500),
    SENIOR("高中生", 2500, 4000),
    COLLEGE("大学生/成人", 4000, 20000);

    public final String label;
    public final int minVocab;
    public final int maxVocab;

    StudentType(String label, int minVocab, int maxVocab) {
        this.label = label;
        this.minVocab = minVocab;
        this.maxVocab = maxVocab;
    }

    public static StudentType fromVocab(int vocab) {
        if (vocab < 1000) return PRIMARY;
        if (vocab < 2500) return JUNIOR;
        if (vocab < 4000) return SENIOR;
        return COLLEGE;
    }
}
