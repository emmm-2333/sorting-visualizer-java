# 项目重构与功能增强总结

## 1. 概述
本项目对排序可视化系统进行了重构和功能增强。主要目标是解耦庞大的 `MainController`，并完善性能比较模块，增加内存占用分析、算法复杂度展示以及更丰富的图表展示。

## 2. 重构工作

### 2.1 控制器解耦 (`MainController`)
原有的 `MainController` 包含了大量的业务逻辑，导致代码臃肿且难以维护。本次重构将其拆分为以下几个部分：

*   **`SortingService`**: 专门负责处理可视化排序的逻辑。它创建并管理排序任务 (`Task`)，处理排序过程中的动画回调（比较、交换、赋值），将 UI 更新逻辑与控制器分离。
*   **`BenchmarkService`**: 专门负责性能测试逻辑。它负责生成测试数据、运行算法、测量时间及内存占用，并返回性能指标列表。
*   **`AlgorithmRegistry`**: 集中管理所有排序算法实例及其元数据。控制器不再直接实例化具体的算法类，而是通过注册表获取。

### 2.2 代码结构优化
*   **移除冗余代码**: 清理了 `MainController` 中不再需要的私有方法和导入。
*   **服务化**: 引入了服务层 (`Service Layer`) 概念，使各个模块职责更单一。

## 3. 功能增强：性能比较模块

### 3.1 数据模型升级
*   **`AlgorithmInfo`**: 新增记录类，用于存储算法的静态元数据，包括：
    *   最佳/平均/最差时间复杂度
    *   空间复杂度
    *   稳定性
*   **`PerformanceMetrics`**: 升级了性能指标记录类，新增了：
    *   `memoryUsageBytes`: 内存占用（估算值）
    *   `algorithmInfo`: 关联的算法元数据

### 3.2 算法注册表 (`AlgorithmRegistry`)
创建了统一的注册表，为每个算法配置了详细的元数据。例如：
*   **冒泡排序**: 时间 O(n²), 空间 O(1), 稳定
*   **快速排序**: 时间 O(n log n), 空间 O(log n), 不稳定
*   ...以及其他所有算法。

### 3.3 性能测试逻辑 (`BenchmarkService`)
*   **内存估算**: 在排序前后调用 `Runtime.getRuntime().totalMemory() - freeMemory()` 并强制 GC，以估算算法的内存消耗。
*   **批量测试**: 支持对选定的算法集合进行批量测试。

### 3.4 结果展示优化
性能比较界面从单一的柱状图升级为 `TabPane` 多视图展示：
1.  **时间对比图**: 展示各算法的执行耗时 (ms)。
2.  **内存对比图**: 展示各算法的内存占用 (MB)。
3.  **详细数据表**: 表格形式展示完整信息，包括：
    *   算法名称
    *   耗时
    *   平均时间复杂度
    *   空间复杂度
    *   稳定性

## 4. 修改文件列表

*   `src/main/java/org/example/sortingvisualizer/controller/MainController.java` (重构)
*   `src/main/java/org/example/sortingvisualizer/service/SortingService.java` (新增)
*   `src/main/java/org/example/sortingvisualizer/service/BenchmarkService.java` (新增)
*   `src/main/java/org/example/sortingvisualizer/algorithm/AlgorithmRegistry.java` (新增)
*   `src/main/java/org/example/sortingvisualizer/model/AlgorithmInfo.java` (新增)
*   `src/main/java/org/example/sortingvisualizer/model/PerformanceMetrics.java` (修改)

## 5. 运行说明
项目依赖 Maven 构建。
*   **编译**: `mvn clean compile`
*   **运行**: `mvn javafx:run`

