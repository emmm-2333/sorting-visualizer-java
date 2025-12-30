package org.example.sortingvisualizer.step;

import java.util.Objects;

public final class SwapOperation implements SortOperation {

    private final int index1;
    private final int index2;

    public SwapOperation(int index1, int index2) {
        // 记录交换涉及的两个下标
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
        // apply：执行交换
        Objects.requireNonNull(array, "array");
        // 防御性边界检查：避免录制到的下标异常导致回放崩溃
        if (index1 < 0 || index1 >= array.length || index2 < 0 || index2 >= array.length) {
            return;
        }
        int t = array[index1];
        array[index1] = array[index2];
        array[index2] = t;
    }

    @Override
    public void undo(int[] array) {
        // swap 的逆操作仍然是 swap：再次交换即可还原
        apply(array);
    }

    @Override
    public String description(int[] arrayBeforeApply) {
        // description 用于 UI 文本回显
        // 注意：这里应尽量用“执行前快照”的值，避免执行后数值已变化导致回显难理解
        if (arrayBeforeApply == null) {
            return "交换: [" + index1 + "] <-> [" + index2 + "]";
        }
        int v1 = (index1 >= 0 && index1 < arrayBeforeApply.length) ? arrayBeforeApply[index1] : Integer.MIN_VALUE;
        int v2 = (index2 >= 0 && index2 < arrayBeforeApply.length) ? arrayBeforeApply[index2] : Integer.MIN_VALUE;
        if (v1 == Integer.MIN_VALUE || v2 == Integer.MIN_VALUE) {
            // 越界时回退到仅显示下标
            return "交换: [" + index1 + "] <-> [" + index2 + "]";
        }
        // 正常情况：显示下标 + 执行前的值
        return "交换: a[" + index1 + "]=" + v1 + " 与 a[" + index2 + "]=" + v2;
    }
}
