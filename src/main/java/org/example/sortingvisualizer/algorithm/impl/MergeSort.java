package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;

/**
 * 归并排序实现
 * 采用递归方式
 */
public class MergeSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        mergeSort(array, 0, array.length - 1, listener);
    }

    private void mergeSort(int[] array, int l, int r, SortStepListener listener) {
        if (l < r) {
            int m = l + (r - l) / 2;

            mergeSort(array, l, m, listener);
            mergeSort(array, m + 1, r, listener);

            merge(array, l, m, r, listener);
        }
    }

    private void merge(int[] array, int l, int m, int r, SortStepListener listener) {
        // 找出两个子数组的大小
        int n1 = m - l + 1;
        int n2 = r - m;

        // 创建临时数组
        int[] L = new int[n1];
        int[] R = new int[n2];

        // 复制数据到临时数组
        for (int i = 0; i < n1; ++i)
            L[i] = array[l + i];
        for (int j = 0; j < n2; ++j)
            R[j] = array[m + 1 + j];

        // 合并临时数组

        int i = 0, j = 0;
        int k = l;
        while (i < n1 && j < n2) {
            // 触发比较事件 (注意：这里比较的是临时数组的值，但在UI上我们可能想高亮原始数组对应的位置)
            // 为了简化动画逻辑，我们这里只触发 onSet，因为归并排序本质上是覆盖原数组
            if (listener != null) {
                listener.onCompare(l + i, m + 1 + j); // 近似位置高亮
            }

            if (L[i] <= R[j]) {
                array[k] = L[i];
                if (listener != null) listener.onSet(k, L[i]);
                i++;
            } else {
                array[k] = R[j];
                if (listener != null) listener.onSet(k, R[j]);
                j++;
            }
            k++;
        }

        // 复制 L[] 的剩余元素
        while (i < n1) {
            array[k] = L[i];
            if (listener != null) listener.onSet(k, L[i]);
            i++;
            k++;
        }

        // 复制 R[] 的剩余元素
        while (j < n2) {
            array[k] = R[j];
            if (listener != null) listener.onSet(k, R[j]);
            j++;
            k++;
        }
    }

    @Override
    public String getName() {
        return "Merge Sort";
    }
}

