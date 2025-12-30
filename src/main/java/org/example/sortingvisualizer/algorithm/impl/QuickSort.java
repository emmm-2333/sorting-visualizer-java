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
        // 对整个数组区间 [0, n-1] 进行快速排序；listener 用于把关键步骤回调给“录制/可视化”
        quickSort(array, 0, array.length - 1, listener);
    }

    private void quickSort(int[] array, int low, int high, SortStepListener listener) {
        // 递归终止条件：当区间长度 <= 1 时已天然有序
        if (low < high) {
            // partition 会把 pivot 放到最终位置，并返回 pivot 下标
            int pi = partition(array, low, high, listener);

            // 递归处理 pivot 左侧子区间
            quickSort(array, low, pi - 1, listener);
            // 递归处理 pivot 右侧子区间
            quickSort(array, pi + 1, high, listener);
        }
    }

    private int partition(int[] array, int low, int high, SortStepListener listener) {
        // 选择最后一个元素作为 pivot（基准值）
        int pivot = array[high];
        // i 指向“已发现的小于 pivot 的最后位置”（初始为 low-1，表示尚未发现）
        int i = (low - 1); // index of smaller element

        for (int j = low; j < high; j++) {
            // j 扫描 [low, high-1] 区间的每个元素，逐个与 pivot 比较
            // 触发比较事件（供录制/可视化高亮）
            if (listener != null) {
                listener.onCompare(j, high); // 把 j 与 pivot(在 high 位置) 的比较回调出去
            }

            // 如果当前元素 < pivot，则它应该被放到“左侧小元素区”
            if (array[j] < pivot) {
                i++; // 扩大左侧小元素区边界

                // 交换 array[i] 和 array[j]：把这个小元素放到左侧区间末尾
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;

                // 触发交换事件（供录制/可视化）
                if (listener != null) {
                    listener.onSwap(i, j);
                }
            }
        }

        // 扫描结束后：i 指向最后一个“小于 pivot”的位置
        // 把 pivot 放到 i+1（pivot 的最终位置）
        int temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;

        // 触发 pivot 最终交换事件
        if (listener != null) {
            listener.onSwap(i + 1, high);
        }

        // 返回 pivot 下标：左边都 < pivot，右边都 >= pivot
        return i + 1;
    }

    @Override
    public String getName() {
        // 注意：UI 展示使用 AlgorithmRegistry 中的中文名，这里多用于传统接口/调试
        return "Quick Sort";
    }
}

