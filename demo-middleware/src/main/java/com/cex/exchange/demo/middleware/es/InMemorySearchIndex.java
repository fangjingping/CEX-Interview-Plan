package com.cex.exchange.demo.middleware.es;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * InMemorySearchIndex 核心类。
 */
public class InMemorySearchIndex {
    private final Map<String, Set<String>> invertedIndex = new HashMap<>();

    public void index(String documentId, String text) {
        if (documentId == null || documentId.isBlank()) {
            throw new IllegalArgumentException("documentId must not be blank");
        }
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        for (String token : tokenize(text)) {
            invertedIndex.computeIfAbsent(token, value -> new HashSet<>()).add(documentId);
        }
    }

    public List<String> search(String term) {
        if (term == null || term.isBlank()) {
            throw new IllegalArgumentException("term must not be blank");
        }
        Set<String> result = invertedIndex.getOrDefault(term.toLowerCase(Locale.ROOT), Set.of());
        return new ArrayList<>(result);
    }

    private List<String> tokenize(String text) {
        String[] raw = text.toLowerCase(Locale.ROOT).split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String value : raw) {
            if (!value.isBlank()) {
                tokens.add(value);
            }
        }
        return tokens;
    }
}
