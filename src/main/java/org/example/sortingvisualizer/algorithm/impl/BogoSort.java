package org.example.sortingvisualizer.algorithm.impl;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;
import java.util.Random;

/**
 * 猴子排序 (Bogo Sort)
 * 随机打乱数组直到有序
 */
public class BogoSort implements Sorter {

    private final Random random = new Random();

    @Override
    public void sort(int[] array, SortStepListener listener) {
        while (!isSorted(array, listener)) {
            shuffle(array, listener);
        }
    }

    private boolean isSorted(int[] array, SortStepListener listener) {
        for (int i = 0; i < array.length - 1; i++) {
            if (listener != null) listener.onCompare(i, i + 1);
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }

    private void shuffle(int[] array, SortStepListener listener) {
        for (int i = 0; i < array.length; i++) {
            int index = random.nextInt(array.length);
            int temp = array[i];
            array[i] = array[index];
            array[index] = temp;
            if (listener != null) listener.onSwap(i, index);
        }
    }

    @Override
    public String getName() {
        return "Bogo Sort";
    }
}

