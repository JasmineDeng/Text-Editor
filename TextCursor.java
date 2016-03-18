package editor;

import javafx.scene.shape.Rectangle;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jasmine on 2/24/16.
 */
public class TextCursor extends Rectangle {

    private Timer t;
    private final int TIME = 500;

    public TextCursor() {
        super(5, 0, 1, 10);
        startFlash();
    }

    private class CursorTimerTask extends TimerTask {
        TextCursor tc;
        int opacity;
        public CursorTimerTask(TextCursor tc) {
            this.tc = tc;
            opacity = 1;
        }
        @Override
        public void run() {
            tc.setOpacity(opacity);
            opacity = 1 - opacity;
        }
    }

    public void halt() {
        if (t != null) {
            t.cancel();
        }
    }

    private void startFlash() {
        t = new Timer();
        t.schedule(new CursorTimerTask(this), 0, TIME);
    }

    public String toString() {
        return "TextCursor[x=" + this.getX() + ",y=" + this.getY() + "]";
    }

}
