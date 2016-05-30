package sample;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import sample.command.*;
import sample.connection.*;
import sample.view.CompassView;
import sample.view.Stick;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @FXML Label labelLifetime, labelFreeMemory;
    @FXML Label labelConnectionStatus;

    @FXML Messenger messenger = new Messenger();
    @FXML CompassView compass;
    @FXML Stick stick;

    private Connection connection;
    private int connectionType = SERIAL;
    private Parser commandParser = new Parser();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initConnectionOptions();
        initMotionControl();
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

    private void initMotionControl() {
        stick.setOnControlListener((x, y) -> {
            Command command = new Command(Commands.MOVE);
            command.addArgument(new Argument((byte) 'x', x));
            command.addArgument(new Argument((byte) 'y', y));
            if (connection != null) connection.send(command.serialize());
        });
        sliderAzimuth.valueProperty().addListener((observable, oldValue, newValue) -> {
            Command command = new Command(Commands.ROTATE).addArgument(new Argument(Commands.AZIMUTH, newValue.shortValue()));
            if (connection != null && connection.isOpen()) connection.send(command.serialize());
            compass.setAzimuth(newValue.doubleValue());

        });
        //Motion control
        EventHandler<javafx.event.ActionEvent> moveEventHandler = event -> {
            if (connection != null && connection.isOpen()) {
                short x = 0, y = 0;
                if (event.getSource().equals(buttonForward)) y = 255;
                else if (event.getSource().equals(buttonBackward)) y = -255;
                else if (event.getSource().equals(buttonLeft)) x = -255;
                else if (event.getSource().equals(buttonRight)) x = 255;
                Command command = new Command(Commands.MOVE);
                command.addArgument(new Argument((byte) 'x', x));
                command.addArgument(new Argument((byte) 'y', y));
                System.out.println(Arrays.toString(command.serialize()));
                connection.send(command.serialize());
            }
        };
        buttonStop.setOnAction(moveEventHandler);
        buttonForward.setOnAction(moveEventHandler);
        buttonBackward.setOnAction(moveEventHandler);
        buttonLeft.setOnAction(moveEventHandler);
        buttonRight.setOnAction(moveEventHandler);
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
        if (connection.isOpen()) {
            connection.addOnSendListener(this);
            connection.addOnReceiveListener(this);
            connection.addOnSendListener(messenger);
            connection.addOnReceiveListener(messenger);
            messenger.setConnection(connection);
            labelConnectionStatus.setTextFill(Color.FORESTGREEN);
            labelConnectionStatus.setText("connected to " + connection.getTargetName());
        } else {
            labelConnectionStatus.setTextFill(Color.DARKRED);
            labelConnectionStatus.setText("connection error");
        }
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
            try {
                switch (command.getKey()) {
                    case Commands.TELEMETRY: {
                        compass.setAzimuth(command.getArguments().get(0).getFloat());
                        long time = Integer.toUnsignedLong(command.getArgument(Commands.TIME).getInt());
                        int mem = command.getArgument(Commands.MEMORY).getShort();
                        Platform.runLater(() -> {
                            labelLifetime.setText(dateFormat.format(new Date(time)));
                            labelFreeMemory.setText(String.valueOf(mem));
                        });
                        break;
                    }
                    case Commands.SONAR: {
                        int azimuth = command.getArgument(Commands.AZIMUTH).getShort();
                        int distance = command.getArgument(Commands.DISTANCE).getShort();
                        long time = Integer.toUnsignedLong(command.getArgument(Commands.TIME).getInt());
                        System.out.println(time + ": azimuth = " + azimuth + ", distance = " + distance);
                        Platform.runLater(() -> compass.drawPoint(azimuth, distance));
                    }
                }
            } catch (ValueSizeException e) {
                e.printStackTrace();
            }
        }
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
}