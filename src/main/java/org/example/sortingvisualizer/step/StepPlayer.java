package org.example.sortingvisualizer.step;

import java.util.List;
import java.util.Objects;

public final class StepPlayer {

    private final int[] workingArray;
    private final List<SortOperation> operations;

    /** 指向“下一步要执行的操作”的位置，范围 [0..operations.size()] */
    private int cursor;

    public StepPlayer(int[] initialArray, List<SortOperation> operations) {
        // workingArray：回放/撤销过程中真正被 apply/undo 修改的工作数组
        this.workingArray = (initialArray == null) ? new int[0] : initialArray.clone();
        // operations：录制得到的操作序列（不可为 null）
        this.operations = Objects.requireNonNull(operations, "operations");
        // cursor=0 表示“尚未执行任何操作”；cursor=size 表示“已执行完所有操作（完成态）”
        this.cursor = 0;
    }

    public int size() {
        // 总步数（操作条数）
        return operations.size();
    }

    public int cursor() {
        // 当前游标（下一步将执行 operations[cursor]）
        return cursor;
    }

    public boolean hasNext() {
        // 只要 cursor < size，就还有下一步
        return cursor < operations.size();
    }

    public boolean hasPrev() {
        // 只要 cursor > 0，就可以撤销上一步
        return cursor > 0;
    }

    public SortOperation next() {
        // 前进一步：把“下一步操作”应用到 workingArray，并把 cursor 向前移动
        if (!hasNext()) {
            return null; // 已到末尾
        }
        SortOperation op = operations.get(cursor); // 取出本次要执行的操作
        int[] before = workingArray.clone();       // 拷贝“执行前数组”，用于生成更友好的描述文本
        op.apply(workingArray);                    // 对工作数组应用操作（可能是 swap/set；compare 不改数组）
        cursor++;                                  // 游标前进：表示这一步已执行
        return new DescribedOperation(op, before); // 返回带上下文的操作包装（用于 UI 回显）
    }

    public SortOperation prev() {
        // 后退一步：先把 cursor 回退到“上一步操作”的位置，再对 workingArray 执行 undo
        if (!hasPrev()) {
            return null; // 已在起点
        }
        cursor--;                            // 回到“上一条操作”的索引
        SortOperation op = operations.get(cursor);
        int[] before = workingArray.clone(); // 拷贝“撤销前数组”（用于描述文本）
        op.undo(workingArray);               // 撤销操作：swap 的 undo 仍是 swap；set 的 undo 写回 oldValue
        return new DescribedOperation(op, before);
    }

    public int[] currentArray() {
        // 对外返回快照：避免外部拿到内部数组引用后误修改
        return workingArray.clone();
    }

    private static final class DescribedOperation implements SortOperation {
        private final SortOperation delegate;
        private final int[] arrayBeforeApply;

        private DescribedOperation(SortOperation delegate, int[] arrayBeforeApply) {
            // delegate：真实操作（compare/swap/set）
            this.delegate = delegate;
            // arrayBeforeApply：执行本步之前的数组快照，用于生成包含具体数值的描述
            this.arrayBeforeApply = arrayBeforeApply;
        }

        @Override
        public SortOperationType type() {
            return delegate.type();
        }

        @Override
        public int index1() {
            return delegate.index1();
        }

        @Override
        public int index2() {
            return delegate.index2();
        }

        @Override
        public void apply(int[] array) {
            delegate.apply(array);
        }

        @Override
        public void undo(int[] array) {
            delegate.undo(array);
        }

        @Override
        public String description(int[] ignored) {
            // 这里忽略调用者传入的数组，统一使用“执行前快照”来生成更稳定的描述
            // 例如：交换时可以显示 a[i]=? 与 a[j]=?（执行前的值）
            return delegate.description(arrayBeforeApply);
        }
    }
}
