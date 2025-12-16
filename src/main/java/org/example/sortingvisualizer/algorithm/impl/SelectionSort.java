package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;

/**
 * 选择排序实现
 */
public class SelectionSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        int n = array.length;

        for (int i = 0; i < n - 1; i++) {
            int min_idx = i;
            for (int j = i + 1; j < n; j++) {
                if (listener != null) listener.onCompare(j, min_idx);
                if (array[j] < array[min_idx])
                    min_idx = j;
            }

            int temp = array[min_idx];
            array[min_idx] = array[i];
            array[i] = temp;

            if (listener != null) listener.onSwap(min_idx, i);
        }
    }

    @Override
    public String getName() {
        return "Selection Sort";
    }
}

