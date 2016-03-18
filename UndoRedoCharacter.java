package editor;

import javafx.scene.text.Text;

/**
 * Created by jasmine on 3/6/16.
 */
public class UndoRedoCharacter {
    private Text t;
    private int cursor_pos;
    private boolean added_char;
    public UndoRedoCharacter(Text t, int cursor_pos, boolean added_char) {
        this.t = t;
        this.cursor_pos = cursor_pos;
        this.added_char = added_char;
    }

    public Text getTextObj() {
        return t;
    }

    public boolean wasAdded() {
        return added_char;
    }

    public int cursorPosition() {
        return cursor_pos;
    }
}
