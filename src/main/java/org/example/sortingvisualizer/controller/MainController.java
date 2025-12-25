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

/**
 * 主界面控制器（Controller）。
 * <p>
 * 角色定位：这是一个“流程编排器”，只负责 UI 交互、输入校验、任务启动与视图切换。
 * <ul>
 *   <li>排序算法实现、逐步回调与暂停协议：由 {@link SortingService} 与算法实现负责</li>
 *   <li>性能比较与统计：由 {@link BenchmarkService} 负责</li>
 *   <li>数据解析（字符串/文件）：由 {@link DataInputService} 负责</li>
 *   <li>绘制与动画表现：由 {@link VisualizerPane} 负责</li>
 * </ul>
 * Controller 只传递“算法名/数据/速度/是否显示标签”等参数，不在这里写任何排序逻辑。
 * <p>
 * 视图模式：
 * <ul>
 *   <li>可视化模式：{@code rootPane.center = visualizerPane}</li>
 *   <li>基准测试结果模式：{@code rootPane.center = TabPane(时间/内存/表格)}</li>
 * </ul>
 */
public class MainController {

    /** 根布局容器：通过切换 center 来在“可视化面板/性能结果TabPane”之间切换。 */
    @FXML
    private BorderPane rootPane;

    /** 算法下拉框：显示中文算法名（来自 {@link AlgorithmRegistry}）。 */
    @FXML
    private ComboBox<String> algorithmComboBox;

    /** 数据规模输入：用于生成数据，也用于基准测试的规模参数。 */
    @FXML
    private TextField dataSizeField;

    /** 数据类型下拉框：随机/有序/逆序/部分有序（决定 {@link DataGenerator} 的生成策略）。 */
    @FXML
    private ComboBox<String> dataTypeComboBox;

    /** 生成数据按钮：生成并立即重绘到 {@link #visualizerPane}。 */
    @FXML
    private Button generateButton;

    /** 排序按钮：启动排序动画任务（后台 Task + UI 线程更新）。 */
    @FXML
    private Button sortButton;

    /** 性能比较按钮：启动基准测试任务（不做动画、只测耗时/内存估算）。 */
    @FXML
    private Button benchmarkButton;

    /** 暂停/继续按钮：仅对“排序动画任务”有效；基准测试不使用。 */
    @FXML
    private Button pauseButton;

    /** 退出排序按钮：在排序过程中立即终止并恢复到排序前界面。 */
    @FXML
    private Button exitSortButton;

    /** 用户自定义数据输入框（例如 1,2,3 或 1 2 3），解析失败会弹框。 */
    @FXML
    private TextField customDataField;

    /** 将自定义数据展示到可视化面板。 */
    @FXML
    private Button showDataButton;

    /** 从文件加载数据并展示到可视化面板。 */
    @FXML
    private Button loadFileButton;

    /** 是否显示柱子上的数值标签（只影响绘制，不影响排序）。 */
    @FXML
    private CheckBox showValuesCheckbox;

    /** 速度滑块：值越大，动画越快（控制 delay）。 */
    @FXML
    private Slider speedSlider;

    /** 底部状态提示：用于显示当前模式、进度、错误信息。 */
    @FXML
    private Label statusLabel;

    /** 可视化绘制面板（柱状图动画都在这里画）。 */
    private VisualizerPane visualizerPane;

    /** 当前用于展示/排序的数据源。生成/加载/自定义输入会覆盖它。 */
    private int[] currentArray;

    /** 排序动画服务：创建排序 Task、处理暂停/恢复协议、在 UI 线程刷新可视化。 */
    private final SortingService sortingService = new SortingService();
    /** 性能比较服务：批量运行算法并统计耗时/内存、输出 {@link PerformanceMetrics} 列表。 */
    private final BenchmarkService benchmarkService = new BenchmarkService();
    /** 输入服务：解析自定义输入字符串、读取文件并转成 int[]。 */
    private final DataInputService dataInputService = new DataInputService();

    /** 当前排序任务引用：用于判断是否可暂停/继续，以及在结束时清理。 */
    private Task<Void> currentSortTask;

    /** 启动排序前的数组快照：用于“退出排序”后恢复。 */
    private int[] arrayBeforeSort;

    /** 标记本次取消是否由用户点击“退出排序”触发。 */
    private boolean exitRequestedByUser;

    /** 动画每一步的延迟（毫秒）。会被 speedSlider 动态更新。 */
    private long delay = 50;

    private long mapSpeedToDelay(double sliderValue) {
        // 非线性映射：滑块越大，越快（delay 越小）
        // 目标范围：最快 1ms，最慢 1000ms
        // 说明：这里返回的是“每一步动画的 sleep 时长”，并非排序耗时
        final double minDelay = 1.0;
        final double maxDelay = 1000.0;

        double min = (speedSlider != null) ? speedSlider.getMin() : 1.0;
        double max = (speedSlider != null) ? speedSlider.getMax() : 100.0;
        if (max <= min) {
            // 防御：滑块范围异常时使用一个中等默认值
            return 50;
        }

        double t = (sliderValue - min) / (max - min); // 0..1
        // 防御：避免滑块值超出范围导致映射出现负数/NaN
        if (t < 0) t = 0;
        if (t > 1) t = 1;

        // 指数曲线：低延迟区更“细腻”，高延迟区跨度更大
        // 公式：delay = minDelay * (maxDelay/minDelay)^(1-t)
        // t=0 -> maxDelay（最慢），t=1 -> minDelay（最快）
        double delayMs = minDelay * Math.pow(maxDelay / minDelay, 1.0 - t);
        long rounded = Math.round(delayMs);
        // 最终收敛到 [1,1000]，避免极端值导致 UI 卡顿或过快看不清
        if (rounded < 1) return 1;
        if (rounded > 1000) return 1000;
        return rounded;
    }

    @FXML
    public void initialize() {
        // 1) 初始化可视化面板，并作为主界面 center
        visualizerPane = new VisualizerPane();
        rootPane.setCenter(visualizerPane);

        // 2) 监听面板大小变化以触发重绘。
        //    目的：窗口缩放时，柱子宽度/高度需要重新计算
        visualizerPane.widthProperty().addListener((obs, oldVal, newVal) -> visualizerPane.setArray(currentArray));
        visualizerPane.heightProperty().addListener((obs, oldVal, newVal) -> visualizerPane.setArray(currentArray));

        // 3) 初始化下拉框（中文）。算法选项来自注册表，体现“可插拔：新增算法只需注册即可出现在 UI”。
        algorithmComboBox.getItems().addAll(AlgorithmRegistry.getAllAlgorithmNames());
        algorithmComboBox.getSelectionModel().selectFirst();

        dataTypeComboBox.getItems().addAll("随机数据", "有序数据", "逆序数据", "部分有序");
        dataTypeComboBox.getSelectionModel().selectFirst();

        // 4) 速度滑块监听：滑块值越大，延迟越小（速度越快）。
        //    非线性指数映射：最快 1ms，最慢 1000ms。
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) ->
            delay = mapSpeedToDelay(newVal.doubleValue())
        );
        // 初始化一次 delay（否则第一次排序仍可能用默认 50ms）
        delay = mapSpeedToDelay(speedSlider.getValue());

        // 5) 初始生成一组数据，保证一启动就有可视化内容。
        onGenerateData();

        // 6) 标签显示切换：只影响绘制，不影响排序逻辑。
        //    控件允许为 null（例如 FXML 复用/裁剪），因此做空判断。
        if (showValuesCheckbox != null) {
            showValuesCheckbox.selectedProperty().addListener((obs, ov, nv) -> visualizerPane.setShowLabels(nv));
        }

        // 7) 暂停按钮初始禁用：只有排序任务启动后才允许点击。
        if (pauseButton != null) {
            pauseButton.setDisable(true);
        }

        // 8) 退出排序按钮初始禁用：只有排序任务启动后才允许点击。
        if (exitSortButton != null) {
            exitSortButton.setDisable(true);
        }
    }

    @FXML
    private void onGenerateData() {
        // 如果用户正在查看“性能比较的 TabPane”，此时生成数据应回到可视化面板。
        if (rootPane.getCenter() != visualizerPane) {
            rootPane.setCenter(visualizerPane);
        }

        // 解析数据规模：提供默认值与边界限制。
        // 上限 500：避免柱子过密导致 UI 体验差 / 动画过慢。
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
            // 非数字输入直接回退默认值
            dataSizeField.setText("50");
        }

        String type = dataTypeComboBox.getValue();
        if (type == null) type = "随机数据";

        // 根据数据类型选择生成策略。
        // 说明：这里生成的是“用于演示/排序”的 currentArray；benchmark 会另行生成，不复用该数组。
        switch (type) {
            case "随机数据" -> currentArray = DataGenerator.generateLinearShuffledData(size);//"->" 自动跳出，不会"贯穿"
            case "有序数据" -> currentArray = DataGenerator.generateSortedData(size);
            case "逆序数据" -> currentArray = DataGenerator.generateReversedData(size);
            case "部分有序" -> currentArray = DataGenerator.generateNearlySortedData(size);
            default -> currentArray = DataGenerator.generateLinearShuffledData(size);
        }

        // 数据生成后立即触发绘制
        visualizerPane.setArray(currentArray);
        statusLabel.setText("数据已生成 (" + type + ")，准备排序。");
    }

    @FXML
    private void onShowData() {
        // 将用户在文本框中输入的数据解析为 int[] 并展示。
        // 解析规则与错误信息由 DataInputService 统一管理，Controller 负责提示。
        String input = customDataField != null ? customDataField.getText() : null;
        try {
            // 直接赋值，避免冗余局部变量
            // 说明：parseInputString 支持“逗号/空格”等常见分隔形式
            currentArray = dataInputService.parseInputString(input);

            // 可能在性能结果视图中，展示数据时强制回到可视化面板
            if (rootPane.getCenter() != visualizerPane) {
                rootPane.setCenter(visualizerPane);
            }

            visualizerPane.setArray(currentArray);
            statusLabel.setText("已显示自定义数据，数量：" + currentArray.length);
        } catch (IllegalArgumentException ex) {
            // 解析失败时弹框提示（不让异常打断 UI）
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            alert.setHeaderText("解析失败");
            alert.showAndWait();//阻塞当前线程，直到用户关闭对话框
        }
    }

    @FXML
    private void onLoadFile() {
        // 文件选择 + 解析：支持 txt/csv；具体解析逻辑由 DataInputService 负责。
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择数据文件");
        // 过滤器只是 UI 便利，不影响最终读取（用户仍可选择 *.*）
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("文本文件", "*.txt", "*.csv"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );

        // Scene 可能尚未就绪（理论上 initialize 后一般就绪，但这里仍做防御）
        Window win = rootPane.getScene() != null ? rootPane.getScene().getWindow() : null;
        File file = chooser.showOpenDialog(win);
        // 用户点“取消”时 file 为 null，直接返回即可
        if (file == null) return; // 用户取消

        try {
            currentArray = dataInputService.loadFromFile(file);

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
        // 前置条件：必须先有数据
        if (currentArray == null) return;

        // 保存排序前快照，用于“退出排序”恢复
        // 注意：必须 clone()，避免排序过程中原数组被就地修改导致快照失效
        arrayBeforeSort = currentArray.clone();
        exitRequestedByUser = false;

        // 启动排序动画前，确保当前在可视化面板
        if (rootPane.getCenter() != visualizerPane) {
            rootPane.setCenter(visualizerPane);
        }

        String algoName = algorithmComboBox.getValue();
        // algoName 为中文显示名；SortingService 内部会通过 AlgorithmRegistry 找到对应 Sorter

        // 排序过程中禁用输入控件：防止用户生成新数组/切换算法/启动 benchmark 造成并发与状态错乱。
        setControlsDisabled(true);
        statusLabel.setText("正在使用 " + algoName + " 排序...");

        // 创建后台排序任务：
        // - SortingService 内部负责：获取算法实例、执行排序、按步骤回调更新 VisualizerPane
        // - 这里传入 () -> delay 是关键：允许排序过程动态调速（滑块改变无需重启任务）
        Task<Void> sortTask = sortingService.createSortTask(algoName, currentArray, visualizerPane, () -> delay);
        currentSortTask = sortTask;

        // 排序开始后才允许暂停/继续
        if (pauseButton != null) {
            pauseButton.setDisable(false);
            pauseButton.setText("暂停");
        }

        // 排序开始后才允许退出
        if (exitSortButton != null) {
            exitSortButton.setDisable(false);
        }

        // 任务成功：恢复 UI，清理任务引用，并兜底恢复暂停标志（避免下一次排序卡住）
        sortTask.setOnSucceeded(e -> {
            setControlsDisabled(false);
            statusLabel.setText("排序完成！");
            if (pauseButton != null) {
                pauseButton.setDisable(true);
                pauseButton.setText("暂停");
            }
            if (exitSortButton != null) {
                exitSortButton.setDisable(true);
            }
            sortingService.resume();
            currentSortTask = null;
        });

        // 任务失败：恢复 UI，并显示错误原因
        sortTask.setOnFailed(e -> {
            setControlsDisabled(false);
            // 注意：异常来自后台线程，需在这里给用户一个可读提示
            statusLabel.setText("排序失败: " + sortTask.getException().getMessage());
            if (pauseButton != null) {
                pauseButton.setDisable(true);
                pauseButton.setText("暂停");
            }
            if (exitSortButton != null) {
                exitSortButton.setDisable(true);
            }
            sortingService.resume();
            currentSortTask = null;
        });

        // 设置任务取消时的处理逻辑
        sortTask.setOnCancelled(e -> {
            // 恢复所有被禁用的控件状态
            setControlsDisabled(false);

            // 重置暂停按钮状态
            if (pauseButton != null) {
                // 禁用暂停按钮
                pauseButton.setDisable(true);
                // 恢复按钮文本为"暂停"
                pauseButton.setText("暂停");
            }
            // 禁用退出排序按钮
            if (exitSortButton != null) {
                exitSortButton.setDisable(true);
            }

            // 恢复排序服务的运行状态（以防在暂停状态被取消）
            sortingService.resume();
            // 清空当前任务引用，以便垃圾回收
            currentSortTask = null;

            if (exitRequestedByUser && arrayBeforeSort != null) {
                // 用户主动“退出排序”：恢复排序前数据，让界面回到“开始排序前”的状态
                currentArray = arrayBeforeSort.clone();
                if (rootPane.getCenter() != visualizerPane) {
                    rootPane.setCenter(visualizerPane);
                }
                visualizerPane.setArray(currentArray);
                statusLabel.setText("已退出排序，已恢复到排序前数据。");
            } else {
                // 其他原因导致取消（例如外部调用 cancel）
                statusLabel.setText("排序已取消。");
            }

            exitRequestedByUser = false;
        });

        // 启动任务：这里直接 new Thread 启动后台线程。
        // 说明：JavaFX 的 Task 需要在非 FX 线程运行；UI 更新由 Task/Service 内部用 Platform.runLater 完成
        new Thread(sortTask).start();
    }

    @FXML
    private void onExitSort() {
        // 只允许退出“正在运行”的排序任务
        if (currentSortTask == null || !currentSortTask.isRunning()) return;

        exitRequestedByUser = true;
        statusLabel.setText("正在退出排序...");

        // 先解除暂停，避免排序线程卡在 wait 上无法结束
        // requestCancel 会让 SortingService 退出等待/睡眠并尽快结束
        sortingService.requestCancel();

        // 触发 Task 取消（会中断 sleep，并配合 SortingService 的协作式检查尽快退出）
        currentSortTask.cancel();

        if (pauseButton != null) {
            pauseButton.setDisable(true);
        }
        if (exitSortButton != null) {
            exitSortButton.setDisable(true);
        }
    }

    @FXML
    private void onPauseResume() {
        // 只允许暂停“正在运行”的排序任务
        if (currentSortTask == null || !currentSortTask.isRunning()) return;

        // 暂停协议由 SortingService 实现：排序线程在每一步之间检查 paused 标志并等待/唤醒。
        if (sortingService.isPaused()) {
            sortingService.resume();
            if (pauseButton != null) pauseButton.setText("暂停");
            statusLabel.setText("继续排序...");
        } else {
            // 暂停只影响“动画排序”，不影响 benchmark
            sortingService.pause();
            if (pauseButton != null) pauseButton.setText("继续");
            statusLabel.setText("已暂停，点击继续。");
        }
    }

    @FXML
    private void onBenchmark() {
        // 性能比较：会在后台批量运行多个算法并统计结果。
        // 注意：benchmark 不复用 currentArray，而是由 BenchmarkService 针对每个算法生成/复制数据，确保公平。
        int size;
        try {
            size = Integer.parseInt(dataSizeField.getText());
        } catch (NumberFormatException e) {
            size = 500;
        }

        final int benchmarkSize = size;
        final String dataType = dataTypeComboBox.getValue();

        statusLabel.setText("正在进行性能比较...");
        setControlsDisabled(true);

        // 获取所有算法名称，并排除“不适合严肃性能对比”的演示型算法。
        // - 猴子排序/睡眠排序：随机或依赖线程调度，结果波动大且可能极慢
        // - 珠排序：对数据范围/分布敏感，且实现往往有额外开销，不适合与主流算法公平对比
        List<String> algos = AlgorithmRegistry.getAllAlgorithmNames().stream()
                .filter(name -> !name.equals("猴子排序") && !name.equals("睡眠排序") && !name.equals("珠排序"))
                .collect(Collectors.toList());

        // 说明：benchmarkTask 运行在后台线程，完成后通过 setOnSucceeded 切回 UI 展示图表/表格

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
        // 将中心视图切换为 TabPane：展示时间图/内存图/表格。
        // Tab 标题携带 size/type，便于用户确认本次 benchmark 的参数。
        TabPane tabPane = new TabPane();

        String typeText = (type == null || type.isBlank()) ? "数据" : type;
        String prefix = "[" + typeText + ", n=" + size + "] ";

        Tab timeTab = new Tab(prefix + "时间对比", createTimeChart(metrics));
        timeTab.setClosable(false);

        Tab memoryTab = new Tab(prefix + "内存对比", createMemoryChart(metrics));
        memoryTab.setClosable(false);

        Tab tableTab = new Tab(prefix + "详细数据", createDetailTable(metrics));
        tableTab.setClosable(false);

        tabPane.getTabs().addAll(timeTab, memoryTab, tableTab);
        rootPane.setCenter(tabPane);
    }

    private BarChart<String, Number> createTimeChart(List<PerformanceMetrics> metrics) {
        // 柱状图：X=算法名，Y=耗时(毫秒)
        // 注意：JavaFX 图表会基于数据自动计算坐标轴刻度
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
        // 柱状图：X=算法名，Y=内存占用(MB)
        // 注意：这里的内存值通常是“估算/采样”，不同 JVM/GC 策略会影响结果。
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
        // 表格：适合展示更完整的“算法元信息 + 实测耗时/内存”
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

        // 避免使用 addAll(varargs) 触发“未检查的泛型数组创建”警告，改为逐个 add。
        table.getColumns().add(nameCol);
        table.getColumns().add(timeCol);
        table.getColumns().add(complexityCol);
        table.getColumns().add(spaceCol);
        table.getColumns().add(stableCol);

        table.getItems().addAll(metrics);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    private void setControlsDisabled(boolean disabled) {
        // 统一的 UI 状态控制：排序/benchmark 运行时禁用关键控件，避免并发操作导致状态不一致。
        // 注意：这里不处理 pause/exit，因为它们的可用性由 onSort/onExitSort/onPauseResume 更精确地控制
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
