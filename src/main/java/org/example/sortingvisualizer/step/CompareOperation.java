package org.example.sortingvisualizer.step;

import java.util.Objects;

public final class CompareOperation implements SortOperation {

    private final int index1;
    private final int index2;

    public CompareOperation(int index1, int index2) {
        this.index1 = index1;
        this.index2 = index2;
    }

    @Override
    public SortOperationType type() {
        return SortOperationType.COMPARE;
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
        // compare 操作不修改数组
    }

    @Override
    public void undo(int[] array) {
        Objects.requireNonNull(array, "array");
        // compare 操作不修改数组
    }

    @Override
    public String description(int[] arrayBeforeApply) {
        if (arrayBeforeApply == null) {
            return "比较: [" + index1 + "] 与 [" + index2 + "]";
        }
        int v1 = (index1 >= 0 && index1 < arrayBeforeApply.length) ? arrayBeforeApply[index1] : Integer.MIN_VALUE;
        int v2 = (index2 >= 0 && index2 < arrayBeforeApply.length) ? arrayBeforeApply[index2] : Integer.MIN_VALUE;
        if (v1 == Integer.MIN_VALUE || v2 == Integer.MIN_VALUE) {
            return "比较: [" + index1 + "] 与 [" + index2 + "]";
        }
        return "比较: a[" + index1 + "]=" + v1 + " 与 a[" + index2 + "]=" + v2;
    }
}
