package editor;

import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.*;
import java.util.ArrayDeque;

/**
 * Created by jasmine on 2/24/16.
 */

public class KeyEventHandler implements EventHandler<KeyEvent> {
    private int window_width = 500;
    private int window_height = 500;

    private TextLinkedList text;

    int space_index;

    private Group root;

    private boolean command_press;

    private int fontSize = 12;
    private String fontName = "Verdana";

    private TextCursor tc;

    private String fileName;
    private File inputFile;

    private ScrollBar sb;

    private ArrayDeque<UndoRedoCharacter> undo;
    private ArrayDeque<UndoRedoCharacter> redo;

    public KeyEventHandler(TextLinkedList t, TextCursor new_tc, final Group root, String fileName, ScrollBar sb) {
        tc = new_tc;
        this.sb = sb;

        space_index = -1;

        text = t;
        text.add(new Text(""));
        text.get(0).setY(0);
        text.get(0).setX(5);
        text.get(0).setTextOrigin(VPos.TOP);
        text.get(0).setFont(Font.font(fontName, fontSize));

        text.add(new Text(""));
        text.get(0).setY(0);
        text.get(0).setX(5);
        text.get(0).setTextOrigin(VPos.TOP);
        text.get(0).setFont(Font.font(fontName, fontSize));
        text.moveRight();

        tc.setHeight((int) text.get(0).getLayoutBounds().getHeight());

        this.root = root;

        root.getChildren().add(text.get(0));
        root.getChildren().add(text.get(1));

        this.fileName = fileName;
        inputFile = new File(fileName);

        undo = new ArrayDeque<>();
        redo = new ArrayDeque<>();

        try {
            if (!inputFile.exists()) {
                inputFile.createNewFile();
            }
            FileReader reader = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(reader);

            int intRead = -1;
            while ((intRead = br.read()) != -1) {
                char charRead = (char) intRead;
                if (String.valueOf(charRead).equals("\n")) {
                    text.add(new Text(5, (int) (text.get(text.size() - 2).getY() + text.get(text.size() - 2).getLayoutBounds().getHeight()), ""));
                } else if (String.valueOf(charRead).equals("\r")) {
                    continue;
                } else {
                    text.add(new Text(String.valueOf(charRead)));
                }
                if (String.valueOf(charRead).equals(" ")) {
                    space_index = text.size() - 1;
                }

                text.getRel().setFont(Font.font(fontName, fontSize));
                text.getRel().setTextOrigin(VPos.TOP);

                root.getChildren().add(text.getRelInd(), text.getRel());
                text.moveRight();
            }
            while (text.getRelInd() > 1) {
                text.moveLeft();
            }
            renderAllText();
            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Unable to read file \"" + fileName + "\"");
            tc.halt();
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Error encountered while trying to read " + fileName);
            tc.halt();
            System.exit(0);
        }

        command_press = false;
    }

    @Override
    public void handle(KeyEvent keyEvent) {
        if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
            String characterTyped = keyEvent.getCharacter();
            if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8 && characterTyped.charAt(0) != 13
                    && !(command_press && characterTyped.equals("=")) && !(command_press && characterTyped.equals("-"))
                    && !(command_press && characterTyped.equals("s")) && !(command_press && characterTyped.equals("p"))
                    && !(command_press && characterTyped.equals("z")) && !(command_press && characterTyped.equals("y"))) {
                text.add(new Text(characterTyped));

                root.getChildren().add(text.getRelInd(), text.getRel());
                text.moveRight();

                if (undo.size() == 100) {
                    undo.removeLast();
                }
                undo.push(new UndoRedoCharacter(text.get(text.getRelInd() - 1), text.getRelInd(), true));
                redo.clear();

                renderAllText();
                keyEvent.consume();
            }
        } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
            KeyCode code = keyEvent.getCode();
            if (code == KeyCode.BACK_SPACE) {
                if (text.getRelInd() > 1) {
                    int i = text.getRelInd();
                    Text remove = text.remove();

                    if (undo.size() == 100) {
                        undo.removeLast();
                    }
                    undo.push(new UndoRedoCharacter(remove, i - 1, false));
                    redo.clear();

                    text.moveLeft();
                    if (remove.getY() != text.getRel().getY()) {
                        text.getRel().setY((int) remove.getY());
                        text.getRel().setX((int) (text.get(text.getRelInd() - 1).getX() + Math.round(text.get(text.getRelInd() - 1).getLayoutBounds().getWidth())));
                    }
                    root.getChildren().remove(i - 1);
                    renderAllText();
                }
            } else if (code == KeyCode.ENTER) {
                text.add(new Text((int) (text.get(text.size() - 1).getX() + Math.round(text.get(text.size() - 1).getLayoutBounds().getWidth())), text.get(text.size() - 1).getY(), ""));
                text.getRel().setTextOrigin(VPos.TOP);
                text.getRel().setFont(Font.font(fontName, fontSize));
                root.getChildren().add(text.getRelInd(), text.getRel());
                text.moveRight();

                if (undo.size() == 100) {
                    undo.removeLast();
                }
                undo.push(new UndoRedoCharacter(text.get(text.getRelInd() - 1), text.getRelInd(), true));
                redo.clear();

                renderAllText();
            } else if (command_press && code == KeyCode.Z) {
                if (!undo.isEmpty()) {
                    UndoRedoCharacter temp = undo.pop();
                    redo.push(temp);
                    if (text.getRelInd() < temp.cursorPosition()) {
                        while (text.getRelInd() < temp.cursorPosition()) {
                            text.moveRight();
                        }
                    } else {
                        while (text.getRelInd() > temp.cursorPosition()) {
                            text.moveLeft();
                        }
                    }
                    if (temp.wasAdded()) {
                        text.remove();
                        root.getChildren().remove(text.getRelInd() - 1);
                        text.moveLeft();
                        renderAllText();
                    } else {
                        text.add(temp.getTextObj());
                        root.getChildren().add(text.getRelInd(), text.getRel());
                        text.moveRight();
                        renderAllText();
                    }
                }
            } else if (command_press && code == KeyCode.Y) {
                if (!redo.isEmpty()) {
                    UndoRedoCharacter temp = redo.pop();
                    undo.push(temp);
                    if (text.getRelInd() < temp.cursorPosition()) {
                        while (text.getRelInd() < temp.cursorPosition() && text.getRelInd() < text.size() - 1) {
                            text.moveRight();
                        }
                    } else {
                        while (text.getRelInd() > temp.cursorPosition()) {
                            text.moveLeft();
                        }
                    }
                    if (!temp.wasAdded()) {
                        text.moveRight();
                        text.remove();
                        root.getChildren().remove(text.getRelInd() - 1);
                        text.moveLeft();
                        renderAllText();
                    } else {
                        if (text.getRelInd() < text.size() - 1) {
                            text.moveLeft();
                        }
                        text.add(temp.getTextObj());
                        root.getChildren().add(text.getRelInd(), text.getRel());
                        text.moveRight();
                        renderAllText();
                    }
                }
            } else if (command_press && code == KeyCode.EQUALS) {
                fontSize += 4;
                renderAllText();
            } else if (command_press && code == KeyCode.MINUS) {
                fontSize = Math.max(4, fontSize - 4);
                renderAllText();
            } else if (command_press && code == KeyCode.P) {
                System.out.println((int) tc.getX() + ", " + (int) tc.getY());
            } else if (command_press && code == KeyCode.S) {
                try {
                    FileWriter writer = new FileWriter(inputFile);
                    for (int i = 0; i < text.size(); i++) {
                        if (text.get(i).getText().length() > 0) {
                            writer.write(text.get(i).getText().charAt(0));
                        } else if (i != 0 && i != text.size() - 1){
                            writer.write("\n");
                        }
                    }
                    writer.close();
                } catch (IOException e) {
                    System.out.println("Error saving " + fileName + " : " + e.getMessage());
                    tc.halt();
                    System.exit(0);
                }
            } else if (code == KeyCode.COMMAND) {
                command_press = true;
            } else if (code == KeyCode.RIGHT) {
                if (text.getRel().getX() + Math.round(text.getRel().getLayoutBounds().getWidth()) > window_width - 25) {
                    text.moveRight();
                    tc.setX(window_width - 25);
                    tc.setY(text.getRelNode().prev.i.getY());
                } else if (text.getRelInd() < text.size() - 1 && text.getRelNode().next.i.getText().equals("")) {
                    tc.setX((int) (text.getRel().getX() + Math.round(text.getRel().getLayoutBounds().getWidth())));
                    tc.setY(text.getRel().getY());
                    text.moveRight();
                } else if (text.getRelInd() < text.size() - 1) {
                    text.moveRight();
                    tc.setX(text.getRel().getX());
                    tc.setY(text.getRel().getY());
                    tc.setHeight(text.getLast().i.getLayoutBounds().getHeight());
                }
                changeScrollBarY();
            } else if (code == KeyCode.LEFT) {
                if (text.getRelNode().prev.i.getX() + Math.round(text.getRelNode().prev.i.getLayoutBounds().getWidth()) > window_width - 25) {
                    text.moveLeft();
                    tc.setX(window_width - 25);
                    tc.setY(text.getRel().getY());
                } else if (text.getRelNode().prev.i.getText().equals("")) {
                    text.moveLeft();
                    tc.setX((int) (text.getRelNode().prev.i.getX() + Math.round(text.getRelNode().prev.i.getLayoutBounds().getWidth())));
                    tc.setY(text.getRelNode().prev.i.getY());
                } else if (text.getRelInd() > 1) {
                    text.moveLeft();
                    tc.setX(text.getRel().getX());
                    tc.setY(text.getRel().getY());
                    tc.setHeight(text.getLast().i.getLayoutBounds().getHeight());
                }
                changeScrollBarY();
            } else if (code == KeyCode.UP) {
                if (tc.getY() > 0) {
                    text.changeLine((int) tc.getX(), (int) (tc.getY() - (int) text.getRel().getLayoutBounds().getHeight()));
                }
                changeScrollBarY();
            } else if (code == KeyCode.DOWN) {
                if (tc.getY() < text.getLast().i.getY()) {
                    text.changeLine((int) tc.getX(), (int) (tc.getY() + text.getRel().getLayoutBounds().getHeight()));
                }
                changeScrollBarY();
            }
        } else if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED) {
            KeyCode code = keyEvent.getCode();
            if (code == KeyCode.COMMAND) {
                command_press = false;
            }
        }
    }

    public void wrapLine(TextLinkedList.Node i, int index) {
        Text space_text = text.get(space_index);
        Text currText = i.i;
        if (space_index == -1 || (space_text.getY() < currText.getY())) {
            currText.setX(5);
            currText.setY((int) (i.prev.i.getY() + currText.getLayoutBounds().getHeight()));
            text.pushLine((int) currText.getY(), index);
        } else if (space_index < index) {
            int character = space_index + 1;
            TextLinkedList.Node afterspace_text = text.getNode(space_index + 1);
            afterspace_text.i.setX(5);
            afterspace_text.i.setY((int) (currText.getY() + currText.getLayoutBounds().getHeight()));
            text.pushLine((int) afterspace_text.i.getY(), character);
            afterspace_text = afterspace_text.next;

            for (int j = character + 1; j <= index; j++) {
                afterspace_text.i.setX((int) (afterspace_text.prev.i.getX() + Math.round(afterspace_text.prev.i.getLayoutBounds().getWidth())));
                afterspace_text.i.setY(afterspace_text.prev.i.getY());
                afterspace_text = afterspace_text.next;
            }
        } else if (space_index == index) {
            currText.setX((int) (i.prev.i.getX() + Math.round(i.prev.i.getLayoutBounds().getWidth())));
            currText.setY((int) (i.prev.i.getY()));
        }
    }

    public void renderAllText() {
        int cursor = text.getRelInd();
        TextLinkedList.Node curr = text.getNode(0);

        text.get(0).setX(5);
        text.get(0).setY(0);
        text.pushLine(0, 0);
        curr = curr.next;

        space_index = -1;
        int size = text.size();

        for (int i = 1; i < size; i++) {
            Text currText = curr.i;
            currText.setTextOrigin(VPos.TOP);
            currText.setFont(Font.font(fontName, fontSize));

            int x = (int) (curr.prev.i.getX() + Math.round(curr.prev.i.getLayoutBounds().getWidth()));
            if (currText.getText().equals(" ")) {
                space_index = i;
            }
            if (x + Math.round(currText.getLayoutBounds().getWidth()) > window_width - 25
                    && Math.round(currText.getLayoutBounds().getWidth()) < window_width - 25) {
                currText.setY(curr.prev.i.getY());
                wrapLine(curr, i);
            } else if (currText.getText().equals("") && i < size - 1) {
                currText.setX(5);
                currText.setY((int) (curr.prev.i.getY() + curr.prev.i.getLayoutBounds().getHeight()));
                text.pushLine((int) currText.getY(), i);
            } else {
                currText.setY((int) curr.prev.i.getY());
                currText.setX(x);
            }
            curr = curr.next;
        }

        int xPos = (int) (text.get(cursor - 1).getX() + Math.round(text.get(cursor - 1).getLayoutBounds().getWidth()));
        if (xPos > window_width - 25) {
            tc.setX(window_width - 25);
        } else {
            tc.setX(xPos);
        }
        tc.setY(text.get(cursor - 1).getY());
        tc.setHeight(text.get(text.size() - 1).getLayoutBounds().getHeight());


        if ((text.get(text.size() - 1).getY() + text.get(text.size() - 2).getLayoutBounds().getHeight()) - window_height > 0) {
            sb.setMax(window_height);
        } else {
            sb.setMax(0);
            sb.setValue(0);
        }
        int height = (int) Math.round(text.get(text.size() - 1).getY() + text.get(text.size() - 2).getLayoutBounds().getHeight());
        if (height >= window_height && Math.round(sb.getValue()) == window_height && height + root.getLayoutY() < window_height) {
            root.setLayoutY(-Math.abs(height - window_height));
        }
        if (tc.getHeight() < window_height) {
            changeScrollBarY();
        }
    }

    public void updateWindowSize(double new_width, double new_height) {
        if (new_width < 0) {
            window_height = (int) new_height;
            sb.setPrefHeight(window_height);
            sb.setMin(0);
            if ((text.get(text.size() - 1).getY() + text.get(text.size() - 2).getLayoutBounds().getHeight()) - window_height > 0) {
                sb.setMax(window_height);
            } else {
                sb.setMax(0);
            }
        } else if (new_height < 0) {
            window_width = (int) new_width;
            int usableScreenWidth = (int) (window_width - sb.getLayoutBounds().getWidth());
            sb.setLayoutX(usableScreenWidth);
        }
        changeScrollBarY();
        renderAllText();
    }

    public void changeScrollBarY() {
        double height = text.getLast().i.getY() + text.getLast().prev.i.getLayoutBounds().getHeight();
        if (tc.getY() + root.getLayoutY() < 0) {
            root.setLayoutY( - tc.getY());
            sb.setValue( - (int) root.getLayoutY() * (double) window_height / (height - window_height));
        } else if (tc.getY() + tc.getHeight() + root.getLayoutY() > window_height) {
            root.setLayoutY( - (tc.getY() + tc.getHeight() - window_height));
            sb.setValue( - (int) root.getLayoutY() * (double) window_height / (height - window_height));
        }
    }

    public int getWindowHeight() {
        return window_height;
    }
}
