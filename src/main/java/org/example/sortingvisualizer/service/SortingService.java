package org.example.sortingvisualizer.service;

import java.util.function.LongSupplier;

import org.example.sortingvisualizer.algorithm.AlgorithmRegistry;
import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;
import org.example.sortingvisualizer.view.VisualizerPane;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;

/**
 * 排序服务类，负责创建和管理排序任务
 */
public class SortingService {

    private volatile boolean paused;
    private final Object pauseLock = new Object();

    /**
     * 创建一个排序任务
     * 
     * @param algorithmName   算法名称
     * @param data            待排序的数据数组
     * @param visualizerPane  可视化面板，用于显示排序过程
     * @param delaySupplier   延迟供应器，控制排序步骤之间的延迟
     * @return 返回一个JavaFX Task对象，可以在后台线程中执行排序
     */
    public Task<Void> createSortTask(String algorithmName, int[] data, VisualizerPane visualizerPane, LongSupplier delaySupplier) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                resume(); // 确保每次开始排序时处于运行状态
                // 根据算法名称获取对应的排序器
                Sorter sorter = AlgorithmRegistry.getSorter(algorithmName);
                // 如果找不到对应算法，则直接返回
                if (sorter == null) return null;

                // 克隆原始数据，避免修改原数组
                int[] arrayToSort = data.clone();

                // 执行排序操作，并传入监听器以监控排序过程
                sorter.sort(arrayToSort, new SortStepListener() {
                    /**
                     * 当比较两个元素时调用
                     * @param index1 第一个元素的索引
                     * @param index2 第二个元素的索引
                     */
                    @Override
                    public void onCompare(int index1, int index2) {
                        // 在JavaFX应用程序线程中高亮显示正在比较的元素（红色）
                        Platform.runLater(() -> visualizerPane.highlight(index1, index2, Color.RED));
                        // 暂停一段时间，以便用户可以看到可视化效果
                        sleep();
                    }

                    /**
                     * 当交换两个元素时调用
                     * @param index1 第一个元素的索引
                     * @param index2 第二个元素的索引
                     */
                    @Override
                    public void onSwap(int index1, int index2) {
                        Platform.runLater(() -> {
                            // 更新可视化面板上的数组显示
                            visualizerPane.updateArray(arrayToSort);
                            // 高亮显示正在交换的元素（绿色）
                            visualizerPane.highlight(index1, index2, Color.GREEN);
                        });
                        // 暂停一段时间，以便用户可以看到可视化效果
                        sleep();
                    }

                    /**
                     * 当设置某个位置的值时调用
                     * @param index 元素的索引
     * @param value 要设置的值
                     */
                    @Override
                    public void onSet(int index, int value) {
                        Platform.runLater(() -> {
                            // 更新可视化面板上的数组显示
                            visualizerPane.updateArray(arrayToSort);
                            // 高亮显示正在设置值的元素（绿色）
                            visualizerPane.highlight(index, index, Color.GREEN);
                        });
                        // 暂停一段时间，以便用户可以看到可视化效果
                        sleep();
                    }

                    /**
                     * 暂停方法，根据delaySupplier提供的延迟时间暂停线程
                     */
                    private void sleep() {
                        try {
                            waitIfPaused();
                            Thread.sleep(delaySupplier.getAsLong());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    private void waitIfPaused() throws InterruptedException {
                        synchronized (pauseLock) {
                            while (paused) {
                                pauseLock.wait();
                            }
                        }
                    }
                });

                // 排序完成后，在JavaFX应用程序线程中更新最终的数组显示
                Platform.runLater(() -> visualizerPane.updateArray(arrayToSort));
                return null;
            }
        };
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public boolean isPaused() {
        return paused;
    }
}

