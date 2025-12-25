package org.example.sortingvisualizer.step;

public interface SortOperation {
    SortOperationType type();

    int index1();

    int index2();

    void apply(int[] array);

    void undo(int[] array);

    String description(int[] arrayBeforeApply);
}
