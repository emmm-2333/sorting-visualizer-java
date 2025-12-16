package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;

/**
 * 堆排序实现
 */
public class HeapSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        int n = array.length;

        // 构建堆 (重新排列数组)
        for (int i = n / 2 - 1; i >= 0; i--)
            heapify(array, n, i, listener);

        // 逐个从堆中提取元素
        for (int i = n - 1; i > 0; i--) {
            // 将当前根节点移至末尾
            int temp = array[0];
            array[0] = array[i];
            array[i] = temp;

            if (listener != null) listener.onSwap(0, i);

            // 在缩减后的堆上调用 max heapify
            heapify(array, i, 0, listener);
        }
    }

    // 将以节点 i 为根的子树堆化，n 是堆的大小
    void heapify(int[] array, int n, int i, SortStepListener listener) {
        int largest = i; // 初始化 largest 为根
        int l = 2 * i + 1; // left = 2*i + 1
        int r = 2 * i + 2; // right = 2*i + 2

        // 如果左子节点大于根
        if (l < n) {
            if (listener != null) listener.onCompare(l, largest);
            if (array[l] > array[largest])
                largest = l;
        }

        // 如果右子节点大于目前最大的
        if (r < n) {
            if (listener != null) listener.onCompare(r, largest);
            if (array[r] > array[largest])
                largest = r;
        }

        // 如果 largest 不是根
        if (largest != i) {
            int swap = array[i];
            array[i] = array[largest];
            array[largest] = swap;

            if (listener != null) listener.onSwap(i, largest);

            // 递归地堆化受影响的子树
            heapify(array, n, largest, listener);
        }
    }

    @Override
    public String getName() {
        return "Heap Sort";
    }
}

