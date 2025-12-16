package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;

/**
 * 插入排序实现
 */
public class InsertionSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        int n = array.length;
        for (int i = 1; i < n; ++i) {
            int key = array[i];
            int j = i - 1;

            while (j >= 0) {
                if (listener != null) listener.onCompare(i, j); // 高亮当前比较
                if (array[j] > key) {
                    array[j + 1] = array[j];
                    if (listener != null) listener.onSet(j + 1, array[j]);
                    j = j - 1;
                } else {
                    break;
                }
            }
            array[j + 1] = key;
            if (listener != null) listener.onSet(j + 1, key);
        }
    }

    @Override
    public String getName() {
        return "Insertion Sort";
    }
}

