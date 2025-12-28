package com.cex.exchange.demo.middleware.es;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BufferedSearchIndex 核心类。
 */
public class BufferedSearchIndex {
    private final InMemorySearchIndex liveIndex = new InMemorySearchIndex();
    private final Map<String, String> buffer = new LinkedHashMap<>();

    public void index(String documentId, String text) {
        buffer.put(documentId, text);
    }

    public void refresh() {
        for (Map.Entry<String, String> entry : buffer.entrySet()) {
            liveIndex.index(entry.getKey(), entry.getValue());
        }
        buffer.clear();
    }

    public List<String> search(String term) {
        return liveIndex.search(term);
    }

    public int pendingCount() {
        return buffer.size();
    }
}
