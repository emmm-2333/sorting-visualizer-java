package org.example.sortingvisualizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * 应用程序入口
 */
public class SortingVisualizerApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // 使用绝对类路径，确保在打包和运行时都能找到 FXML
        FXMLLoader fxmlLoader = new FXMLLoader(
            SortingVisualizerApp.class.getResource("/org/example/sortingvisualizer/view/MainLayout.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("排序算法性能比较与动画演示系统");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
