package org.example.sortingvisualizer.algorithm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.example.sortingvisualizer.algorithm.impl.BeadSort;
import org.example.sortingvisualizer.algorithm.impl.BogoSort;
import org.example.sortingvisualizer.algorithm.impl.BubbleSort;
import org.example.sortingvisualizer.algorithm.impl.BucketSort;
import org.example.sortingvisualizer.algorithm.impl.CountingSort;
import org.example.sortingvisualizer.algorithm.impl.HeapSort;
import org.example.sortingvisualizer.algorithm.impl.InsertionSort;
import org.example.sortingvisualizer.algorithm.impl.MergeSort;
import org.example.sortingvisualizer.algorithm.impl.QuickSort;
import org.example.sortingvisualizer.algorithm.impl.RadixSort;
import org.example.sortingvisualizer.algorithm.impl.SelectionSort;
import org.example.sortingvisualizer.algorithm.impl.ShellSort;
import org.example.sortingvisualizer.algorithm.impl.SleepSort;
import org.example.sortingvisualizer.model.AlgorithmInfo;

/**
 * 算法注册表，管理所有排序算法及其元数据
 */
public class AlgorithmRegistry {

    private static final Map<String, Sorter> sorters = new LinkedHashMap<>();
    private static final Map<String, AlgorithmInfo> metadata = new LinkedHashMap<>();

    static {
        register(new BubbleSort(), new AlgorithmInfo("冒泡排序", "O(n)", "O(n²)", "O(n²)", "O(1)", true));
        register(new QuickSort(), new AlgorithmInfo("快速排序", "O(n log n)", "O(n log n)", "O(n²)", "O(log n)", false));
        register(new MergeSort(), new AlgorithmInfo("归并排序", "O(n log n)", "O(n log n)", "O(n log n)", "O(n)", true));
        register(new HeapSort(), new AlgorithmInfo("堆排序", "O(n log n)", "O(n log n)", "O(n log n)", "O(1)", false));
        register(new InsertionSort(), new AlgorithmInfo("插入排序", "O(n)", "O(n²)", "O(n²)", "O(1)", true));
        register(new ShellSort(), new AlgorithmInfo("希尔排序", "O(n log n)", "O(n^1.5)", "O(n²)", "O(1)", false));
        register(new SelectionSort(), new AlgorithmInfo("选择排序", "O(n²)", "O(n²)", "O(n²)", "O(1)", false));
        register(new CountingSort(), new AlgorithmInfo("计数排序", "O(n+k)", "O(n+k)", "O(n+k)", "O(k)", true));
        register(new BucketSort(), new AlgorithmInfo("桶排序", "O(n+k)", "O(n+k)", "O(n²)", "O(n)", true));
        register(new RadixSort(), new AlgorithmInfo("基数排序", "O(nk)", "O(nk)", "O(nk)", "O(n+k)", true));
        register(new BogoSort(), new AlgorithmInfo("猴子排序", "O(n)", "O(n·n!)", "∞", "O(1)", false));
        register(new SleepSort(), new AlgorithmInfo("睡眠排序", "O(n)", "O(n)", "O(n)", "O(n)", true));
        register(new BeadSort(), new AlgorithmInfo("珠排序", "O(n)", "O(S)", "O(S)", "O(S²)", true));
    }

    private static void register(Sorter sorter, AlgorithmInfo info) {
        sorters.put(info.name(), sorter);
        metadata.put(info.name(), info);
    }

    public static Sorter getSorter(String name) {
        return sorters.get(name);
    }

    public static AlgorithmInfo getInfo(String name) {
        return metadata.get(name);
    }

    public static List<String> getAllAlgorithmNames() {
        return new ArrayList<>(sorters.keySet());
    }

    public static List<Sorter> getAllSorters() {
        return new ArrayList<>(sorters.values());
    }
}

