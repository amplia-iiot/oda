package es.amplia.oda.hardware.atmanager.api;

import lombok.Value;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class ATCommand {
    private final ATCommandType type;
    private final String command;
    private final List<String> parameters;
    private final boolean isBasic;
    private final boolean isS;

    private ATCommand(ATCommandType type, String command, boolean isBasic, boolean isS, List<String> params) {
        this.type = type;
        this.command = command;
        this.parameters = params;
        this.isBasic = isBasic;
        this.isS = isS;
    }

    static public ATCommand emptyCommand() {
        return new ATCommand(ATCommandType.ACTION, "", true, false, null);
    }

    static public ATCommand basicCommand(char c) {
        c = Character.toUpperCase(c);
        assert (c != 'S');
        return new ATCommand(ATCommandType.ACTION, "" + c, true, false, Arrays.asList("0"));
    }

    static public ATCommand basicCommand(char c, int parameter) {
        c = Character.toUpperCase(c);
        assert (c != 'S');
        return new ATCommand(ATCommandType.ACTION, "" + c, true, false, Arrays.asList(String.valueOf(parameter)));
    }

    static public ATCommand ampersandCommand(char c) {
        c = Character.toUpperCase(c);
        assert (c != 'S');
        return new ATCommand(ATCommandType.ACTION, "&" + c, true, false, null);
    }

    static public ATCommand ampersandCommand(char c, int parameter) {
        c = Character.toUpperCase(c);
        assert (c != 'S');
        return new ATCommand(ATCommandType.ACTION, "&" + c, true, false, Arrays.asList(String.valueOf(parameter)));
    }

    static public ATCommand sReadCommand(int register) {
        return new ATCommand(ATCommandType.READ, "S" + String.valueOf(register), true, true, null);
    }

    static public ATCommand sSetCommand(int register, int value) {
        return new ATCommand(ATCommandType.SET, "S" + String.valueOf(register), true, true, Arrays.asList(String.valueOf(value)));
    }

    static public ATCommand extendedCommand(ATCommandType type, String cmd) {
        assert (cmd.length() > 1 && cmd.charAt(0) == '+');
        char c = Character.toUpperCase(cmd.charAt(1));
        assert (c >= 'A' && c <= 'Z');
        return new ATCommand(type, cmd, false, false, null);
    }

    static public ATCommand extendedSetCommand(String cmd, String... parameters) {
        return new ATCommand(ATCommandType.SET, cmd, false, false, Arrays.asList(parameters));
    }

    static public ATCommand extendedSetCommand(String cmd, List<String> parameters) {
        return new ATCommand(ATCommandType.SET, cmd, false, false, parameters);
    }

    public String asWireString() {
        StringBuilder resp = new StringBuilder();
        resp.append(command);
        if (isBasic) {
            if (isS && type == ATCommandType.SET) {
                resp.append('=');
                resp.append(parameters.get(0));
            } else if (parameters != null) {
                resp.append(parameters.get(0));
            }
        }
        if (type == ATCommandType.READ) {
            resp.append('?');
        } else if (type == ATCommandType.TEST) {
            resp.append("=?");
        } else if (!isBasic && type == ATCommandType.SET) {
            resp.append('=');
            String p = parameters.stream()
                    .map(v -> {
                        if (v.isEmpty()) return "";
                        if (v.matches("\\d+")) return v;
                        if (v.matches("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}")) return v;
                        if (v.matches("[A-F0-9]+")) return v;
                        if (v.matches("[A-Z]+")) return v;
                        return "\"" + v + "\"";
                    })
                    .collect(Collectors.joining(","));
            resp.append(p);
        }
        return resp.toString();
    }
}
