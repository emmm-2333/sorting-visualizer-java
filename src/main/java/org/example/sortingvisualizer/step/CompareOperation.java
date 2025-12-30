package org.example.sortingvisualizer.step;

import java.util.Objects;

public final class CompareOperation implements SortOperation {

    private final int index1;
    private final int index2;

    public CompareOperation(int index1, int index2) {
        // 记录比较涉及的两个下标（不关心先后顺序）
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
        // compare 操作本质只用于“高亮/回显”，不会修改数组内容
        Objects.requireNonNull(array, "array");
    }

    @Override
    public void undo(int[] array) {
        // compare 也无需撤销：undo 同样不改数组
        Objects.requireNonNull(array, "array");
    }

    @Override
    public String description(int[] arrayBeforeApply) {
        // description 用于 UI 文本回显：尽量显示“比较了谁”和“比较前的值”
        if (arrayBeforeApply == null) {
            // 如果没有提供执行前数组，就仅输出下标
            return "比较: [" + index1 + "] 与 [" + index2 + "]";
        }
        int v1 = (index1 >= 0 && index1 < arrayBeforeApply.length) ? arrayBeforeApply[index1] : Integer.MIN_VALUE;
        int v2 = (index2 >= 0 && index2 < arrayBeforeApply.length) ? arrayBeforeApply[index2] : Integer.MIN_VALUE;
        if (v1 == Integer.MIN_VALUE || v2 == Integer.MIN_VALUE) {
            // 下标越界时，回退到仅显示下标的描述
            return "比较: [" + index1 + "] 与 [" + index2 + "]";
        }
        // 正常情况：显示下标 + 执行前的值
        return "比较: a[" + index1 + "]=" + v1 + " 与 a[" + index2 + "]=" + v2;
    }
}
