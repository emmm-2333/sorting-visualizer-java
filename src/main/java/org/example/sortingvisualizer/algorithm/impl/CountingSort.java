package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;

/**
 * 计数排序实现
 */
public class CountingSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        int n = array.length;
        if (n == 0) return;

        // 1. 寻找最大值
        int max = array[0];
        int min = array[0];
        for (int i = 1; i < n; i++) {
            if (listener != null) listener.onCompare(i, 0); // 简单视觉反馈
            if (array[i] > max) max = array[i];
            if (array[i] < min) min = array[i];
        }

        int range = max - min + 1;
        int[] count = new int[range];
        int[] output = new int[n];

        // 2. 统计频率
        for (int i = 0; i < n; i++) {
            count[array[i] - min]++;
            if (listener != null) listener.onCompare(i, i); // 视觉反馈：正在读取
        }

        // 3. 累加计数
        for (int i = 1; i < range; i++) {
            count[i] += count[i - 1];
        }

        // 4. 构建输出数组
        for (int i = n - 1; i >= 0; i--) {
            output[count[array[i] - min] - 1] = array[i];
            count[array[i] - min]--;
        }

        // 5. 复制回原数组 (可视化重点)
        for (int i = 0; i < n; i++) {
            array[i] = output[i];
            if (listener != null) listener.onSet(i, array[i]);
        }
    }

    @Override
    public String getName() {
        return "Counting Sort";
    }
}

