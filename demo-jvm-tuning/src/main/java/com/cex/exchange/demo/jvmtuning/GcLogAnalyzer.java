package com.cex.exchange.demo.jvmtuning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GcLogAnalyzer 核心类。
 */
public class GcLogAnalyzer {
    private static final Pattern PAUSE_PATTERN = Pattern.compile("(?i)pause[^0-9]*([0-9]+\\.?[0-9]*)ms");

    public GcPauseStats analyze(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new GcPauseStats(0, 0.0, 0.0, 0.0, 0.0);
        }
        List<Double> pauses = new ArrayList<>();
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            Matcher matcher = PAUSE_PATTERN.matcher(line);
            if (matcher.find()) {
                pauses.add(Double.parseDouble(matcher.group(1)));
            }
        }
        if (pauses.isEmpty()) {
            return new GcPauseStats(0, 0.0, 0.0, 0.0, 0.0);
        }
        Collections.sort(pauses);
        double sum = 0.0;
        for (double value : pauses) {
            sum += value;
        }
        double average = sum / pauses.size();
        double max = pauses.get(pauses.size() - 1);
        double p95 = percentile(pauses, 0.95);
        double p99 = percentile(pauses, 0.99);
        return new GcPauseStats(pauses.size(), average, max, p95, p99);
    }

    private double percentile(List<Double> values, double percentile) {
        if (values.isEmpty()) {
            return 0.0;
        }
        int index = (int) Math.ceil(percentile * values.size()) - 1;
        int safeIndex = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(safeIndex);
    }
}
