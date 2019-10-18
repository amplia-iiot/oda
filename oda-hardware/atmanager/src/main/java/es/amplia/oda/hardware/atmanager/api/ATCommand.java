package es.amplia.oda.hardware.atmanager.api;

import lombok.Value;

import java.util.Arrays;
import java.util.Collections;
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

    public static ATCommand emptyCommand() {
        return new ATCommand(ATCommandType.ACTION, "", true, false, null);
    }

    public static ATCommand basicCommand(char c) {
        c = Character.toUpperCase(c);
        if (c == 'S') {
            throw new IllegalArgumentException("Char at basic command can not be 'S'");
        }
        return new ATCommand(ATCommandType.ACTION, "" + c, true, false, Collections.singletonList("0"));
    }

    public static ATCommand basicCommand(char c, int parameter) {
        c = Character.toUpperCase(c);
        if (c == 'S') {
            throw new IllegalArgumentException("Char at basic command can not be 'S'");
        }
        return new ATCommand(ATCommandType.ACTION, "" + c, true, false,
                Collections.singletonList(String.valueOf(parameter)));
    }

    public static ATCommand ampersandCommand(char c) {
        c = Character.toUpperCase(c);
        if (c == 'S') {
            throw new IllegalArgumentException("Char at ampersand command can not be 'S'");
        }
        return new ATCommand(ATCommandType.ACTION, "&" + c, true, false, null);
    }

    public static ATCommand ampersandCommand(char c, int parameter) {
        c = Character.toUpperCase(c);
        if (c == 'S') {
            throw new IllegalArgumentException("Char at ampersand command can not be 'S'");
        }
        return new ATCommand(ATCommandType.ACTION, "&" + c, true, false,
                Collections.singletonList(String.valueOf(parameter)));
    }

    public static ATCommand sReadCommand(int register) {
        return new ATCommand(ATCommandType.READ, "S" + register, true, true, null);
    }

    public static ATCommand sSetCommand(int register, int value) {
        return new ATCommand(ATCommandType.SET, "S" + register, true, true,
                Collections.singletonList(String.valueOf(value)));
    }

    public static ATCommand extendedCommand(ATCommandType type, String cmd) {
        if (cmd.length() <= 1 || cmd.charAt(0) != '+') {
            throw new IllegalArgumentException("Command length should be more than 1 and starts with '+'");
        }
        char c = Character.toUpperCase(cmd.charAt(1));
        assert (c >= 'A' && c <= 'Z');
        return new ATCommand(type, cmd, false, false, null);
    }

    public static ATCommand extendedSetCommand(String cmd, String... parameters) {
        return new ATCommand(ATCommandType.SET, cmd, false, false, Arrays.asList(parameters));
    }

    public static ATCommand extendedSetCommand(String cmd, List<String> parameters) {
        return new ATCommand(ATCommandType.SET, cmd, false, false, parameters);
    }

    public String asWireString() {
        StringBuilder resp = new StringBuilder();
        resp.append(command);
        if (isBasic) {
            appendBasicCommand(resp);
        }
        if (type == ATCommandType.READ) {
            appendReadCommand(resp);
        } else if (type == ATCommandType.TEST) {
            appendTestCommand(resp);
        } else if (!isBasic && type == ATCommandType.SET) {
            appendSetCommand(resp);
        }
        return resp.toString();
    }

    private void appendBasicCommand(StringBuilder resp) {
        if (isS && type == ATCommandType.SET) {
            resp.append('=');
            resp.append(parameters.get(0));
        } else if (parameters != null) {
            resp.append(parameters.get(0));
        }
    }

    private void appendReadCommand(StringBuilder resp) {
        resp.append('?');
    }

    private void appendTestCommand(StringBuilder resp) {
        resp.append("=?");
    }

    private void appendSetCommand(StringBuilder resp) {
        resp.append('=');
        String p = parameters.stream()
                .map(v -> {
                    if (v.isEmpty()) return "";
                    if (v.matches("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}")) return v;
                    if (v.matches("\\d+.\\d+")) return v;
                    if (v.matches("\\d+")) return v;
                    if (v.matches("[A-F0-9]+")) return v;
                    if (v.matches("[A-Z]+")) return v;
                    return "\"" + v + "\"";
                })
                .collect(Collectors.joining(","));
        resp.append(p);
    }
}
