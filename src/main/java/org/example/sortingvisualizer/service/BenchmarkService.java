package org.example.sortingvisualizer.service;

import javafx.concurrent.Task;
import org.example.sortingvisualizer.algorithm.AlgorithmRegistry;
import org.example.sortingvisualizer.algorithm.Sorter;
import org.example.sortingvisualizer.model.AlgorithmInfo;
import org.example.sortingvisualizer.model.PerformanceMetrics;
import org.example.sortingvisualizer.util.DataGenerator;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkService {

    public Task<List<PerformanceMetrics>> createBenchmarkTask(int size, String dataType, List<String> algorithmsToRun) {
        return new Task<>() {
            @Override
            protected List<PerformanceMetrics> call() throws Exception {
                List<PerformanceMetrics> results = new ArrayList<>();

                // 预生成数据
                int[] baseArray = generateData(size, dataType);

                for (String algoName : algorithmsToRun) {
                    Sorter sorter = AlgorithmRegistry.getSorter(algoName);
                    if (sorter == null) continue;

                    AlgorithmInfo info = AlgorithmRegistry.getInfo(algoName);
                    int[] arrayCopy = baseArray.clone();

                    // 强制 GC 以获得较准确的内存初始值
                    System.gc();
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}

                    long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    long startTime = System.nanoTime();

                    sorter.sort(arrayCopy, null);

                    long endTime = System.nanoTime();
                    long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                    long memoryUsed = Math.max(0, endMem - startMem);
                    long timeElapsed = endTime - startTime;

                    results.add(new PerformanceMetrics(
                        algoName,
                        dataType,
                        size,
                        timeElapsed,
                        memoryUsed,
                        info
                    ));
                }
                return results;
            }
        };
    }

    private int[] generateData(int size, String type) {
        return switch (type) {
            case "有序数据" -> DataGenerator.generateSortedData(size);
            case "递序数据" -> DataGenerator.generateReversedData(size);
            case "部分有序" -> DataGenerator.generateNearlySortedData(size);
            default -> DataGenerator.generateLinearShuffledData(size);
        };
    }
}

