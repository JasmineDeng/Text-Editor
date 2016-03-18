package editor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.control.ScrollBar;

/**
 * Created by jasmine on 2/28/16.
 */
public class TextLayoutManager {
    private TextLinkedList text;
    private TextCursor tc;
    private KeyEventHandler keyHandle;
    private MouseEventHandler mouseHandle;
    private ScrollBar sb;
    private final int WINDOW_HEIGHT = 500;
    private final int WINDOW_WIDTH = 500;

    public TextLayoutManager(final Group root, String fileName) {
        tc = new TextCursor();
        text = new TextLinkedList(tc);
        sb = new ScrollBar();
        sb.setOrientation(Orientation.VERTICAL);
        sb.setPrefHeight(WINDOW_HEIGHT);
        sb.setMin(0);
        sb.setMax(0);
        int usableScreenWidth = (int) (WINDOW_WIDTH - sb.getLayoutBounds().getWidth());
        sb.setLayoutX(usableScreenWidth);

        Group textRoot = new Group();
        root.getChildren().add(sb);
        root.getChildren().add(textRoot);

        sb.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                double height = (text.get(text.size() - 1).getY() + text.get(text.size() - 2).getLayoutBounds().getHeight());
                int shift = - (int) ((double) newValue * (height - keyHandle.getWindowHeight()) / (double) keyHandle.getWindowHeight());
                if (Math.ceil((double) newValue) > keyHandle.getWindowHeight() - 2 && Math.ceil((double) newValue) < keyHandle.getWindowHeight() + 2) {
                    sb.setValue(keyHandle.getWindowHeight());
                }
                textRoot.setLayoutY(shift);
            }
        });
        textRoot.getChildren().add(tc);

        keyHandle = new KeyEventHandler(text, tc, textRoot, fileName, sb);
        mouseHandle = new MouseEventHandler(text, tc, textRoot, sb);
    }

    public KeyEventHandler getKeyHandle() {
        return keyHandle;
    }

    public MouseEventHandler getMouseHandle() {
        return mouseHandle;
    }

    public TextCursor getTc() {
        return tc;
    }

    public void updateWindowHeight(double window_height) {
        mouseHandle.updateWindowHeight((int) window_height);
        keyHandle.updateWindowSize(-1, window_height);
    }

    public void updateWindowWidth(double window_width) {
        keyHandle.updateWindowSize(window_width, -1);
    }
}
