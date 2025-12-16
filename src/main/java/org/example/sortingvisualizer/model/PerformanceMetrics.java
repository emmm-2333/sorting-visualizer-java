package org.example.sortingvisualizer.model;

/**
 * 性能指标记录类 (Java 16+ Record)
 * 用于存储单次排序的性能数据
 */
public record PerformanceMetrics(
    String algorithmName,
    String datasetType,
    int dataSize,
    long timeElapsedNanos,
    long comparisons, // 预留：比较次数
    long swaps        // 预留：交换次数
) {
    public double getTimeElapsedMillis() {
        return timeElapsedNanos / 1_000_000.0;
    }
}

