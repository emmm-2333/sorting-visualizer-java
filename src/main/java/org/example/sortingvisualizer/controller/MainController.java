package org.example.sortingvisualizer.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.example.sortingvisualizer.algorithm.SortStepListener;
import org.example.sortingvisualizer.algorithm.Sorter;
import org.example.sortingvisualizer.algorithm.impl.BubbleSort;
import org.example.sortingvisualizer.algorithm.impl.HeapSort;
import org.example.sortingvisualizer.algorithm.impl.MergeSort;
import org.example.sortingvisualizer.algorithm.impl.QuickSort;
import org.example.sortingvisualizer.util.DataGenerator;
import org.example.sortingvisualizer.view.VisualizerPane;
import javafx.scene.paint.Color;

public class MainController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private ComboBox<String> algorithmComboBox;

    @FXML
    private TextField dataSizeField; // Changed from ComboBox to TextField

    @FXML
    private Button generateButton;

    @FXML
    private Button sortButton;

    @FXML
    private Slider speedSlider;

    @FXML
    private Label statusLabel;

    private VisualizerPane visualizerPane;
    private int[] currentArray;
    private Sorter currentSorter;

    // 动画延迟 (毫秒)
    private long delay = 50;

    @FXML
    public void initialize() {
        // 初始化可视化面板
        visualizerPane = new VisualizerPane();
        rootPane.setCenter(visualizerPane);

        // 监听面板大小变化以重绘
        visualizerPane.widthProperty().addListener((obs, oldVal, newVal) -> visualizerPane.setArray(currentArray));
        visualizerPane.heightProperty().addListener((obs, oldVal, newVal) -> visualizerPane.setArray(currentArray));

        // 初始化下拉框 (中文)
        algorithmComboBox.getItems().addAll("冒泡排序", "快速排序", "归并排序", "堆排序");
        algorithmComboBox.getSelectionModel().selectFirst();

        // 速度滑块监听
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // 滑块值越大，延迟越小 (速度越快)
            // 假设滑块范围 1-100
            delay = (long) (100 - newVal.doubleValue()) + 1;
        });

        // 初始生成数据
        onGenerateData();
    }

    @FXML
    private void onGenerateData() {
        int size = 50;
        try {
            size = Integer.parseInt(dataSizeField.getText());
            if (size > 500) {
                size = 500;
                dataSizeField.setText("500");
            } else if (size < 5) {
                size = 5;
                dataSizeField.setText("5");
            }
        } catch (NumberFormatException e) {
            dataSizeField.setText("50");
        }

        // 使用线性洗牌数据，保证排序后形成完美的梯度三角形
        currentArray = DataGenerator.generateLinearShuffledData(size);
        visualizerPane.setArray(currentArray);
        statusLabel.setText("数据已生成，准备排序。");
    }

    @FXML
    private void onSort() {
        if (currentArray == null) return;

        String algoName = algorithmComboBox.getValue();
        currentSorter = switch (algoName) {
            case "冒泡排序" -> new BubbleSort();
            case "快速排序" -> new QuickSort();
            case "归并排序" -> new MergeSort();
            case "堆排序" -> new HeapSort();
            default -> new BubbleSort();
        };

        // 禁用按钮防止重复点击
        setControlsDisabled(true);
        statusLabel.setText("正在使用 " + algoName + " 排序...");

        // 在后台线程运行排序，避免阻塞 UI
        Task<Void> sortTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 复制一份数据进行排序，不破坏原始数据生成逻辑
                int[] arrayToSort = currentArray.clone();

                currentSorter.sort(arrayToSort, new SortStepListener() {
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
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                });

                // 排序完成，最后刷新一次
                Platform.runLater(() -> visualizerPane.updateArray(arrayToSort));

                return null;
            }

            @Override
            protected void succeeded() {
                setControlsDisabled(false);
                statusLabel.setText("排序完成！");
            }

            @Override
            protected void failed() {
                setControlsDisabled(false);
                statusLabel.setText("排序失败: " + getException().getMessage());
            }
        };

        new Thread(sortTask).start();
    }

    private void setControlsDisabled(boolean disabled) {
        generateButton.setDisable(disabled);
        sortButton.setDisable(disabled);
        algorithmComboBox.setDisable(disabled);
        dataSizeField.setDisable(disabled);
    }
}
