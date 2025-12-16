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
import org.example.sortingvisualizer.algorithm.impl.InsertionSort;
import org.example.sortingvisualizer.algorithm.impl.SelectionSort;
import org.example.sortingvisualizer.algorithm.impl.CountingSort;
import org.example.sortingvisualizer.algorithm.impl.BucketSort;
import org.example.sortingvisualizer.algorithm.impl.RadixSort;
import org.example.sortingvisualizer.algorithm.impl.BogoSort;
import org.example.sortingvisualizer.algorithm.impl.SleepSort;
import org.example.sortingvisualizer.algorithm.impl.BeadSort;
import org.example.sortingvisualizer.util.DataGenerator;
import org.example.sortingvisualizer.view.VisualizerPane;
import javafx.scene.paint.Color;
import org.example.sortingvisualizer.view.VisualizerPane;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private ComboBox<String> algorithmComboBox;

    @FXML
    private TextField dataSizeField;

    @FXML
    private ComboBox<String> dataTypeComboBox;

    @FXML
    private Button generateButton;

    @FXML
    private Button sortButton;

    @FXML
    private Button benchmarkButton;

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
        algorithmComboBox.getItems().addAll(
            "冒泡排序", "快速排序", "归并排序", "堆排序", "插入排序", "选择排序",
            "计数排序", "桶排序", "基数排序", "猴子排序", "睡眠排序", "珠排序"
        );
        algorithmComboBox.getSelectionModel().selectFirst();

        dataTypeComboBox.getItems().addAll("随机数据", "有序数据", "递序数据", "部分有序");
        dataTypeComboBox.getSelectionModel().selectFirst();

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
        // 切换回可视化面板（如果之前在图表模式）
        if (rootPane.getCenter() != visualizerPane) {
            rootPane.setCenter(visualizerPane);
        }

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

        String type = dataTypeComboBox.getValue();
        if (type == null) type = "随机数据";

        switch (type) {
            case "随机数据" -> currentArray = DataGenerator.generateLinearShuffledData(size);
            case "有序数据" -> currentArray = DataGenerator.generateSortedData(size);
            case "递序数据" -> currentArray = DataGenerator.generateReversedData(size);
            case "部分有序" -> currentArray = DataGenerator.generateNearlySortedData(size);
            default -> currentArray = DataGenerator.generateLinearShuffledData(size);
        }

        visualizerPane.setArray(currentArray);
        statusLabel.setText("数据已生成 (" + type + ")，准备排序。");
    }

    @FXML
    private void onSort() {
        if (currentArray == null) return;
        // 切换回可视化面板
        if (rootPane.getCenter() != visualizerPane) {
            rootPane.setCenter(visualizerPane);
        }

        String algoName = algorithmComboBox.getValue();
        currentSorter = getSorterByName(algoName);

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

    private Sorter getSorterByName(String name) {
        return switch (name) {
            case "冒泡排序" -> new BubbleSort();
            case "快速排序" -> new QuickSort();
            case "归并排序" -> new MergeSort();
            case "堆排序" -> new HeapSort();
            case "插入排序" -> new InsertionSort();
            case "选择排序" -> new SelectionSort();
            case "计数排序" -> new CountingSort();
            case "桶排序" -> new BucketSort();
            case "基数排序" -> new RadixSort();
            case "猴子排序" -> new BogoSort();
            case "睡眠排序" -> new SleepSort();
            case "珠排序" -> new BeadSort();
            default -> new BubbleSort();
        };
    }

    @FXML
    private void onBenchmark() {
        int size = 500; // 默认基准测试大小
        try {
            size = Integer.parseInt(dataSizeField.getText());
        } catch (NumberFormatException e) {
            size = 500;
        }

        final int benchmarkSize = size;
        final String dataType = dataTypeComboBox.getValue();

        statusLabel.setText("正在进行性能比较...");
        setControlsDisabled(true);

        Task<List<XYChart.Data<String, Number>>> benchmarkTask = new Task<>() {
            @Override
            protected List<XYChart.Data<String, Number>> call() throws Exception {
                List<XYChart.Data<String, Number>> results = new ArrayList<>();
                String[] algos = {
                    "冒泡排序", "快速排序", "归并排序", "堆排序", "插入排序", "选择排序",
                    "计数排序", "桶排序", "基数排序", "珠排序"
                    // 排除猴子排序和睡眠排序，因为它们太慢或不稳定
                };

                // 预生成一份数据，保证所有算法排序的是同一组数据
                int[] baseArray;
                switch (dataType) {
                    case "有序数据" -> baseArray = DataGenerator.generateSortedData(benchmarkSize);
                    case "递序数据" -> baseArray = DataGenerator.generateReversedData(benchmarkSize);
                    case "部分有序" -> baseArray = DataGenerator.generateNearlySortedData(benchmarkSize);
                    default -> baseArray = DataGenerator.generateLinearShuffledData(benchmarkSize);
                }

                for (String algoName : algos) {
                    Sorter sorter = getSorterByName(algoName);
                    int[] arrayCopy = baseArray.clone();

                    long startTime = System.nanoTime();
                    // 传入 null listener 表示不进行动画回调，纯跑算法
                    sorter.sort(arrayCopy, null);
                    long endTime = System.nanoTime();

                    double durationMs = (endTime - startTime) / 1_000_000.0;
                    results.add(new XYChart.Data<>(algoName, durationMs));
                }
                return results;
            }

            @Override
            protected void succeeded() {
                setControlsDisabled(false);
                statusLabel.setText("性能比较完成！");
                showBenchmarkChart(getValue(), benchmarkSize, dataType);
            }

            @Override
            protected void failed() {
                setControlsDisabled(false);
                statusLabel.setText("性能比较失败: " + getException().getMessage());
                getException().printStackTrace();
            }
        };

        new Thread(benchmarkTask).start();
    }

    private void showBenchmarkChart(List<XYChart.Data<String, Number>> data, int size, String type) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("排序算法");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("耗时 (毫秒)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("性能比较 - 数据量: " + size + " (" + type + ")");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().addAll(data);

        barChart.getData().add(series);

        // 切换中心视图为图表
        rootPane.setCenter(barChart);
    }

    private void setControlsDisabled(boolean disabled) {
        generateButton.setDisable(disabled);
        sortButton.setDisable(disabled);
        benchmarkButton.setDisable(disabled);
        algorithmComboBox.setDisable(disabled);
        dataSizeField.setDisable(disabled);
        dataTypeComboBox.setDisable(disabled);
    }
}
