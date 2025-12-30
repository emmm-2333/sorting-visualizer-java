package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;

/**
 * 选择排序实现
 */
public class SelectionSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        // n 为数组长度；选择排序的核心思想：每一轮把“最小值”选到前面
        int n = array.length;

        // 外层 i：当前要放置“本轮最小值”的位置（左侧 [0..i-1] 已就位）
        for (int i = 0; i < n - 1; i++) {
            // min_idx 记录当前轮找到的最小值下标
            int min_idx = i;
            // 内层 j：在未排序区间 [i+1..n-1] 中寻找最小值
            for (int j = i + 1; j < n; j++) {
                // 回调比较事件：用于录制/可视化高亮
                if (listener != null) listener.onCompare(j, min_idx);
                // 如果发现更小的元素，更新最小值位置
                if (array[j] < array[min_idx])
                    min_idx = j;
            }

            // 将本轮最小值交换到位置 i
            int temp = array[min_idx];
            array[min_idx] = array[i];
            array[i] = temp;

            // 回调交换事件：用于录制/可视化
            if (listener != null) listener.onSwap(min_idx, i);
        }
    }

    @Override
    public String getName() {
        // 注意：UI 侧展示中文名来自 AlgorithmRegistry；这里多用于接口/调试
        return "Selection Sort";
    }
}

