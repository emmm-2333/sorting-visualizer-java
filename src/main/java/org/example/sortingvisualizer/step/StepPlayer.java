package org.example.sortingvisualizer.step;

import java.util.List;
import java.util.Objects;

public final class StepPlayer {

    private final int[] workingArray;
    private final List<SortOperation> operations;

    /** 指向“下一步要执行的操作”的位置，范围 [0..operations.size()] */
    private int cursor;

    public StepPlayer(int[] initialArray, List<SortOperation> operations) {
        this.workingArray = (initialArray == null) ? new int[0] : initialArray.clone();
        this.operations = Objects.requireNonNull(operations, "operations");
        this.cursor = 0;
    }

    public int size() {
        return operations.size();
    }

    public int cursor() {
        return cursor;
    }

    public boolean hasNext() {
        return cursor < operations.size();
    }

    public boolean hasPrev() {
        return cursor > 0;
    }

    public SortOperation next() {
        if (!hasNext()) {
            return null;
        }
        SortOperation op = operations.get(cursor);
        int[] before = workingArray.clone();
        op.apply(workingArray);
        cursor++;
        return new DescribedOperation(op, before);
    }

    public SortOperation prev() {
        if (!hasPrev()) {
            return null;
        }
        cursor--;
        SortOperation op = operations.get(cursor);
        int[] before = workingArray.clone();
        op.undo(workingArray);
        return new DescribedOperation(op, before);
    }

    public int[] currentArray() {
        return workingArray.clone();
    }

    private static final class DescribedOperation implements SortOperation {
        private final SortOperation delegate;
        private final int[] arrayBeforeApply;

        private DescribedOperation(SortOperation delegate, int[] arrayBeforeApply) {
            this.delegate = delegate;
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
            return delegate.description(arrayBeforeApply);
        }
    }
}
