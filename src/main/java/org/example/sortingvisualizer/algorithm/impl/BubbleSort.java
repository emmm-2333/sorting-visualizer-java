package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;

/**
 * 冒泡排序实现
 * 作为基础测试算法
 */
public class BubbleSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        int n = array.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                // 触发比较事件
                if (listener != null) {
                    listener.onCompare(j, j + 1);
                }

                if (array[j] > array[j + 1]) {
                    // 交换元素
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;

                    // 触发交换事件
                    if (listener != null) {
                        listener.onSwap(j, j + 1);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        // 返回算法的英文名称，用于算法标识和内部处理
        // 注意：界面显示使用的是AlgorithmRegistry中注册的中文名称"冒泡排序"
        // 该方法主要用于日志记录、调试和其他需要英文标识的场景
        return "Bubble Sort";
    }
}

