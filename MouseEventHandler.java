package editor;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;

/**
 * Created by jasmine on 2/28/16.
 */
public class MouseEventHandler implements EventHandler<MouseEvent> {
    private TextLinkedList text;
    private TextCursor tc;
    private ScrollBar sb;
    private Group root;
    private int window_height;
    public MouseEventHandler(TextLinkedList text, TextCursor tc, Group root, ScrollBar sb) {
        this.text = text;
        this.tc = tc;
        this.sb = sb;
        this.root = root;
        window_height = 500;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            double height = text.getLast().i.getY() + text.getLast().prev.i.getLayoutBounds().getHeight();
            if (mouseEvent.getY() > height) {
                text.changeLine((int) mouseEvent.getX(), (int) text.getLast().i.getY());
            } else if (mouseEvent.getY() < 0) {
                text.changeLine((int) mouseEvent.getX(), 0);
            }
            else {
                text.changeLine((int) mouseEvent.getX(), text.roundY((int) (mouseEvent.getY() - root.getLayoutY())));
            }
            if (tc.getY() + root.getLayoutY() < 0) {
                root.setLayoutY( - tc.getY());
                sb.setValue( - (int) root.getLayoutY() * (double) window_height / (height - window_height));
            } else if (tc.getY() + tc.getHeight() + root.getLayoutY() > window_height) {
                root.setLayoutY( - (tc.getY() + tc.getHeight() - window_height));
                sb.setValue( - (int) root.getLayoutY() * (double) window_height / (height - window_height));
            }
        }
    }

    public void updateWindowHeight(int window_height) {
        this.window_height = window_height;
    }
}
