package sample.connection;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by vladstarikov on 03.05.16.
 */
public class TCPConnection extends Connection implements Runnable {

    private Socket client;

    public TCPConnection(String host, int port) {
        try {
            client = new Socket(host, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!client.isClosed() && client.isConnected()) {
            new Thread(TCPConnection.this).start();
        }
    }

    @Override
    public void run() {
        while (client.isConnected() && !client.isClosed()) {
            try {
                int available = client.getInputStream().available();
                if (available > 0) {
                    byte[] bytes = new byte[available];
                    client.getInputStream().read(bytes);
                    System.out.print(new String(bytes));
                    onReceiveListeners.stream().forEach(listener -> listener.onReceive(bytes));
                } else {
                    Thread.sleep(1);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void send(byte[] data) {
        super.send(data);
        try {
            client.getOutputStream().write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void send(String data) {
        send(data.getBytes());
    }

    public synchronized boolean isOpen() {
        return client.isConnected();
    }

    @Override
    public synchronized String getTargetName() {
        return client.getInetAddress().toString();
    }

    public synchronized void close() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
