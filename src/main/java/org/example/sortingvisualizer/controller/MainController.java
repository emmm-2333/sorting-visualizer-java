package org.example.sortingvisualizer.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.example.sortingvisualizer.algorithm.AlgorithmRegistry;
import org.example.sortingvisualizer.model.PerformanceMetrics;
import org.example.sortingvisualizer.service.BenchmarkService;
import org.example.sortingvisualizer.service.DataInputService;
import org.example.sortingvisualizer.service.SortingService;
import org.example.sortingvisualizer.util.DataGenerator;
import org.example.sortingvisualizer.view.VisualizerPane;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

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
    private Button pauseButton;

    @FXML
    private TextField customDataField;

    @FXML
    private Button showDataButton;

    @FXML
    private Button loadFileButton;

    @FXML
    private CheckBox showValuesCheckbox;

    @FXML
    private Slider speedSlider;

    @FXML
    private Label statusLabel;

    private VisualizerPane visualizerPane;
    private int[] currentArray;

    private final SortingService sortingService = new SortingService();
    private final BenchmarkService benchmarkService = new BenchmarkService();
    private final DataInputService dataInputService = new DataInputService();

    private Task<Void> currentSortTask;

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
        algorithmComboBox.getItems().addAll(AlgorithmRegistry.getAllAlgorithmNames());
        algorithmComboBox.getSelectionModel().selectFirst();

        dataTypeComboBox.getItems().addAll("随机数据", "有序数据", "逆序数据", "部分有序");
        dataTypeComboBox.getSelectionModel().selectFirst();

        // 速度滑块监听
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // 滑块值越大，延迟越小 (速度越快)
            // 假设滑块范围 1-100
            delay = (long) (100 - newVal.doubleValue()) + 1;
        });

        // 初始生成数据
        onGenerateData();

        // 标签显示切换
        if (showValuesCheckbox != null) {
            showValuesCheckbox.selectedProperty().addListener((obs, ov, nv) -> visualizerPane.setShowLabels(nv));
        }

        if (pauseButton != null) {
            pauseButton.setDisable(true);
        }
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
            case "逆序数据" -> currentArray = DataGenerator.generateReversedData(size);
            case "部分有序" -> currentArray = DataGenerator.generateNearlySortedData(size);
            default -> currentArray = DataGenerator.generateLinearShuffledData(size);
        }

        visualizerPane.setArray(currentArray);
        statusLabel.setText("数据已生成 (" + type + ")，准备排序。");
    }

    @FXML
    private void onShowData() {
        String input = customDataField != null ? customDataField.getText() : null;
        try {
            int[] arr = dataInputService.parseInputString(input);
            currentArray = arr;
            if (rootPane.getCenter() != visualizerPane) {
                rootPane.setCenter(visualizerPane);
            }
            visualizerPane.setArray(currentArray);
            statusLabel.setText("已显示自定义数据，数量：" + currentArray.length);
        } catch (IllegalArgumentException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            alert.setHeaderText("解析失败");
            alert.showAndWait();
        }
    }

    @FXML
    private void onLoadFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择数据文件");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("文本文件", "*.txt", "*.csv"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        Window win = rootPane.getScene() != null ? rootPane.getScene().getWindow() : null;
        File file = chooser.showOpenDialog(win);
        if (file == null) return;
        try {
            int[] arr = dataInputService.loadFromFile(file);
            currentArray = arr;
            if (rootPane.getCenter() != visualizerPane) {
                rootPane.setCenter(visualizerPane);
            }
            visualizerPane.setArray(currentArray);
            statusLabel.setText("已从文件加载数据，数量：" + currentArray.length);
        } catch (IOException | IllegalArgumentException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            alert.setHeaderText("读取失败");
            alert.showAndWait();
        }
    }

    @FXML
    private void onSort() {
        if (currentArray == null) return;
        // 切换回可视化面板
        if (rootPane.getCenter() != visualizerPane) {
            rootPane.setCenter(visualizerPane);
        }

        String algoName = algorithmComboBox.getValue();

        // 禁用按钮防止重复点击
        setControlsDisabled(true);
        statusLabel.setText("正在使用 " + algoName + " 排序...");

        Task<Void> sortTask = sortingService.createSortTask(algoName, currentArray, visualizerPane, () -> delay);
        currentSortTask = sortTask;
        if (pauseButton != null) {
            pauseButton.setDisable(false);
            pauseButton.setText("暂停");
        }

        sortTask.setOnSucceeded(e -> {
            setControlsDisabled(false);
            statusLabel.setText("排序完成！");
            if (pauseButton != null) {
                pauseButton.setDisable(true);
                pauseButton.setText("暂停");
            }
            sortingService.resume();
            currentSortTask = null;
        });

        sortTask.setOnFailed(e -> {
            setControlsDisabled(false);
            statusLabel.setText("排序失败: " + sortTask.getException().getMessage());
            if (pauseButton != null) {
                pauseButton.setDisable(true);
                pauseButton.setText("暂停");
            }
            sortingService.resume();
            currentSortTask = null;
        });

        new Thread(sortTask).start();
    }

    @FXML
    private void onPauseResume() {
        if (currentSortTask == null || !currentSortTask.isRunning()) return;

        if (sortingService.isPaused()) {
            sortingService.resume();
            if (pauseButton != null) pauseButton.setText("暂停");
            statusLabel.setText("继续排序...");
        } else {
            sortingService.pause();
            if (pauseButton != null) pauseButton.setText("继续");
            statusLabel.setText("已暂停，点击继续。");
        }
    }

    @FXML
    private void onBenchmark() {
        int size; // 默认基准测试大小
        try {
            size = Integer.parseInt(dataSizeField.getText());
        } catch (NumberFormatException e) {
            size = 500;
        }

        final int benchmarkSize = size;
        final String dataType = dataTypeComboBox.getValue();

        statusLabel.setText("正在进行性能比较...");
        setControlsDisabled(true);

        // 获取所有算法名称，并排除不适合性能测试的算法
        List<String> algos = AlgorithmRegistry.getAllAlgorithmNames().stream()
                .filter(name -> !name.equals("猴子排序") && !name.equals("睡眠排序") && !name.equals("珠排序"))
                .collect(Collectors.toList());

        Task<List<PerformanceMetrics>> benchmarkTask = benchmarkService.createBenchmarkTask(benchmarkSize, dataType, algos);

        benchmarkTask.setOnSucceeded(e -> {
            setControlsDisabled(false);
            statusLabel.setText("性能比较完成！");
            showBenchmarkResults(benchmarkTask.getValue(), benchmarkSize, dataType);
        });

        benchmarkTask.setOnFailed(e -> {
            setControlsDisabled(false);
            statusLabel.setText("性能比较失败: " + benchmarkTask.getException().getMessage());
            benchmarkTask.getException().printStackTrace();
        });

        new Thread(benchmarkTask).start();
    }

    private void showBenchmarkResults(List<PerformanceMetrics> metrics, int size, String type) {
        TabPane tabPane = new TabPane();

        // Tab 1: 时间对比图
        Tab timeTab = new Tab("时间对比", createTimeChart(metrics));
        timeTab.setClosable(false);

        // Tab 2: 内存对比图
        Tab memoryTab = new Tab("内存对比", createMemoryChart(metrics));
        memoryTab.setClosable(false);

        // Tab 3: 详细数据表
        Tab tableTab = new Tab("详细数据", createDetailTable(metrics));
        tableTab.setClosable(false);

        tabPane.getTabs().addAll(timeTab, memoryTab, tableTab);
        rootPane.setCenter(tabPane);
    }

    private BarChart<String, Number> createTimeChart(List<PerformanceMetrics> metrics) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("排序算法");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("耗时 (毫秒)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("算法耗时对比");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (PerformanceMetrics m : metrics) {
            series.getData().add(new XYChart.Data<>(m.algorithmName(), m.getTimeElapsedMillis()));
        }
        barChart.getData().add(series);
        return barChart;
    }

    private BarChart<String, Number> createMemoryChart(List<PerformanceMetrics> metrics) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("排序算法");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("内存占用 (MB)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("算法内存占用对比 (估算)");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (PerformanceMetrics m : metrics) {
            series.getData().add(new XYChart.Data<>(m.algorithmName(), m.getMemoryUsageMB()));
        }
        barChart.getData().add(series);
        return barChart;
    }

    private TableView<PerformanceMetrics> createDetailTable(List<PerformanceMetrics> metrics) {
        TableView<PerformanceMetrics> table = new TableView<>();

        TableColumn<PerformanceMetrics, String> nameCol = new TableColumn<>("算法名称");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().algorithmName()));

        TableColumn<PerformanceMetrics, Number> timeCol = new TableColumn<>("耗时 (ms)");
        timeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getTimeElapsedMillis()));

        TableColumn<PerformanceMetrics, String> complexityCol = new TableColumn<>("平均时间复杂度");
        complexityCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().algorithmInfo().averageTimeComplexity()));

        TableColumn<PerformanceMetrics, String> spaceCol = new TableColumn<>("空间复杂度");
        spaceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().algorithmInfo().spaceComplexity()));

        TableColumn<PerformanceMetrics, String> stableCol = new TableColumn<>("稳定性");
        stableCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().algorithmInfo().isStable() ? "是" : "否"));

        table.getColumns().addAll(nameCol, timeCol, complexityCol, spaceCol, stableCol);
        table.getItems().addAll(metrics);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    private void setControlsDisabled(boolean disabled) {
        generateButton.setDisable(disabled);
        sortButton.setDisable(disabled);
        benchmarkButton.setDisable(disabled);
        algorithmComboBox.setDisable(disabled);
        dataSizeField.setDisable(disabled);
        dataTypeComboBox.setDisable(disabled);
        if (showDataButton != null) showDataButton.setDisable(disabled);
        if (loadFileButton != null) loadFileButton.setDisable(disabled);
        if (customDataField != null) customDataField.setDisable(disabled);
        if (showValuesCheckbox != null) showValuesCheckbox.setDisable(disabled);
    }
}
