package org.example.sortingvisualizer.algorithm;

/**
 * 排序算法接口 (策略模式)
 * 所有具体的排序算法都必须实现此接口。
 */
public interface Sorter {
    /**
     * 执行排序
     * @param array 待排序数组
     * @param listener 排序步骤监听器 (用于动画演示)，如果为 null 则表示仅执行排序不演示
     */
    void sort(int[] array, SortStepListener listener);

    /**
     * 获取算法名称
     * @return 算法名称
     */
    String getName();
}

