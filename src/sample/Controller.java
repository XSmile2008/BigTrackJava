package sample;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import sample.CompassView.CompassView;
import sample.command.*;
import sample.connection.Connection;
import sample.connection.OnReceiveListener;
import sample.connection.OnSendListener;
import sample.connection.SerialConnection;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable, OnSendListener, OnReceiveListener {

    public static final int WIFI = 1;
    public static final int SERIAL = 2;

    @FXML Button buttonStop;
    @FXML Button buttonForward;
    @FXML Button buttonBackward;
    @FXML Button buttonLeft;
    @FXML Button buttonRight;
    @FXML Slider sliderAzimuth;

    @FXML Menu menuSerialPort;
    @FXML Menu menuBaudRate;

    @FXML MenuItem menuItemConnect;
    @FXML MenuItem menuItemDisconnect;

    @FXML ToggleGroup toggleGroupConnectionType;
    ToggleGroup toggleGroupSerialPort;
    ToggleGroup toggleGroupBaudRate;

    @FXML Label labelConnectionStatus;

    @FXML TextField textFieldOutput;
    @FXML ScrollPane textFlowContainer;

    @FXML VBox vBoxLeft;

    CompassView compass;

    Connection connection;
    int connectionType = SERIAL;

    Parser commandParser = new Parser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Toggle group connection type
        toggleGroupConnectionType.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (((RadioMenuItem) newValue).getText().equals("Wi-Fi")) connectionType = WIFI;
                else if (((RadioMenuItem) newValue).getText().equals("Serial")) connectionType = SERIAL;
                menuSerialPort.setVisible(connectionType == SERIAL);
                menuBaudRate.setVisible(connectionType == SERIAL);
            }
        });

        ChangeListener<Toggle> needToReconnectListener = (observable, oldValue, newValue) -> {
            if (newValue != null && connection != null && connection.isOpen())
                connect();
        };

        //Toggle group baud rate
        toggleGroupBaudRate = new ToggleGroup();
        toggleGroupBaudRate.selectedToggleProperty().addListener(needToReconnectListener);
        for (int baudRate : new int[]{9600, 57600, 74880, 115200}) {
            RadioMenuItem radioMenuItem = new RadioMenuItem(String.valueOf(baudRate));
            radioMenuItem.setToggleGroup(toggleGroupBaudRate);
            radioMenuItem.setUserData(baudRate);
            radioMenuItem.setSelected(baudRate == 115200);
            menuBaudRate.getItems().add(radioMenuItem);
        }

        //Toggle grout serial port
        toggleGroupSerialPort = new ToggleGroup();
        toggleGroupSerialPort.selectedToggleProperty().addListener(needToReconnectListener);
        SerialPort defaultSerialPort = SerialConnection.getDefaultSerialPort();
        for (SerialPort serialPort : SerialConnection.getAvailableSerialPorts()) {
            RadioMenuItem radioMenuItem = new RadioMenuItem(serialPort.getSystemPortName());
            radioMenuItem.setToggleGroup(toggleGroupSerialPort);
            radioMenuItem.setUserData(serialPort);
            radioMenuItem.setSelected(serialPort.getSystemPortName().equals(defaultSerialPort != null ? defaultSerialPort.getSystemPortName() : null));
            menuSerialPort.getItems().add(radioMenuItem);
        }
        if (defaultSerialPort == null) {
            List<Toggle> toggleList = toggleGroupSerialPort.getToggles();
            if (toggleList.size() > 0) toggleList.get(0).setSelected(true);
        }

        EventHandler<ActionEvent> moveEventHandler = event -> {
            if (connection != null && connection.isOpen()) {
                Command command = null;
                if (event.getSource().equals(buttonStop)) {
                    command = new Command(Commands.STOP);
                } else if (event.getSource().equals(buttonForward)) {
                    command = new Command(Commands.MOVE).addArgument(new Argument(Commands.DIRACTION, Commands.FORWARD));
                } else if (event.getSource().equals(buttonBackward)) {
                    command = new Command(Commands.MOVE).addArgument(new Argument(Commands.DIRACTION, Commands.BACKWARD));
                } else if (event.getSource().equals(buttonLeft)) {
                    command = new Command(Commands.ROTATE).addArgument(new Argument(Commands.DIRACTION, Commands.LEFT));
                } else if (event.getSource().equals(buttonRight)) {
                    command = new Command(Commands.ROTATE).addArgument(new Argument(Commands.DIRACTION, Commands.RIGHT));
                }
                System.out.println(Arrays.toString(command.serialize()));
                connection.send(command.serialize());
            }
        };
        buttonStop.setOnAction(moveEventHandler);
        buttonForward.setOnAction(moveEventHandler);
        buttonBackward.setOnAction(moveEventHandler);
        buttonLeft.setOnAction(moveEventHandler);
        buttonRight.setOnAction(moveEventHandler);
        sliderAzimuth.valueProperty().addListener((observable, oldValue, newValue) -> {
            Command command = new Command(Commands.ROTATE).addArgument(new Argument(Commands.AZIMUTH, newValue.shortValue()));
            if (connection != null && connection.isOpen()) connection.send(command.serialize());
            compass.setAzimuth(newValue.doubleValue());

        });

        vBoxLeft.getChildren().add(compass = new CompassView());
    }

    @FXML
    protected void connect() {
        if (connection != null) connection.close();
        if (connectionType == WIFI) {
            System.out.println("Coming soon...");
        } else {
            SerialPort serialPort = (SerialPort) toggleGroupSerialPort.getSelectedToggle().getUserData();
            int baudRate = (Integer) toggleGroupBaudRate.getSelectedToggle().getUserData();
            connection = new SerialConnection(serialPort, baudRate);
            connection.addOnSendListener(this);
            connection.addOnReceiveListener(this);
            labelConnectionStatus.setTextFill(Color.FORESTGREEN);
            labelConnectionStatus.setText("connected to " + connection.getTargetName());
        }
    }

    @FXML
    protected void disconnect() {
        if (connection != null) connection.close();
        labelConnectionStatus.setTextFill(Color.DARKRED);
        labelConnectionStatus.setText("not connected");
    }

    @FXML
    protected void sendFromTextField(Event event) {
        if (event instanceof KeyEvent && ((KeyEvent)event).getCode() != KeyCode.ENTER) return;
        if (connection != null && connection.isOpen() && textFieldOutput.getText().length() > 0) {
            connection.send(textFieldOutput.getText());//TODO: add "\r\n"
            textFieldOutput.setText(null);
        }
    }

    @Override
    public void onSend(byte[] data) {
        Platform.runLater(() -> {
            Text text = new Text(new String(data));
            text.setFill(Color.DARKRED);
            ((TextFlow)textFlowContainer.getContent()).getChildren().add(text);
            textFlowContainer.setVvalue(textFlowContainer.getVmax());
        });
    }

    @Override
    public void onReceive(byte[] data) {
        for (Command command : commandParser.parse(data)) {
            System.out.println(Arrays.toString(command.serialize()));
            if (command.getKey() == 'T' && command.getArguments().size() > 0) {
                try {
                    compass.setAzimuth(command.getArguments().get(0).getFloat());
                } catch (ValueSizeException e) {
                    e.printStackTrace();
                }
            }
        }
        Platform.runLater(() -> {
            Text text = new Text(new String(data));
            text.setFill(Color.FORESTGREEN);
            ((TextFlow)textFlowContainer.getContent()).getChildren().add(text);
            textFlowContainer.setVvalue(textFlowContainer.getVmax());
        });
    }
}