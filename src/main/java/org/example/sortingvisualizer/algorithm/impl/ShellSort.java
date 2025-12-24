package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;

/**
 * 希尔排序实现（Shell Sort）
 * 
 * 使用分组插入排序思想：gap 逐步缩小，直到 gap=1 退化为普通插入排序。
 * 动画回调策略：
 * - 比较：onCompare(j-gap, j)
 * - 移动/写回：onSet(index, value)
 */
public class ShellSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        int n = array.length;
        if (n <= 1) return;

        for (int gap = n / 2; gap > 0; gap /= 2) {
            // 对每个 gap 分组执行“插入排序”
            for (int i = gap; i < n; i++) {
                int temp = array[i];
                int j = i;

                while (j >= gap) {
                    if (listener != null) listener.onCompare(j - gap, j);
                    if (array[j - gap] > temp) {
                        array[j] = array[j - gap];
                        if (listener != null) listener.onSet(j, array[j]);
                        j -= gap;
                    } else {
                        break;
                    }
                }

                array[j] = temp;
                if (listener != null) listener.onSet(j, temp);
            }
        }
    }

    @Override
    public String getName() {
        return "Shell Sort";
    }
}
