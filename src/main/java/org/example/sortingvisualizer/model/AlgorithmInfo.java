package org.example.sortingvisualizer.model;

/**
 * 算法元数据信息
 */
public record AlgorithmInfo(
    String name,
    String bestTimeComplexity,
    String averageTimeComplexity,
    String worstTimeComplexity,
    String spaceComplexity,
    boolean isStable
) {}

