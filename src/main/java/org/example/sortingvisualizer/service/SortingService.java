package org.example.sortingvisualizer.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import org.example.sortingvisualizer.algorithm.AlgorithmRegistry;
import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;
import org.example.sortingvisualizer.view.VisualizerPane;

import java.util.function.LongSupplier;

public class SortingService {

    public Task<Void> createSortTask(String algorithmName, int[] data, VisualizerPane visualizerPane, LongSupplier delaySupplier) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                Sorter sorter = AlgorithmRegistry.getSorter(algorithmName);
                if (sorter == null) return null;

                int[] arrayToSort = data.clone();

                sorter.sort(arrayToSort, new SortStepListener() {
                    @Override
                    public void onCompare(int index1, int index2) {
                        Platform.runLater(() -> visualizerPane.highlight(index1, index2, Color.RED));
                        sleep();
                    }

                    @Override
                    public void onSwap(int index1, int index2) {
                        Platform.runLater(() -> {
                            visualizerPane.updateArray(arrayToSort);
                            visualizerPane.highlight(index1, index2, Color.GREEN);
                        });
                        sleep();
                    }

                    @Override
                    public void onSet(int index, int value) {
                        Platform.runLater(() -> {
                            visualizerPane.updateArray(arrayToSort);
                            visualizerPane.highlight(index, index, Color.GREEN);
                        });
                        sleep();
                    }

                    private void sleep() {
                        try {
                            Thread.sleep(delaySupplier.getAsLong());
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                });

                Platform.runLater(() -> visualizerPane.updateArray(arrayToSort));
                return null;
            }
        };
    }
}

