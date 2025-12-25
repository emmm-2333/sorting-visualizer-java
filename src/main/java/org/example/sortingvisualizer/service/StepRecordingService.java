package org.example.sortingvisualizer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;
import org.example.sortingvisualizer.step.CompareOperation;
import org.example.sortingvisualizer.step.RecordedSort;
import org.example.sortingvisualizer.step.SetOperation;
import org.example.sortingvisualizer.step.SortOperation;
import org.example.sortingvisualizer.step.SwapOperation;

/**
 * 录制排序操作序列：不做 UI 更新，只把算法回调转成可回放/可撤销的操作列表。
 * 这样可以天然支持“上一步/下一步”，并保持与算法实现松耦合。
 */
public class StepRecordingService {

    public RecordedSort record(String algorithmName, Sorter sorter, int[] data) {
        Objects.requireNonNull(sorter, "sorter");

        int[] initial = (data == null) ? new int[0] : data.clone();
        // working：交给算法实际排序的数组
        // state：仅用于推导 oldValue/回放序列的状态机（不能与 working 共用，否则会重复应用 swap/set）
        int[] working = initial.clone();
        int[] state = initial.clone();

        List<SortOperation> ops = new ArrayList<>(Math.max(16, state.length * 8));

        sorter.sort(working, new SortStepListener() {
            @Override
            public void onCompare(int index1, int index2) {
                ops.add(new CompareOperation(index1, index2));
            }

            @Override
            public void onSwap(int index1, int index2) {
                ops.add(new SwapOperation(index1, index2));
                // 维护 shadow 状态，让后续 Set 能拿到正确 oldValue
                if (index1 >= 0 && index1 < state.length && index2 >= 0 && index2 < state.length) {
                    int t = state[index1];
                    state[index1] = state[index2];
                    state[index2] = t;
                }
            }

            @Override
            public void onSet(int index, int value) {
                int old = (index >= 0 && index < state.length) ? state[index] : value;
                ops.add(new SetOperation(index, old, value));
                if (index >= 0 && index < state.length) {
                    state[index] = value;
                }
            }
        });

        return new RecordedSort(algorithmName, initial, ops);
    }
}
