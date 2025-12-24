package org.example.sortingvisualizer.service;

import java.util.concurrent.atomic.AtomicLong;
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

    /**
     * 暂停标志，使用volatile关键字确保多线程环境下的可见性
     * true表示排序暂停，false表示正常运行
     */
    private volatile boolean paused;

    /** 协作式取消标志：由 Controller 触发，用于让排序线程尽快退出。 */
    private volatile boolean cancelRequested;

    /** 运行令牌：用于屏蔽取消/新任务后旧任务残留的 runLater 更新。 */
    private final AtomicLong runToken = new AtomicLong();
    
    /**
     * 暂停同步锁对象，用于线程间同步
     * 通过该对象实现等待/通知机制
     */
    private final Object pauseLock = new Object();

    private static final class SortCancelledException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

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
                // 新任务开始：重置取消标志并生成新的运行令牌
                cancelRequested = false;
                long myToken = runToken.incrementAndGet();
                resume(); // 确保每次开始排序时处于运行状态

                // 根据算法名称获取对应的排序器
                Sorter sorter = AlgorithmRegistry.getSorter(algorithmName);
                // 如果找不到对应算法，则直接返回
                if (sorter == null) return null;

                // 克隆原始数据，避免修改原数组
                int[] arrayToSort = data.clone();

                // 执行排序操作，并传入监听器以监控排序过程
                try {
                    sorter.sort(arrayToSort, new SortStepListener() {
                    /**
                     * 当比较两个元素时调用
                     * @param index1 第一个元素的索引
                     * @param index2 第二个元素的索引
                     */
                    @Override
                    public void onCompare(int index1, int index2) {
                        checkCancelled();
                        // 在JavaFX应用程序线程中高亮显示正在比较的元素（红色）
                        Platform.runLater(() -> {
                            if (!isActiveRun()) return;
                            visualizerPane.highlight(index1, index2, Color.RED);
                        });
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
                        checkCancelled();
                        Platform.runLater(() -> {
                            if (!isActiveRun()) return;
                            // 更新可视化面板上的数组显示
                            visualizerPane.updateArray(arrayToSort);
                            // 高亮显示正在交换的元素（绿色）
                            visualizerPane.highlight(index1, index2, Color.GREEN);
                        });
                        // 暂停一段时间，以便用户可以看到可视化效果
                        sleep();
                    }

                    /**
                     * 当设置(修改)某个位置的值时调用
                     * @param index 元素的索引
                     * @param value 要设置的值
                     */
                    @Override
                    public void onSet(int index, int value) {
                        checkCancelled();
                        Platform.runLater(() -> {
                            if (!isActiveRun()) return;
                            // 更新可视化面板上的数组显示
                            visualizerPane.updateArray(arrayToSort);
                            // 高亮显示正在设置值的元素（蓝色）
                            visualizerPane.highlight(index, index, Color.BLUE);
                        });
                        // 暂停一段时间，以便用户可以看到可视化效果
                        sleep();
                    }

                    /**
                     * 暂停方法，根据delaySupplier提供的延迟时间暂停线程
                     */

                    private void sleep() {
                        try {
                            checkCancelled();
                            // 检查是否处于暂停状态，如果是则等待直到恢复
                            waitIfPaused();
                            checkCancelled();
                            // 根据delaySupplier提供的延迟时间进行休眠，控制排序速度
                            Thread.sleep(delaySupplier.getAsLong());
                        } catch (InterruptedException e) {
                            // 恢复线程的中断状态，确保中断信号不丢失
                            Thread.currentThread().interrupt();
                            throw new SortCancelledException();
                        }
                    }

                    /**
                     * 检查排序是否处于暂停状态，如果是则等待直到恢复
                     * 使用pauseLock对象进行同步，确保线程安全
                     * 
                     * @throws InterruptedException 当线程在等待期间被中断时抛出
                     */
                    private void waitIfPaused() throws InterruptedException {
                        synchronized (pauseLock) {
                            // 当paused标志为true时，持续等待
                            while (paused) {
                                if (cancelRequested || isCancelled() || !isActiveRun()) {
                                    throw new SortCancelledException();
                                }
                                // 释放锁并进入等待状态，直到其他线程调用notify/notifyAll
                                pauseLock.wait(50);
                            }
                        }
                    }

                    private void checkCancelled() {
                        if (cancelRequested || isCancelled() || Thread.currentThread().isInterrupted() || !isActiveRun()) {
                            throw new SortCancelledException();
                        }
                    }

                    private boolean isActiveRun() {
                        return myToken == runToken.get();
                    }
                });
                } catch (SortCancelledException ex) {
                    if (!isCancelled()) {
                        cancel();
                    }
                    return null;
                }

                // 排序完成后，在JavaFX应用程序线程中更新最终的数组显示
                if (!isCancelled() && myToken == runToken.get()) {
                    Platform.runLater(() -> {
                        if (myToken != runToken.get()) return;
                        visualizerPane.updateArray(arrayToSort);
                    });
                }
                return null;
            }
        };
    }

    /**
     * 请求取消当前排序：用于“退出排序”。
     * 会解除暂停并使旧任务的 UI 更新失效。
     */
    public void requestCancel() {
        cancelRequested = true;
        runToken.incrementAndGet();
        resume();
    }

    /**
     * 暂停排序过程
     * 将paused标志设置为true，使排序在下一步骤时进入等待状态
     */
    public void pause() {
        paused = true;
    }

    /**
     * 恢复排序过程
     * 设置paused标志为false，并通知所有等待的线程继续执行
     */
    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            // 唤醒所有在pauseLock上等待的线程
            pauseLock.notifyAll();
        }
    }

    /**
     * 检查排序是否处于暂停状态
     * 
     * @return true表示暂停中，false表示运行中
     */
    public boolean isPaused() {
        return paused;
    }
}

