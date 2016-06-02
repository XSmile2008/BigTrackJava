package sample.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vladstarikov on 14.02.16.
 */
public class Parser {

    private static final int NOT_FIND = -1;

    private int maxBufferLength;
    private byte[] buffer = new byte[0];

    public Parser() {
        maxBufferLength = 128;
    }

    public Parser(int maxBufferLength) {
        this.maxBufferLength = maxBufferLength;
    }

    public List<Command> parse(byte[] data) {
        if (buffer.length == 0) {
            int start = searchStart(data, 0);
            if (start != NOT_FIND) buffer = Arrays.copyOfRange(data, start, data.length);//TODO: check
        } else {
            if (buffer.length > maxBufferLength) trim(buffer.length / 2);//TODO: test this!
            byte[] newBuffer = new byte[buffer.length + data.length];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            System.arraycopy(data, 0, newBuffer, buffer.length, data.length);
            buffer = newBuffer;
        }

        List<Command> commands = new ArrayList<>();
        while (true) {
            System.out.println("Buffer length = " + buffer.length);//TODO: remove
            Command command = searchCommand();
            if (command != null) commands.add(command);
            else break;
        }
        return commands;
    }

    /**
     * used for trim buffer
     * to cut off unused data from start of @buffer,
     * and delete already parsed commands
     *
     * @param from search from this position to find new command start, if not find delete all buffer
     */
    private void trim(int from) {
        int start = searchStart(buffer, from);
        if (start == NOT_FIND) buffer = new byte[0];
        else if (start != 0) buffer = Arrays.copyOfRange(buffer, start, buffer.length);
    }

    private int searchStart(byte[] buffer, int from) {
        //if (from + Command.START.length > buffer.length) return NOT_FIND;//TODO: may be useful, add bool parameter
        for (int i = from; i < buffer.length; i++) {
            if (buffer[i] == Command.START[0]) {
                int errors = 0;
                int overflow = (i + Command.START.length) - buffer.length;
                for (int j = 1; j < Command.START.length - (overflow > 0 ? overflow : 0); j++) {
                    if (buffer[i + j] != Command.START[j]) errors++;
                }
                if (errors == 0) return i;
            }
        }
        return NOT_FIND;
    }

    private int searchEnd(byte[] buffer, int from) {//TODO: check this
//        from = from - Command.END.length >= 0 ? from : Command.END.length;
        for (int i = from; i < buffer.length; i++) {
            if (buffer[i] == Command.END[Command.END.length - 1]) {
                int errors = 0;
                for (int j = 0; j < Command.END.length; j++) {
                    if (buffer[i - Command.END.length + j + 1] != Command.END[j]) errors++;
                }
                if (errors == 0) return i;
            }
        }
        return NOT_FIND;
    }

    private Command searchCommand() {
        for (int start = searchStart(buffer, 0); start != NOT_FIND; start = searchStart(buffer, start + 1)) {
            for (int end = searchEnd(buffer, start + Command.EMPTY_LENGTH - 1); end != NOT_FIND; end = searchEnd(buffer, end + 1)) {
                Command command = Command.deserialize(Arrays.copyOfRange(buffer, start, end + 1));
                if (command != null) {
                    trim(end + 1);
                    return command;
                }
            }
        }
        return null;
    }

}
