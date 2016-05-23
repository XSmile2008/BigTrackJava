package sample.command;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by olyadowgal on 04.02.16.
 * This class used to serialize commands before it sends over serial interface
 */
public class Command {

    public static final byte[] START = {':', ':'};//TODO:
    public static final byte[] END = {'\r', '\n'};
    public static final int EMPTY_LENGTH = START.length + 2 + END.length;

    private byte key;
    private List<Argument> arguments;

    public Command(byte key) {
        this.key = key;
        this.arguments = new ArrayList<>();
    }

    public Command(byte key, List<Argument> arguments) {
        this.key = key;
        this.arguments = arguments;
    }

    public byte[] serialize() {
        int argsSize = 0;
        for (Argument argument : arguments) argsSize += argument.serialize().length;
        ByteBuffer buffer = ByteBuffer.allocate(EMPTY_LENGTH + argsSize).put(START).put(key).put((byte) arguments.size());
        for (Argument argument : arguments) {
            buffer.put(argument.serialize());
        }
        return buffer.put(END).array();
    }

    public static Command deserialize(byte[] bytes) {
        if (bytes.length < EMPTY_LENGTH) return null;
        int pos = START.length;
        byte key = bytes[pos++];
        byte argsCount = bytes[pos++];
        List<Argument> arguments = new ArrayList<>(argsCount);
        for (int i = 0; i < argsCount; i++) {
            int argSize = bytes[pos + Argument.SIZE];
            if (pos + Argument.OFFSET + argSize + END.length > bytes.length || argSize < 0) return null;
            byte[] arg = Arrays.copyOfRange(bytes, pos, pos + Argument.OFFSET + argSize);
            arguments.add(new Argument(arg));
            pos += arg.length;
        }
        if (pos + END.length != bytes.length) return null;
        return new Command(key, arguments);
    }

    public byte getKey() {
        return key;
    }

    public void setKey(byte key) {
        this.key = key;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public Command addArgument(Argument argument) {
        arguments.add(argument);
        return this;
    }

    public Argument getArgument(byte key) {
        Optional<Argument> arg = arguments.stream().filter(argument -> argument.getKey() == key).findFirst();
        return arg.isPresent() ? arg.get() : null;
    }
}
