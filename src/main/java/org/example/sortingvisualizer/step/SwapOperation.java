package org.example.sortingvisualizer.step;

import java.util.Objects;

public final class SwapOperation implements SortOperation {

    private final int index1;
    private final int index2;

    public SwapOperation(int index1, int index2) {
        this.index1 = index1;
        this.index2 = index2;
    }

    @Override
    public SortOperationType type() {
        return SortOperationType.SWAP;
    }

    @Override
    public int index1() {
        return index1;
    }

    @Override
    public int index2() {
        return index2;
    }

    @Override
    public void apply(int[] array) {
        Objects.requireNonNull(array, "array");
        if (index1 < 0 || index1 >= array.length || index2 < 0 || index2 >= array.length) {
            return;
        }
        int t = array[index1];
        array[index1] = array[index2];
        array[index2] = t;
    }

    @Override
    public void undo(int[] array) {
        // swap 的逆操作仍然是 swap
        apply(array);
    }

    @Override
    public String description(int[] arrayBeforeApply) {
        if (arrayBeforeApply == null) {
            return "交换: [" + index1 + "] <-> [" + index2 + "]";
        }
        int v1 = (index1 >= 0 && index1 < arrayBeforeApply.length) ? arrayBeforeApply[index1] : Integer.MIN_VALUE;
        int v2 = (index2 >= 0 && index2 < arrayBeforeApply.length) ? arrayBeforeApply[index2] : Integer.MIN_VALUE;
        if (v1 == Integer.MIN_VALUE || v2 == Integer.MIN_VALUE) {
            return "交换: [" + index1 + "] <-> [" + index2 + "]";
        }
        return "交换: a[" + index1 + "]=" + v1 + " 与 a[" + index2 + "]=" + v2;
    }
}
