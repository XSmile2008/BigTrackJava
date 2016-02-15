package sample.command;

import java.util.Arrays;

/**
 * Created by vladstarikov on 14.02.16.
 */
public class Parser {

    public static final int NOT_FIND = -1;

    private byte[] buffer = new byte[0];

    public Command parse(byte[] data) {
        if (buffer.length == 0) {
            int start = -1;
            for (int i = 0; i < data.length; i++) {
                if (data[i] == Command.COMMAND_START[0]) {
                    start = i;
                    break;
                }
            }
            if (start != -1) buffer = Arrays.copyOfRange(data, start, data.length);
        } else {
            byte[] newBuffer = new byte[buffer.length + data.length];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            System.arraycopy(data, 0, newBuffer, buffer.length, data.length);
            buffer = newBuffer;
        }

        for (int start = searchStart(0); start != NOT_FIND; start = searchStart(start + 1)) {
            for (int end = searchEnd(start + Command.EMPTY_COMMAND_SIZE - 1); end != NOT_FIND; end = searchEnd(end + 1)) {
                Command command = Command.deserialize(Arrays.copyOfRange(buffer, start, end + 1));
                if (command != null) {
                    System.out.println(buffer.length);
                    buffer = Arrays.copyOfRange(buffer, end + (end < buffer.length - 1 ? 1 : 0), buffer.length - 1);
                    return command;
                }
            }
        }
        return null;
    }

    private int searchStart(int from) {
        if (from + Command.COMMAND_START.length > buffer.length) return NOT_FIND;
        for (int i  = from; i < buffer.length; i++) {
            int bytesMatch = 0;
            for (int j = 0; j < Command.COMMAND_START.length; j++) {
                if (buffer[i + j] == Command.COMMAND_START[j]) bytesMatch++;
            }
            if (bytesMatch == Command.COMMAND_START.length) return i;
        }
        return NOT_FIND;
    }

    private int searchEnd(int from) {
        from = from - Command.COMMAND_END.length >= 0 ? from : Command.COMMAND_END.length;
        for (int i  = from; i < buffer.length; i++) {
            int bytesMatch = 0;
            for (int j = 0; j < Command.COMMAND_END.length; j++) {
                if (buffer[i - Command.COMMAND_END.length + j + 1] == Command.COMMAND_END[j]) bytesMatch++;
            }
            if (bytesMatch == Command.COMMAND_END.length) return i;
        }
        return NOT_FIND;
    }

}
