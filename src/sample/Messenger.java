package sample;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
 * Created by vladstarikov on 25.03.16.
 */
public class Messenger extends VBox implements IOnReceiveListener, IOnSendListener {

    private Connection connection;

    @FXML private TextField textFieldOutput;
    @FXML private TextFlow textFlow;
    @FXML private ScrollPane scrollPane;

    public Messenger() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("punyan/messenger.fxml"));
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
    }

    @Override
    public void onSend(byte[] data) {
        Platform.runLater(() -> {
            textFlow.getChildren().addAll(toPrintable(data, Color.DARKRED, Color.DARKORANGE));
            scrollPane.setVvalue(scrollPane.getVmax());
        });
    }

    @Override
    public void onReceive(byte[] data) {
        Platform.runLater(() -> {//TODO: replace invisible symbols by code \n\r or \13\10
            textFlow.getChildren().addAll(toPrintable(data, Color.FORESTGREEN, Color.DARKORANGE));
            scrollPane.setVvalue(scrollPane.getVmax());
        });
    }

    @FXML
    protected void sendFromTextField(Event event) {
        if (event instanceof KeyEvent && ((KeyEvent) event).getCode() != KeyCode.ENTER) return;
        if (connection != null && connection.isOpen() && textFieldOutput.getText().length() > 0) {
            connection.send(textFieldOutput.getText());//TODO: add "\r\n"
            textFieldOutput.setText(null);
        }
    }

    @FXML
    protected void clear() {
        textFlow.getChildren().clear();
    }

    private List<Text> toPrintable(byte[] data, Color plaintext, Color invisible) {
        List<Text> result = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (byte b : data) {
            if ((b >= 32 && b <= 126) || b == 10 || b == 13) {
                builder.append((char) b);
            } else {
                if (builder.length() > 0) {
                    Text text = new Text(builder.toString());
                    text.setFill(plaintext);
                    result.add(text);
                    builder.delete(0, builder.length());
                }
                Text text = new Text("\\" + b);
                text.setFill(invisible);
                result.add(text);
            }
        }
        if (builder.length() > 0) {
            Text text = new Text(builder.toString());
            text.setFill(plaintext);
            result.add(text);
        }
        return result;
    }

}
