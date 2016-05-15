package sample;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import sample.connection.Connection;
import sample.connection.IOnReceiveListener;
import sample.connection.IOnSendListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by olga_dowgal on 25.03.16.
 */
public class Messenger extends VBox implements IOnReceiveListener, IOnSendListener {

    private static final int MAX_SIZE = 2048;

    private Connection connection;

    @FXML private TextField textFieldOutput;
    @FXML private TextFlow textFlow;
    @FXML private ScrollPane scrollPane;
    @FXML private CheckBox checkBoxScroll;

    public Messenger() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("messenger.fxml"));
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public Messenger(Connection connection) {
        this.connection = connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        textFlow.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (checkBoxScroll.isSelected()) {
                scrollPane.setVvalue(scrollPane.getVmax());
            }
        });
    }

    @Override
    public void onSend(byte[] data) {
        add(toPrintable(data, Color.DARKRED, Color.DARKORANGE));
    }

    @Override
    public void onReceive(byte[] data) {//TODO: replace invisible symbols by code \n\r or \13\10
        add(toPrintable(data, Color.FORESTGREEN, Color.DARKORANGE));
    }

    @FXML
    protected void sendFromTextField(Event event) {
        if (event instanceof KeyEvent && ((KeyEvent) event).getCode() != KeyCode.ENTER) return;
        if (connection != null && connection.isOpen() && textFieldOutput.getText().length() > 0) {
            connection.send(textFieldOutput.getText() + "\r\n");
            textFieldOutput.clear();
        }
    }

    @FXML
    protected void clear() {
        textFlow.getChildren().clear();
    }

    private void add(List<Text> texts) {
        Platform.runLater(() -> {
            textFlow.getChildren().addAll(texts);
            if (textFlow.getChildren().size() > MAX_SIZE) {
                textFlow.getChildren().remove(0, textFlow.getChildren().size() - MAX_SIZE);
            }
        });
    }

    private List<Text> toPrintable(byte[] data, Color plaintext, Color invisible) {
        List<Text> result = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (byte b : data) {
            if ((b >= 32 && b <= 126) || b == 10 || b == 13) {
                builder.append((char) b);
            } else {
                if (builder.length() > 0) {
                    result.add(getStyledText(builder.toString(), plaintext));
                    builder.delete(0, builder.length());
                }
                result.add(getStyledText("\\" + Byte.toUnsignedInt(b), invisible));
            }
        }
        if (builder.length() > 0) result.add(getStyledText(builder.toString(), plaintext));
        return result;
    }

    private Text getStyledText(String s, Color fill) {
        Text text = new Text(s);
        text.setFill(fill);
//        text.setFont(Font.font("Monospaced"));
        return text;
    }

}
