package org.example.sortingvisualizer.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.example.sortingvisualizer.algorithm.AlgorithmRegistry;
import org.example.sortingvisualizer.algorithm.Sorter;
import org.example.sortingvisualizer.model.PerformanceMetrics;
import org.example.sortingvisualizer.playback.PlaybackController;
import org.example.sortingvisualizer.playback.PlaybackSnapshot;
import org.example.sortingvisualizer.service.BenchmarkService;
import org.example.sortingvisualizer.service.DataInputService;
import org.example.sortingvisualizer.service.StepRecordingService;
import org.example.sortingvisualizer.step.RecordedSort;
import org.example.sortingvisualizer.step.SortOperation;
import org.example.sortingvisualizer.step.SortOperationType;
import org.example.sortingvisualizer.util.DataGenerator;
import org.example.sortingvisualizer.view.BenchmarkViewBuilder;
import org.example.sortingvisualizer.view.VisualizerPane;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * 主界面控制器（Controller）。
 * <p>
 * 角色定位：这是一个“流程编排器”，只负责 UI 交互、输入校验、任务启动与视图切换。
 * <ul>
 *   <li>排序步骤录制与回放：由 {@link StepRecordingService} 与 {@link org.example.sortingvisualizer.playback.PlaybackController} 负责</li>
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

    private static final Color COLOR_SORTED_FINISH = Color.web("#30d158");

    private final BenchmarkViewBuilder benchmarkViewBuilder = new BenchmarkViewBuilder();

    /** 回放控制器：封装 next/prev/start/pause + 定时逻辑。 */
    private final PlaybackController playbackController = new PlaybackController();

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

    /** 上一步：基于录制回放模式。 */
    @FXML
    private Button prevStepButton;

    /** 下一步：基于录制回放模式。 */
    @FXML
    private Button nextStepButton;

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

    /** 步骤计数显示：例如 123/456。 */
    @FXML
    private Label stepLabel;

    /** 当前操作回显：例如“交换 a[3]=4 与 a[5]=7”。 */
    @FXML
    private Label operationLabel;

    /** 统计：比较次数（当前/总）。 */
    @FXML
    private Label compareCountLabel;

    /** 统计：交换次数（当前/总）。 */
    @FXML
    private Label swapCountLabel;

    /** 统计：写入次数（当前/总）。 */
    @FXML
    private Label setCountLabel;

    /** 可视化绘制面板（柱状图动画都在这里画）。 */
    private VisualizerPane visualizerPane;

    /** 当前用于展示/排序的数据源。生成/加载/自定义输入会覆盖它。 */
    private int[] currentArray;

    /** 录制服务：把排序算法回调转换成可回放/可撤销的操作序列。 */
    private final StepRecordingService stepRecordingService = new StepRecordingService();
    /** 性能比较服务：批量运行算法并统计耗时/内存、输出 {@link PerformanceMetrics} 列表。 */
    private final BenchmarkService benchmarkService = new BenchmarkService();
    /** 输入服务：解析自定义输入字符串、读取文件并转成 int[]。 */
    private final DataInputService dataInputService = new DataInputService();

    /** 当前录制任务引用：用于“退出排序”时取消。 */
    private Task<RecordedSort> currentRecordTask;

    /** 快捷键是否已安装到 Scene。 */
    private boolean shortcutsInstalled;

    /** 统计前缀数组：prefix[i] 表示前 i 步（0..i-1）累计次数。 */
    private int[] comparePrefix;
    private int[] swapPrefix;
    private int[] setPrefix;

    /** 是否对当前回放禁用统计（例如：猴子排序步骤数量不可控）。 */
    private boolean suppressStepStats;

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
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            delay = mapSpeedToDelay(newVal.doubleValue());
            // 回放进行中也要实时刷新延迟，否则只会在“重新开始回放/按键步进后”才生效。
            playbackController.setDelayMillis(delay);
        });
        // 初始化一次 delay（否则第一次排序仍可能用默认 50ms）
        delay = mapSpeedToDelay(speedSlider.getValue());
        playbackController.setDelayMillis(delay);

        // 5) 初始生成一组数据，保证一启动就有可视化内容。
        onGenerateData();

        // 6) 标签显示切换：只影响绘制，不影响排序逻辑。
        //    默认显示数值标签。
        if (showValuesCheckbox != null) {
            showValuesCheckbox.setSelected(true);
            showValuesCheckbox.selectedProperty().addListener((obs, ov, nv) -> visualizerPane.setShowLabels(nv));
        }
        visualizerPane.setShowLabels(true);

        // 7) 暂停按钮初始禁用：只有排序任务启动后才允许点击。
        if (pauseButton != null) {
            pauseButton.setDisable(true);
        }

        // 8) 退出排序按钮初始禁用：只有排序任务启动后才允许点击。
        if (exitSortButton != null) {
            exitSortButton.setDisable(true);
        }

        if (prevStepButton != null) {
            prevStepButton.setDisable(true);
        }
        if (nextStepButton != null) {
            nextStepButton.setDisable(true);
        }

        if (stepLabel != null) {
            stepLabel.setText("步骤: 0/0");
        }
        if (operationLabel != null) {
            operationLabel.setText("-");
        }

        if (compareCountLabel != null) compareCountLabel.setText("比较: 0/0");
        if (swapCountLabel != null) swapCountLabel.setText("交换: 0/0");
        if (setCountLabel != null) setCountLabel.setText("写入: 0/0");

        // 9) 安装快捷键：←/→ 上一步/下一步，Space 暂停/继续
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null || shortcutsInstalled) return;
            shortcutsInstalled = true;
            newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        });
    }

    private void onKeyPressed(KeyEvent e) {
        if (e == null) return;
        if (!playbackController.isLoaded()) return;

        // 避免影响输入框/下拉框/滑块的键盘操作
        var scene = rootPane.getScene();
        var focus = (scene != null) ? scene.getFocusOwner() : null;
        if (focus instanceof TextInputControl) return;
        if (focus instanceof ComboBoxBase<?>) return;
        if (focus instanceof Slider) return;

        if (e.getCode() == KeyCode.LEFT) {
            onPrevStep();
            e.consume();
        } else if (e.getCode() == KeyCode.RIGHT) {
            onNextStep();
            e.consume();
        } else if (e.getCode() == KeyCode.SPACE) {
            onPauseResume();
            e.consume();
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
        int size = 30;
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
            dataSizeField.setText("30");
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

        // 猴子排序在数据量稍大时步骤数呈阶乘级爆炸，录制/回放很容易导致内存或时间异常。
        // 这里做硬保护，避免直接把程序拖崩。
        if ("猴子排序".equals(algoName) && currentArray.length > 9) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "猴子排序在 n>9 时极易导致程序异常（步骤爆炸/内存耗尽）。\n\n建议：把数据量改为 5~9 再运行。", ButtonType.OK);
            alert.setHeaderText("已阻止：猴子排序数据量过大");
            alert.showAndWait();
            setControlsDisabled(false);
            resetStepUi();
            statusLabel.setText("已取消：猴子排序数据量过大。");
            return;
        }

        // 猴子排序步骤数量可能极大且波动，统计次数意义不大且容易影响体验，因此禁用。
        suppressStepStats = "猴子排序".equals(algoName);

        // 排序过程中禁用输入控件：防止用户生成新数组/切换算法/启动 benchmark 造成并发与状态错乱。
        setControlsDisabled(true);
        statusLabel.setText("正在使用 " + algoName + " 排序...");

        // 录制/回放期间允许“退出排序”用于恢复到排序前
        if (exitSortButton != null) {
            exitSortButton.setDisable(false);
        }

        // 录制 + 回放模式：先把操作序列录出来，再支持“上一步/下一步/暂停/继续”。
        stopPlaybackIfNeeded();

        if (pauseButton != null) {
            pauseButton.setDisable(true);
            pauseButton.setText("暂停");
        }
        if (prevStepButton != null) prevStepButton.setDisable(true);
        if (nextStepButton != null) nextStepButton.setDisable(true);
        if (stepLabel != null) stepLabel.setText("步骤: 0/0");
        if (operationLabel != null) operationLabel.setText("正在准备步骤... ");

        currentRecordTask = new Task<>() {
            @Override
            protected RecordedSort call() {
                Sorter sorter = AlgorithmRegistry.getSorter(algoName);
                if (sorter == null) {
                    throw new IllegalArgumentException("找不到算法：" + algoName);
                }
                return stepRecordingService.record(algoName, sorter, currentArray);
            }
        };

        // 录制完成：初始化播放器并开始回放
        currentRecordTask.setOnSucceeded(e -> {
            RecordedSort recorded = currentRecordTask.getValue();
            if (suppressStepStats) {
                comparePrefix = null;
                swapPrefix = null;
                setPrefix = null;
                if (compareCountLabel != null) compareCountLabel.setText("比较: -/-");
                if (swapCountLabel != null) swapCountLabel.setText("交换: -/-");
                if (setCountLabel != null) setCountLabel.setText("写入: -/-");
            } else {
                initOperationStats(recorded.operations());
            }

            playbackController.setDelayMillis(delay);
            playbackController.setOnUpdate(this::onPlaybackUpdate);
            playbackController.setOnFinished(this::onPlaybackFinished);
            playbackController.load(recorded);

            // 初始状态
            visualizerPane.setArray(recorded.initialArray());
            updateStepUi(null);

            if (pauseButton != null) {
                pauseButton.setDisable(false);
                pauseButton.setText("暂停");
            }
            if (exitSortButton != null) exitSortButton.setDisable(false);
            if (prevStepButton != null) prevStepButton.setDisable(true);
            if (nextStepButton != null) nextStepButton.setDisable(!playbackController.hasNext());

            statusLabel.setText("回放中：" + algoName);
            startPlayback();
            currentRecordTask = null;
        });

        currentRecordTask.setOnFailed(e -> {
            setControlsDisabled(false);
            Throwable ex = currentRecordTask.getException();
            statusLabel.setText("准备失败: " + (ex == null ? "未知错误" : ex.getMessage()));
            resetStepUi();
            currentRecordTask = null;
        });

        currentRecordTask.setOnCancelled(e -> {
            setControlsDisabled(false);
            resetStepUi();

            if (exitRequestedByUser && arrayBeforeSort != null) {
                currentArray = arrayBeforeSort.clone();
                if (rootPane.getCenter() != visualizerPane) {
                    rootPane.setCenter(visualizerPane);
                }
                visualizerPane.setArray(currentArray);
                statusLabel.setText("已退出排序，已恢复到排序前数据。");
            } else {
                statusLabel.setText("已取消。");
            }
            exitRequestedByUser = false;
            currentRecordTask = null;
        });

        new Thread(currentRecordTask).start();
    }

    @FXML
    private void onExitSort() {
        exitRequestedByUser = true;
        statusLabel.setText("正在退出...");

        stopPlaybackIfNeeded();

        if (currentRecordTask != null && currentRecordTask.isRunning()) {
            currentRecordTask.cancel();
        }

        // 如果没有录制任务在跑，直接恢复 UI
        if (arrayBeforeSort != null) {
            currentArray = arrayBeforeSort.clone();
            if (rootPane.getCenter() != visualizerPane) {
                rootPane.setCenter(visualizerPane);
            }
            visualizerPane.setArray(currentArray);
        }

        setControlsDisabled(false);
        resetStepUi();

        statusLabel.setText("已退出排序，已恢复到排序前数据。");
        exitRequestedByUser = false;
    }

    @FXML
    private void onPauseResume() {
        // 回放模式：暂停/继续 Timeline
        if (!playbackController.isLoaded()) return;

        if (playbackController.isPlaying()) {
            pausePlayback();
            statusLabel.setText("已暂停，支持上一步/下一步。");
        } else {
            startPlayback();
            statusLabel.setText("继续回放...");
        }
    }

    @FXML
    private void onPrevStep() {
        if (!playbackController.isLoaded()) return;
        pausePlayback();
        playbackController.prev();
    }

    @FXML
    private void onNextStep() {
        if (!playbackController.isLoaded()) return;
        pausePlayback();
        playbackController.next();
    }

    private void startPlayback() {
        if (!playbackController.isLoaded()) return;
        playbackController.setDelayMillis(delay);
        if (pauseButton != null) {
            pauseButton.setText("暂停");
        }
        playbackController.start();
    }

    private void pausePlayback() {
        playbackController.pause();
        if (pauseButton != null) {
            pauseButton.setText("继续");
        }
    }

    private void stopPlaybackIfNeeded() {
        // 停止回放前先清理回调：避免 pause/stop/load 过程中 emit 触发旧回调，
        // 从而在统计前缀尚未初始化/已过期时造成越界。
        playbackController.setOnUpdate(null);
        playbackController.setOnFinished(null);
        pausePlayback();
        playbackController.stop();
    }

    private void onPlaybackUpdate(PlaybackSnapshot snapshot) {
        if (snapshot == null) return;
        updateStepUi(snapshot.operation());
    }

    private void onPlaybackFinished() {
        // 到头：停止回放，保持最终状态
        pausePlayback();
        visualizerPane.renderFinalState(playbackController.currentArray(), COLOR_SORTED_FINISH);
        if (pauseButton != null) {
            pauseButton.setDisable(true);
            pauseButton.setText("暂停");
        }
        if (exitSortButton != null) {
            exitSortButton.setDisable(true);
        }
        setControlsDisabled(false);
        statusLabel.setText("排序完成！");
    }

    private void resetStepUi() {
        pausePlayback();
        comparePrefix = null;
        swapPrefix = null;
        setPrefix = null;
        suppressStepStats = false;
        if (pauseButton != null) {
            pauseButton.setDisable(true);
            pauseButton.setText("暂停");
        }
        if (exitSortButton != null) {
            exitSortButton.setDisable(true);
        }
        if (prevStepButton != null) prevStepButton.setDisable(true);
        if (nextStepButton != null) nextStepButton.setDisable(true);
        if (stepLabel != null) stepLabel.setText("步骤: 0/0");
        if (operationLabel != null) operationLabel.setText("-");

        if (compareCountLabel != null) compareCountLabel.setText("比较: 0/0");
        if (swapCountLabel != null) swapCountLabel.setText("交换: 0/0");
        if (setCountLabel != null) setCountLabel.setText("写入: 0/0");
    }

    private void updateStepUi(SortOperation op) {
        if (!playbackController.isLoaded()) {
            if (stepLabel != null) stepLabel.setText("步骤: 0/0");
            if (operationLabel != null) operationLabel.setText("-");
            return;
        }

        int[] state = playbackController.currentArray();
        if (op != null) {
            visualizerPane.renderState(state, op.index1(), op.index2(), colorForOperation(op.type()));
            if (operationLabel != null) {
                operationLabel.setText(op.description(null));
            }
        } else {
            visualizerPane.renderState(state, -1, -1, null);
            if (operationLabel != null) {
                operationLabel.setText("准备开始");
            }
        }

        // 若已到末尾（排序完成态），统一渲染为绿色，避免停留在最后一步高亮色。
        if (playbackController.cursor() == playbackController.size()) {
            visualizerPane.renderFinalState(state, COLOR_SORTED_FINISH);
        }

        if (stepLabel != null) {
            stepLabel.setText("步骤: " + playbackController.cursor() + "/" + playbackController.size());
        }
        if (prevStepButton != null) prevStepButton.setDisable(!playbackController.hasPrev());
        if (nextStepButton != null) nextStepButton.setDisable(!playbackController.hasNext());

        updateStatsUi();
    }

    private void initOperationStats(List<SortOperation> operations) {
        List<SortOperation> ops = (operations == null) ? List.of() : operations;
        int size = ops.size();
        comparePrefix = new int[size + 1];
        swapPrefix = new int[size + 1];
        setPrefix = new int[size + 1];
        for (int i = 0; i < size; i++) {
            comparePrefix[i + 1] = comparePrefix[i];
            swapPrefix[i + 1] = swapPrefix[i];
            setPrefix[i + 1] = setPrefix[i];

            SortOperation op = ops.get(i);
            if (op == null || op.type() == null) continue;
            switch (op.type()) {
                case COMPARE -> comparePrefix[i + 1]++;
                case SWAP -> swapPrefix[i + 1]++;
                case SET -> setPrefix[i + 1]++;
            }
        }
    }

    private void updateStatsUi() {
        if (suppressStepStats) {
            // 已在 onSort/onSucceeded 初始化为 -/-，此处保持不动
            return;
        }
        if (!playbackController.isLoaded() || comparePrefix == null || swapPrefix == null || setPrefix == null) {
            return;
        }
        int cursor = playbackController.cursor();
        int total = playbackController.size();
        if (cursor < 0) cursor = 0;
        if (cursor > total) cursor = total;

        // 防守：统计数组按“录制时 operations.size()”生成，理论上应与 total 一致。
        // 若出现任何状态不同步（例如旧回调/旧统计未清干净），避免直接越界导致 UI 崩溃。
        int maxIndex = Math.min(comparePrefix.length, Math.min(swapPrefix.length, setPrefix.length)) - 1;
        if (maxIndex < 0) return;
        if (total > maxIndex) total = maxIndex;
        if (cursor > total) cursor = total;

        int compareNow = comparePrefix[cursor];
        int swapNow = swapPrefix[cursor];
        int setNow = setPrefix[cursor];

        int compareTotal = comparePrefix[total];
        int swapTotal = swapPrefix[total];
        int setTotal = setPrefix[total];

        if (compareCountLabel != null) compareCountLabel.setText("比较: " + compareNow + "/" + compareTotal);
        if (swapCountLabel != null) swapCountLabel.setText("交换: " + swapNow + "/" + swapTotal);
        if (setCountLabel != null) setCountLabel.setText("写入: " + setNow + "/" + setTotal);
    }

    private Color colorForOperation(SortOperationType type) {
        if (type == null) return null;
        return switch (type) {
            case COMPARE -> Color.web("#ff3b30");
            case SWAP -> Color.web("#34c759");
            case SET -> Color.web("#007aff");
        };
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
        });

        new Thread(benchmarkTask).start();
    }

    private void showBenchmarkResults(List<PerformanceMetrics> metrics, int size, String type) {
        rootPane.setCenter(benchmarkViewBuilder.buildResults(metrics, size, type));
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
