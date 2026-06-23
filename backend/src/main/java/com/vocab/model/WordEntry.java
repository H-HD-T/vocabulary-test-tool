package com.vocab.model;

public class WordEntry {
    public String word;
    public boolean known;

    public WordEntry() {}

    public WordEntry(String word, boolean known) {
        this.word = word;
        this.known = known;
    }
}
