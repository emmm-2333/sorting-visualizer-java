package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 桶排序实现
 */
public class BucketSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        int n = array.length;
        if (n <= 0) return;

        // 1. 寻找最大最小值
        int maxVal = array[0];
        int minVal = array[0];
        for (int i = 1; i < n; i++) {
            if (listener != null) listener.onCompare(i, 0);
            if (array[i] > maxVal) maxVal = array[i];
            if (array[i] < minVal) minVal = array[i];
        }

        // 2. 初始化桶
        int bucketCount = (int) Math.sqrt(n);
        List<List<Integer>> buckets = new ArrayList<>(bucketCount);
        for (int i = 0; i < bucketCount; i++) {
            buckets.add(new ArrayList<>());
        }

        // 3. 分配元素到桶
        double range = (double) (maxVal - minVal + 1) / bucketCount;
        for (int i = 0; i < n; i++) {
            int bucketIndex = (int) ((array[i] - minVal) / range);
            // 防止最大值越界
            if (bucketIndex >= bucketCount) bucketIndex = bucketCount - 1;
            buckets.get(bucketIndex).add(array[i]);
            if (listener != null) listener.onCompare(i, i); // 视觉反馈
        }

        // 4. 对每个桶排序并合并
        int index = 0;
        for (List<Integer> bucket : buckets) {
            Collections.sort(bucket); // 桶内使用内置排序
            for (int val : bucket) {
                array[index] = val;
                if (listener != null) listener.onSet(index, val);
                index++;
            }
        }
    }

    @Override
    public String getName() {
        return "Bucket Sort";
    }
}

