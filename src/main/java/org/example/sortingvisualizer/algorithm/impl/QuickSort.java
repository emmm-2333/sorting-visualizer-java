package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;

/**
 * 快速排序实现
 * 采用递归方式和标准 Partition 操作
 */
public class QuickSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        quickSort(array, 0, array.length - 1, listener);
    }

    private void quickSort(int[] array, int low, int high, SortStepListener listener) {
        if (low < high) {
            int pi = partition(array, low, high, listener);

            quickSort(array, low, pi - 1, listener);
            quickSort(array, pi + 1, high, listener);
        }
    }

    private int partition(int[] array, int low, int high, SortStepListener listener) {
        int pivot = array[high];
        int i = (low - 1); // index of smaller element

        for (int j = low; j < high; j++) {
            // 触发比较事件
            if (listener != null) {
                listener.onCompare(j, high);
            }

            if (array[j] < pivot) {
                i++;

                // 交换 array[i] 和 array[j]
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;

                // 触发交换事件
                if (listener != null) {
                    listener.onSwap(i, j);
                }
            }
        }

        // 交换 array[i+1] 和 array[high] (or pivot)
        int temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;

        if (listener != null) {
            listener.onSwap(i + 1, high);
        }

        return i + 1;
    }

    @Override
    public String getName() {
        return "Quick Sort";
    }
}

