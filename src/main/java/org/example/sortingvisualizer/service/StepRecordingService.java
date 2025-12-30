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
        // sorter 必须存在：record 的职责是“把排序过程录制成操作序列”，不负责兜底算法选择
        Objects.requireNonNull(sorter, "sorter");

        // initial：录制结果的“初始数组快照”（回放从这里开始）
        int[] initial = (data == null) ? new int[0] : data.clone();
        // working：交给算法“真实排序”的工作数组（算法会就地修改它）
        // state：shadow 状态机，仅用于推导 SetOperation 的 oldValue，以及保持录制时的“逻辑状态”
        //        注意：state 不能与 working 共用，否则录制的 swap/set 会在回放时被重复应用导致错误
        int[] working = initial.clone();
        int[] state = initial.clone();

        // ops：录制输出的“可回放操作序列”（compare/swap/set）
        // 初始容量做一个经验预估：减少扩容次数（不影响逻辑正确性）
        List<SortOperation> ops = new ArrayList<>(Math.max(16, state.length * 8));

        // 执行排序：把 listener 注入算法实现，让算法在关键点回调 compare/swap/set
        sorter.sort(working, new SortStepListener() {
            @Override
            public void onCompare(int index1, int index2) {
                // 记录一次“比较”操作（不修改数组，仅用于高亮与回显）
                ops.add(new CompareOperation(index1, index2));
            }

            @Override
            public void onSwap(int index1, int index2) {
                // 记录一次“交换”操作（可撤销：undo=再 swap 一次）
                ops.add(new SwapOperation(index1, index2));
                // 同步 shadow 状态：保证后续 onSet 能拿到正确 oldValue
                if (index1 >= 0 && index1 < state.length && index2 >= 0 && index2 < state.length) {
                    int t = state[index1];
                    state[index1] = state[index2];
                    state[index2] = t;
                }
            }

            @Override
            public void onSet(int index, int value) {
                // 对于“写回/赋值”类算法（如归并/计数等），需要记录 oldValue 才能支持撤销
                int old = (index >= 0 && index < state.length) ? state[index] : value;
                ops.add(new SetOperation(index, old, value));
                // 同步 shadow 状态，保持 state 与“录制进度”一致
                if (index >= 0 && index < state.length) {
                    state[index] = value;
                }
            }
        });

        // 返回录制结果：algorithmName 主要用于 UI 状态展示/日志；回放以 initial + ops 为准
        return new RecordedSort(algorithmName, initial, ops);
    }
}
