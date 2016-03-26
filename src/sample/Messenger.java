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

/**
 * Created by vladstarikov on 25.03.16.
 */
public class Messenger extends VBox implements IOnReceiveListener, IOnSendListener{

    private Connection connection;

    @FXML private TextField textFieldOutput;
    @FXML private TextFlow textFlow;
    @FXML private ScrollPane scrollPane;

    public Messenger() {
        initFXML();
    }

    public Messenger(Connection connection) {
        initFXML();
        this.connection = connection;
    }

    private void initFXML() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("layout/messenger.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onSend(byte[] data) {
        Platform.runLater(() -> {
            Text text = new Text(new String(data));
            text.setFill(Color.DARKRED);
            textFlow.getChildren().add(text);
            scrollPane.setVvalue(scrollPane.getVmax());
        });
    }

    @Override
    public void onReceive(byte[] data) {
        Platform.runLater(() -> {//TODO: replace invisible symbols by code \n\r or \13\10
            Text text = new Text(new String(data));
            text.setFill(Color.FORESTGREEN);
            textFlow.getChildren().add(text);
            scrollPane.setVvalue(scrollPane.getVmax());
        });
    }

    @FXML
    protected void sendFromTextField(Event event) {
        if (event instanceof KeyEvent && ((KeyEvent)event).getCode() != KeyCode.ENTER) return;
        if (connection != null && connection.isOpen() && textFieldOutput.getText().length() > 0) {
            connection.send(textFieldOutput.getText());//TODO: add "\r\n"
            textFieldOutput.setText(null);
        }
    }

}
