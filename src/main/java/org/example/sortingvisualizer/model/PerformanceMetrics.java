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
    long memoryUsageBytes, // 新增：内存占用
    AlgorithmInfo algorithmInfo // 新增：算法元数据
) {
    public double getTimeElapsedMillis() {
        return timeElapsedNanos / 1_000_000.0;
    }

    public double getMemoryUsageMB() {
        return memoryUsageBytes / (1024.0 * 1024.0);
    }
}
