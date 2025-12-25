package org.example.sortingvisualizer.step;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RecordedSort {

    private final String algorithmName;
    private final int[] initialArray;
    private final List<SortOperation> operations;

    public RecordedSort(String algorithmName, int[] initialArray, List<SortOperation> operations) {
        this.algorithmName = algorithmName;
        this.initialArray = (initialArray == null) ? new int[0] : initialArray.clone();
        this.operations = Collections.unmodifiableList(Objects.requireNonNull(operations, "operations"));
    }

    public String algorithmName() {
        return algorithmName;
    }

    public int[] initialArray() {
        return initialArray.clone();
    }

    public List<SortOperation> operations() {
        return operations;
    }
}
