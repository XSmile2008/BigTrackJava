package sample;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import sample.command.*;
import sample.connection.*;
import sample.view.CompassView;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable, IOnSendListener, IOnReceiveListener {

    private static final int WIFI = 1;
    private static final int SERIAL = 2;

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
    private ToggleGroup toggleGroupSerialPort;
    private ToggleGroup toggleGroupBaudRate;

    @FXML Label labelConnectionStatus;

    @FXML VBox vBoxLeft;

    @FXML Messenger messenger = new Messenger();
    private CompassView compass;

    private Connection connection;
    private int connectionType = SERIAL;
    private Parser commandParser = new Parser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initConnectionOptions();
        initMotionControl();
        vBoxLeft.getChildren().add(compass = new CompassView());
    }

    private void initConnectionOptions() {
        //Toggle group connection type
        toggleGroupConnectionType.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (((RadioMenuItem) newValue).getText().equals("Wi-Fi")) {
                    connectionType = WIFI;
                } else if (((RadioMenuItem) newValue).getText().equals("Serial")) {
                    connectionType = SERIAL;
                    updateSerialPorts();
                }
                menuSerialPort.setVisible(connectionType == SERIAL);
                menuBaudRate.setVisible(connectionType == SERIAL);
            }
        });

        ChangeListener<Toggle> needToReconnectListener = (observable, oldValue, newValue) -> {
            if (newValue != null && connection != null && connection.isOpen()) connect();
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
        updateSerialPorts();
    }

    private void updateSerialPorts() {
        menuSerialPort.getItems().clear();
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
    }

    private void initMotionControl() {
        //Motion control
        EventHandler<ActionEvent> moveEventHandler = event -> {
            if (connection != null && connection.isOpen()) {
                Command command = null;
                if (event.getSource().equals(buttonStop)) {
                    command = new Command(Commands.MOVE).addArgument(new Argument(Commands.STOP));
                } else if (event.getSource().equals(buttonForward)) {
                    command = new Command(Commands.MOVE).addArgument(new Argument(Commands.DIRACTION, Commands.FORWARD));
                } else if (event.getSource().equals(buttonBackward)) {
                    command = new Command(Commands.MOVE).addArgument(new Argument(Commands.DIRACTION, Commands.BACKWARD));
                } else if (event.getSource().equals(buttonLeft)) {
                    command = new Command(Commands.ROTATE).addArgument(new Argument(Commands.DIRACTION, Commands.LEFT));
                } else if (event.getSource().equals(buttonRight)) {
                    command = new Command(Commands.ROTATE).addArgument(new Argument(Commands.DIRACTION, Commands.RIGHT));
                }
                if (command != null) {
                    System.out.println(Arrays.toString(command.serialize()));
                    connection.send(command.serialize());
                }
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
    }

    @FXML
    protected void connect() {
        if (connection != null) connection.close();
        if (connectionType == WIFI) {
            System.out.println("Coming soon...");
            connection = new TCPConnection("esp8266.local", 23);
        } else {
            SerialPort serialPort = (SerialPort) toggleGroupSerialPort.getSelectedToggle().getUserData();
            int baudRate = (Integer) toggleGroupBaudRate.getSelectedToggle().getUserData();
            connection = new SerialConnection(serialPort, baudRate);
        }
        connection.addOnSendListener(this);
        connection.addOnReceiveListener(this);
        connection.addOnSendListener(messenger);
        connection.addOnReceiveListener(messenger);
        messenger.setConnection(connection);
        labelConnectionStatus.setTextFill(Color.FORESTGREEN);
        labelConnectionStatus.setText("connected to " + connection.getTargetName());
    }

    @FXML
    protected void disconnect() {
        if (connection != null) connection.close();
        labelConnectionStatus.setTextFill(Color.DARKRED);
        labelConnectionStatus.setText("not connected");
    }

    @Override
    public void onSend(byte[] data) {

    }

    @Override
    public void onReceive(byte[] data) {
        for (Command command : commandParser.parse(data)) {
//            System.out.println(Arrays.toString(command.serialize()));
            switch (command.getKey()) {
                case Commands.TELEMETRY:
                    try {
                        compass.setAzimuth(command.getArguments().get(0).getFloat());
                    } catch (ValueSizeException e) {
                        e.printStackTrace();
                    }
                    break;
                case Commands.SONAR:
                    try {
                        int azimuth = command.getArgument(Commands.AZIMUTH).getShort();
                        int distance = command.getArgument(Commands.DISTANCE).getShort();
                        long time = Integer.toUnsignedLong(command.getArgument(Commands.TIME).getInt());
                        System.out.println(time + ": azimuth = " + azimuth + ", distance = " + distance);
                        Platform.runLater(() -> compass.drawPoint(azimuth, distance));
                    } catch (ValueSizeException e) {
                        e.printStackTrace();
                    }
            }
        }
    }
}