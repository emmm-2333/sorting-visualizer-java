package org.example.sortingvisualizer;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 应用程序入口
 */
public class SortingVisualizerApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
            SortingVisualizerApp.class.getResource("/org/example/sortingvisualizer/view/MainLayout.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        var cssUrl = SortingVisualizerApp.class.getResource("/org/example/sortingvisualizer/view/apple.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        stage.setTitle("排序算法性能比较与动画演示系统");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
