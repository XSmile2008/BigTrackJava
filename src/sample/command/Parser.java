package sample.command;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vladstarikov on 14.02.16.
 */
public class Parser {

    //TODO: in data array may be few commands! Return List of Commands

    public static final int NOT_FIND = -1;

    private byte[] buffer = new byte[0];

    public List<Command> parse(byte[] data) {
        if (buffer.length == 0) {
            int start = searchStart(data, 0);
            if (start != NOT_FIND) buffer = Arrays.copyOfRange(data, start, data.length);//TODO: check
        } else {
            byte[] newBuffer = new byte[buffer.length + data.length];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            System.arraycopy(data, 0, newBuffer, buffer.length, data.length);
            buffer = newBuffer;
        }

        List<Command> commands = new ArrayList<>();
        while (true) {
            System.out.println(buffer.length);
            Command command = searchCommand();
            if (command != null) {
                commands.add(command);
                System.out.println(Arrays.toString(command.serialize()));
            } else break;
        }
        return commands;
    }

    /** used for trim buffer
     *  to cut off unused data from start of @buffer,
     *  and delete already parsed commands
     *  @param from search from this position to find new command start, if not find delete all buffer
     */
    private void trim(int from) {
        int start = searchStart(buffer, from);
        if (start == NOT_FIND) buffer = new byte[0];
        else if (start != 0) buffer = Arrays.copyOfRange(buffer, start, buffer.length);
    }

    private int searchStart(byte[] buffer, int from) {//TODO: use this to find parts of start
        //if (from + Command.COMMAND_START.length > buffer.length) return NOT_FIND;//TODO: may be useful, add bool parameter
        for (int i  = from; i < buffer.length; i++) {
            if (buffer[i] == Command.COMMAND_START[0]) {
                int errors = 0;
                int overflow = (i + Command.COMMAND_START.length) - buffer.length;
                for (int j = 1; j < Command.COMMAND_START.length - (overflow > 0 ? overflow : 0); j++) {//TODO: check range
                    if (buffer[i + j] != Command.COMMAND_START[j]) errors++;
                }
                if (errors == 0) return i;
            }
        }
        return NOT_FIND;
    }

    private int searchEnd(byte[] buffer, int from) {
        from = from - Command.COMMAND_END.length >= 0 ? from : Command.COMMAND_END.length;
        for (int i  = from; i < buffer.length; i++) {
            if (buffer[i] == Command.COMMAND_END[Command.COMMAND_END.length - 1]) {
                int bytesMatch = 0;
                for (int j = 0; j < Command.COMMAND_END.length; j++) {
                    if (buffer[i - Command.COMMAND_END.length + j + 1] == Command.COMMAND_END[j]) bytesMatch++;
                }
                if (bytesMatch == Command.COMMAND_END.length) return i;
            }
        }
        return NOT_FIND;
    }

    private Command searchCommand() {
        for (int start = searchStart(buffer, 0); start != NOT_FIND; start = searchStart(buffer, start + 1)) {
            for (int end = searchEnd(buffer, start + Command.EMPTY_COMMAND_LENGTH - 1); end != NOT_FIND; end = searchEnd(buffer, end + 1)) {
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
