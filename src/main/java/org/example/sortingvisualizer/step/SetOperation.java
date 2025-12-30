package org.example.sortingvisualizer.step;

import java.util.Objects;

public final class SetOperation implements SortOperation {

    private final int index;
    private final int oldValue;
    private final int newValue;

    public SetOperation(int index, int oldValue, int newValue) {
        // index：要写入的位置；oldValue/newValue：用于支持撤销（undo）
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
        // apply：把该位置写成 newValue
        Objects.requireNonNull(array, "array");
        // 防御：下标越界时直接忽略，避免回放崩溃
        if (index < 0 || index >= array.length) {
            return;
        }
        array[index] = newValue;
    }

    @Override
    public void undo(int[] array) {
        // undo：把该位置恢复成 oldValue
        Objects.requireNonNull(array, "array");
        if (index < 0 || index >= array.length) {
            return;
        }
        array[index] = oldValue;
    }

    @Override
    public String description(int[] arrayBeforeApply) {
        // description 用于 UI 回显：写回/赋值类型操作通常更关注 old -> new
        return "设置: a[" + index + "] " + oldValue + " -> " + newValue;
    }
}
