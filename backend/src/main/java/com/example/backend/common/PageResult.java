package com.example.backend.common;

import java.util.List;

public class PageResult<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static <T> PageResult<T> of(List<T> content, int page, int size, long total) {
        PageResult<T> result = new PageResult<T>();
        result.content = content;
        result.page = page;
        result.size = size;
        result.totalElements = total;
        result.totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return result;
    }

    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
}
