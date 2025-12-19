package org.example.sortingvisualizer.algorithm.impl;

import java.util.ArrayList;
import java.util.List;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;

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
        int bucketCount = Math.max(1, (int) Math.sqrt(n));
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
            // 桶内使用简单插入排序
            insertionSortBucket(bucket, index, listener);
            for (int val : bucket) {
                array[index] = val;
                if (listener != null) listener.onSet(index, val);
                index++;
            }
        }
    }

    /**
     * 对单个桶执行插入排序，同时抛出比较事件。桶内的可视化索引基于其在最终数组中的起始位置。
     */
    private void insertionSortBucket(List<Integer> bucket, int baseIndex, SortStepListener listener) {
        for (int i = 1; i < bucket.size(); i++) {
            int key = bucket.get(i);
            int j = i - 1;

            while (j >= 0) {
                if (listener != null) listener.onCompare(baseIndex + j, baseIndex + j + 1);
                if (bucket.get(j) > key) {
                    bucket.set(j + 1, bucket.get(j));
                    j--;
                } else {
                    break;
                }
            }
            bucket.set(j + 1, key);
        }
    }

    @Override
    public String getName() {
        return "Bucket Sort";
    }
}

