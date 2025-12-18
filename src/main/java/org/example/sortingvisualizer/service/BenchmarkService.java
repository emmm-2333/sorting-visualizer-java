package org.example.sortingvisualizer.service;

import java.util.ArrayList;
import java.util.List;

import org.example.sortingvisualizer.algorithm.AlgorithmRegistry;
import org.example.sortingvisualizer.algorithm.Sorter;
import org.example.sortingvisualizer.model.AlgorithmInfo;
import org.example.sortingvisualizer.model.PerformanceMetrics;
import org.example.sortingvisualizer.util.DataGenerator;

import javafx.concurrent.Task;

/**
 * 性能基准测试服务类
 * 负责对各种排序算法进行性能测试，包括执行时间和内存使用情况的测量
 */
public class BenchmarkService {

    /**
     * 创建基准测试任务
     * 
     * @param size 数据规模
     * @param dataType 数据类型（如"有序数据"、"递序数据"等）
     * @param algorithmsToRun 要测试的算法名称列表
     * @return 返回一个Task对象，执行后返回性能测试结果列表
     */
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
                    // 等待垃圾回收完成，以确保内存测量的准确性
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}

                    // 获取排序前的内存使用量
                    long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    // 获取排序开始时间
                    long startTime = System.nanoTime();

                    // 执行排序算法（不使用监听器，因为我们只关心性能而非可视化）
                    sorter.sort(arrayCopy, null);

                    // 获取排序结束时间
                    long endTime = System.nanoTime();
                    // 获取排序后的内存使用量
                    long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                    // 计算内存使用量（确保不为负数）
                    long memoryUsed = Math.max(0, endMem - startMem);
                    // 计算执行时间
                    long timeElapsed = endTime - startTime;

                    // 将性能测试结果添加到结果列表中
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

    /**
     * 根据指定的类型和大小生成测试数据
     * 
     * @param size 数据规模
     * @param type 数据类型
     * @return 生成的整数数组
     */
    private int[] generateData(int size, String type) {
        return switch (type) {
            case "有序数据" -> DataGenerator.generateSortedData(size);     // 生成已排序的数据
            case "递序数据" -> DataGenerator.generateReversedData(size);   // 生成逆序数据
            case "部分有序" -> DataGenerator.generateNearlySortedData(size); // 生成部分有序数据
            default -> DataGenerator.generateLinearShuffledData(size);      // 默认生成随机打乱的线性数据
        };
    }
}

