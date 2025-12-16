package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 睡眠排序 (Sleep Sort)
 * 为每个元素创建一个线程，睡眠时间等于元素值，醒来后加入结果列表
 */
public class SleepSort implements Sorter {

    @Override
    public void sort(int[] array, SortStepListener listener) {
        int n = array.length;
        List<Integer> sortedList = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(n);

        for (int val : array) {
            new Thread(() -> {
                try {
                    // 睡眠时间 = 值 * 系数 (为了可视化效果，系数设大一点)
                    Thread.sleep(val * 10L);
                    sortedList.add(val);

                    // 实时更新原数组用于可视化 (非线程安全，仅作演示)
                    synchronized (array) {
                        int index = sortedList.size() - 1;
                        if (index < n) {
                            array[index] = val;
                            if (listener != null) listener.onSet(index, val);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "Sleep Sort";
    }
}

