package sample.connection;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vladstarikov on 06.02.16.
 * This is wrapper class for jSerialComm
 */
public class SerialConnection extends Connection {

    SerialPort serialPort;

    public SerialConnection(SerialPort serialPort, int baudRate) {
        this.serialPort = serialPort;
        serialPort.setBaudRate(baudRate);
        if (serialPort.openPort()) {
            System.out.println("Open port " + serialPort.getSystemPortName());
            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent serialPortEvent) {
                    if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
                    byte[] newData = new byte[serialPortEvent.getSerialPort().bytesAvailable()];
                    serialPortEvent.getSerialPort().readBytes(newData, newData.length);
                    for (IOnReceiveListener onReceiveListener : onReceiveListeners) {
                        onReceiveListener.onReceive(newData);
                    }
                }
            });
        } else {
            System.out.printf("Can't open port " + serialPort.getSystemPortName());
        }
    }

    @Override
    public void send(byte[] data) {
        super.send(data);
        serialPort.writeBytes(data, data.length);
    }

    @Override
    public void send(String data) {
        super.send(data);
        serialPort.writeBytes(data.getBytes(), data.length());//TODO: check length
    }

    @Override
    public void close() {
        serialPort.closePort();
    }

    @Override
    public boolean isOpen() {
        return serialPort.isOpen();
    }

    @Override
    public String getTargetName() {
        return serialPort.getSystemPortName();
    }

    public static SerialPort getDefaultSerialPort() {
        for (SerialPort serialPort : SerialPort.getCommPorts()) {
            if (serialPort.getSystemPortName().contains("cu.wchusbserial")) return serialPort;
        }
        return null;
    }

    public static List<SerialPort> getAvailableSerialPorts() {
        List<SerialPort> serialPorts = new ArrayList<>();
        for (SerialPort serialPort : SerialPort.getCommPorts()) {
            if (serialPort.getSystemPortName().contains("cu.")) serialPorts.add(serialPort);
        }
        return serialPorts;
    }

}
