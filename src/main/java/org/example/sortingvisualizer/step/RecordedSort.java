package org.example.sortingvisualizer.step;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RecordedSort {

    private final String algorithmName;
    private final int[] initialArray;
    private final List<SortOperation> operations;

    public RecordedSort(String algorithmName, int[] initialArray, List<SortOperation> operations) {
        // 这是“录制完成后的结果对象”：算法名 + 初始数组 + 操作序列
        this.algorithmName = algorithmName;
        // 防御性拷贝：避免外部持有数组引用后修改，导致回放基准被污染
        this.initialArray = (initialArray == null) ? new int[0] : initialArray.clone();
        // 不可变封装：operations 内部不允许被增删改，保证回放过程可重复
        this.operations = Collections.unmodifiableList(Objects.requireNonNull(operations, "operations"));
    }

    public String algorithmName() {
        // 用于 UI 展示/Benchmark 标签
        return algorithmName;
    }

    public int[] initialArray() {
        // 再次 clone：调用方拿到的是副本，不能影响内部状态
        return initialArray.clone();
    }

    public List<SortOperation> operations() {
        // operations 本身是 unmodifiableList，可直接返回
        return operations;
    }
}
