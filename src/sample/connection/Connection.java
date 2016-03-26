package sample.connection;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by vladstarikov on 06.02.16.
 */
public abstract class Connection {

    protected Set<IOnReceiveListener> onReceiveListeners = new HashSet<>();
    protected Set<IOnSendListener> onSendListeners = new HashSet<>();

    public void send(byte[] data) {
        for (IOnSendListener onSendListener : onSendListeners) {
            onSendListener.onSend(data);
        }
    }

    public void send(String data) {
        for (IOnSendListener onSendListener : onSendListeners) {
            onSendListener.onSend(data.getBytes());
        }
    }

    public void addOnReceiveListener(IOnReceiveListener listener) {
        onReceiveListeners.add(listener);
    }

    public void removeOnReceiveListener(IOnReceiveListener listener) {
        onReceiveListeners.remove(listener);
    }

    public void addOnSendListener(IOnSendListener listener) {
        onSendListeners.add(listener);
    }

    public void removeOnSendListener(IOnSendListener listener) {
        onSendListeners.remove(listener);
    }

    public abstract void close();

    public abstract boolean isOpen();

    public abstract String getTargetName();

}
