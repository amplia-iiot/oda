package es.amplia.oda.hardware.atmanager;

import es.amplia.oda.hardware.atmanager.api.ATCommand;
import es.amplia.oda.hardware.atmanager.api.ATEvent;
import es.amplia.oda.hardware.atmanager.grammar.CommandsGrammar;
import es.amplia.oda.hardware.atmanager.grammar.GrammarException;
import es.amplia.oda.hardware.atmanager.grammar.ResponseGrammar;
import lombok.Value;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ATParserImpl implements ATParser {
    
    private static final Pattern ERROR_PATTERN = Pattern.compile("(?i)^(\\+CME )? *ERROR:? *\"?([^\"]*)\"?$");
    private static final int GROUP_NUMBER_IN_PATTERN_FOR_ERROR_TEXT = 2;
    
    private boolean inResponseMode = false;
    private String responseModeCommandName = "";

    private static String clean(String line) {
        int index = 0;
        while (index < line.length() && (line.charAt(index) == ' ' || line.charAt(index) == '\r'))
            index++;

        return line.substring(index);
    }

    private static Result parseCommand(String line) {
        if (line.equals("")) {
            return Result.empty();
        }
        try {
            List<ATCommand> commandsRead = new CommandsGrammar().parse(line.substring(2));
            return Result.commands(commandsRead);
        } catch (GrammarException e) {
            return Result.error(e.getMessage());
        }
    }

    @Override
    public Result process(String line) {
        if (inResponseMode) {
            return processInResponseMode(line);
        } else {
            return processInNormalMode(line);
        }
    }

    private Result processInNormalMode(String line) {
        line = clean(line);
        if (line.length() >= 2 && line.substring(0, 2).equalsIgnoreCase("AT")) {
            return parseCommand(line);
        }
        if (line.startsWith("+")) {
            return parseResponse(line);
        }
        if (line.equals("")) {
            return Result.empty();
        }
        return Result.error("Unrecognized line");
    }

    private Result processInResponseMode(String line) {
        line = clean(line);
        if (line.length() >= 2 && line.substring(0, 2).equalsIgnoreCase("AT")) {
            return parseCommand(line);
        }
        EndOfResponseResult eor = isEndOfResponse(line);
        if (eor.isEnd()) {
            inResponseMode = false;
            if (eor.isOk()) {
                return Result.completeResponseOk();
            }
            return Result.completeResponseError(eor.getErrorMsg());
        }
        if (line.startsWith("+")) {
            return parseResponse(line);
        }
        line = line.replace('\r', ' ');
        line = line.trim();
        return Result.bodyLine(line);
    }

    private EndOfResponseResult isEndOfResponse(String line) {
        String backup = line;
        line = line.replace('\r', ' ');
        line = line.trim();
        line = line.toUpperCase();
        if (line.equals("OK")) return new EndOfResponseResult(true, true, null);

        Matcher matcher = ERROR_PATTERN.matcher(backup);
        if (matcher.matches()) {
            String err = matcher.group(GROUP_NUMBER_IN_PATTERN_FOR_ERROR_TEXT);
            return new EndOfResponseResult(true, false, err);
        }
        return new EndOfResponseResult(false, false, null);
    }

    private Result parseResponse(String lineWithoutPlus) {
        try {
            ATEvent response = new ResponseGrammar().parse(lineWithoutPlus);
            if (!inResponseMode || !response.getName().equals(responseModeCommandName)) {
                return Result.unsolicitedResponse(response.getName(), response.getParameters());
            }
            return Result.partialResponse(response.getName(), response.getParameters());
        } catch (GrammarException e) {
            return Result.error(e.getMessage());
        }
    }

    @Override
    public void setResponseMode(String cmd) {
        if (inResponseMode)
            throw new IllegalArgumentException("Cannot change to responseMode(\"" + cmd
                    + "\") because already in responseMode(\"" + responseModeCommandName + "\")");
        inResponseMode = true;
        responseModeCommandName = cmd;
    }

    @Override
    public boolean isInResponseMode() {
        return inResponseMode;
    }

    @Override
    public void resetMode() {
        inResponseMode = false;
    }

    @Value
    private static class EndOfResponseResult {
        boolean end;
        boolean ok;
        String errorMsg;
    }
}
