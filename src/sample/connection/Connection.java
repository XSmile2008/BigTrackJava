package sample.connection;

/**
 * Created by vladstarikov on 06.02.16.
 */
public abstract class Connection {

    protected OnReceiveListener onReceiveListener;
    protected OnSendListener onSendListener;

    public void send(byte[] data) {
        if (onSendListener != null) {
            onSendListener.onSend(data);
        }
    }

    public void send(String data) {
        if (onSendListener != null) {
            onSendListener.onSend(data.getBytes());
        }
    }

    public void addOnReceiveListener(OnReceiveListener listener) {
        onReceiveListener = listener;
    }

    public void removeOnReceiveListener() {
        onReceiveListener = null;
    }

    public void addOnSendListener(OnSendListener listener) {
        onSendListener = listener;
    }

    public void removeOnSendListener() {
        onSendListener = null;
    }

    public abstract void close();

    public abstract boolean isOpen();

    public abstract String getTargetName();

}
