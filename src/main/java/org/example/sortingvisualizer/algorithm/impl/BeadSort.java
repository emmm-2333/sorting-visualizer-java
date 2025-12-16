package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;

/**
 * 珠排序 (Bead Sort / Gravity Sort)
 * 模拟珠子在算盘上下落
 * 注意：仅适用于正整数
 */
public class BeadSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        int n = array.length;
        int max = array[0];
        for (int i = 1; i < n; i++) {
            if (array[i] > max) max = array[i];
        }

        // 模拟算盘网格
        byte[][] grid = new byte[n][max];

        // 初始化珠子
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < array[i]; j++) {
                grid[i][j] = 1;
            }
        }

        // 珠子下落
        for (int j = 0; j < max; j++) {
            // 统计每一列有多少个珠子
            int sum = 0;
            for (int i = 0; i < n; i++) {
                sum += grid[i][j];
                grid[i][j] = 0; // 先清空
            }

            // 珠子落到底部
            for (int i = n - 1; i >= n - sum; i--) {
                grid[i][j] = 1;
            }
        }

        // 将珠子转换回数字并更新数组
        for (int i = 0; i < n; i++) {
            int val = 0;
            for (int j = 0; j < max; j++) {
                val += grid[i][j];
            }
            array[i] = val;
            if (listener != null) listener.onSet(i, val);
        }
    }

    @Override
    public String getName() {
        return "Bead Sort";
    }
}

