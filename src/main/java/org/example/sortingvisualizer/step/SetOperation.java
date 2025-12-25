package org.example.sortingvisualizer.step;

import java.util.Objects;

public final class SetOperation implements SortOperation {

    private final int index;
    private final int oldValue;
    private final int newValue;

    public SetOperation(int index, int oldValue, int newValue) {
        this.index = index;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public SortOperationType type() {
        return SortOperationType.SET;
    }

    @Override
    public int index1() {
        return index;
    }

    @Override
    public int index2() {
        return index;
    }

    @Override
    public void apply(int[] array) {
        Objects.requireNonNull(array, "array");
        if (index < 0 || index >= array.length) {
            return;
        }
        array[index] = newValue;
    }

    @Override
    public void undo(int[] array) {
        Objects.requireNonNull(array, "array");
        if (index < 0 || index >= array.length) {
            return;
        }
        array[index] = oldValue;
    }

    @Override
    public String description(int[] arrayBeforeApply) {
        return "设置: a[" + index + "] " + oldValue + " -> " + newValue;
    }
}
