package org.example.sortingvisualizer.algorithm;

/**
 * 排序步骤监听器接口
 * 用于在排序算法执行过程中回调 UI 更新，实现动画效果。
 */
public interface SortStepListener {
    /**
     * 当两个元素进行比较时调用
     * @param index1 第一个元素的索引
     * @param index2 第二个元素的索引
     */
    void onCompare(int index1, int index2);

    /**
     * 当两个元素交换位置时调用
     * @param index1 第一个元素的索引
     * @param index2 第二个元素的索引
     */
    void onSwap(int index1, int index2);

    /**
     * 当某个位置的元素被设置新值时调用 (用于归并排序等非交换排序)
     * @param index 索引
     * @param value 新值
     */
    void onSet(int index, int value);
}

