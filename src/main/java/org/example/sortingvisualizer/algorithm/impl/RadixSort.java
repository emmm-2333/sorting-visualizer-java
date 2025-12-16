package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;
import java.util.Arrays;

/**
 * 基数排序实现 (LSD)
 */
public class RadixSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        int n = array.length;
        if (n == 0) return;

        // 寻找最大值以确定位数
        int max = array[0];
        for (int i = 1; i < n; i++) {
            if (listener != null) listener.onCompare(i, 0);
            if (array[i] > max) max = array[i];
        }

        // 对每一位进行计数排序
        for (int exp = 1; max / exp > 0; exp *= 10) {
            countSort(array, n, exp, listener);
        }
    }

    private void countSort(int[] array, int n, int exp, SortStepListener listener) {
        int[] output = new int[n];
        int[] count = new int[10];
        Arrays.fill(count, 0);

        // 统计频率
        for (int i = 0; i < n; i++) {
            count[(array[i] / exp) % 10]++;
            if (listener != null) listener.onCompare(i, i); // 视觉反馈
        }

        // 累加
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }

        // 构建输出
        for (int i = n - 1; i >= 0; i--) {
            output[count[(array[i] / exp) % 10] - 1] = array[i];
            count[(array[i] / exp) % 10]--;
        }

        // 复制回原数组
        for (int i = 0; i < n; i++) {
            array[i] = output[i];
            if (listener != null) listener.onSet(i, array[i]);
        }
    }

    @Override
    public String getName() {
        return "Radix Sort";
    }
}

