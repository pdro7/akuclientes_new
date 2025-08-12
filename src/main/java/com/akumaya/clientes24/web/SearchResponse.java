// src/main/java/com/akumaya/clientes24/web/SearchResponse.java
package com.akumaya.clientes24.web;

import java.util.List;

public class SearchResponse<T> {
    private List<T> items;
    private int page;
    private int size;
    private long total;

    public SearchResponse(List<T> items, int page, int size, long total) {
        this.items = items; this.page = page; this.size = size; this.total = total;
    }
    public List<T> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
}
