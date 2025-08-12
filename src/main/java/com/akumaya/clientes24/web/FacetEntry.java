package com.akumaya.clientes24.web;

public class FacetEntry {
    private String key;
    private long count;

    public FacetEntry(String key, long count) {
        this.key = key;
        this.count = count;
    }

    public String getKey() { return key; }
    public long getCount() { return count; }
}