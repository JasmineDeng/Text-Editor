package editor;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Editor extends Application {

    private static String fileName;

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();

        Scene scene = new Scene(root, 500, 500, Color.WHITE);

        TextLayoutManager tlm = new TextLayoutManager(root, fileName);

        scene.setOnKeyTyped(tlm.getKeyHandle());
        scene.setOnKeyPressed(tlm.getKeyHandle());
        scene.setOnKeyReleased(tlm.getKeyHandle());

        scene.setOnMouseClicked(tlm.getMouseHandle());

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                tlm.updateWindowWidth((double)newSceneWidth);
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                tlm.updateWindowHeight((double) newSceneHeight);
            }
        });

        primaryStage.setTitle(fileName);
        primaryStage.setOnCloseRequest(event -> tlm.getTc().halt());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No filename provided.");
            return;
        } else if (args.length > 2) {
            System.out.println("Too many arguments.");
            return;
        }
        else {
            fileName = args[0];
        }
        launch(args);
    }
}