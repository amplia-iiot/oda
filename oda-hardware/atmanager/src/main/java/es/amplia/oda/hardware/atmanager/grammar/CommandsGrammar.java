package es.amplia.oda.hardware.atmanager.grammar;

import es.amplia.oda.hardware.atmanager.api.ATCommand;
import es.amplia.oda.hardware.atmanager.api.ATCommandType;
import es.amplia.oda.hardware.atmanager.grammar.SyntacticParser.Tokens;

import java.util.ArrayList;
import java.util.List;

public class CommandsGrammar {
    /*
     * <commands> ::= <basic_command> <commands>?
     *             |  <extended_command>
     *             |  <extended_command> SEMICOLON <commands>?
     *
     * <basic_command> ::= BASIC_NAME NUMBER?
     *                  |  S_NAME EQUALS NUMBER
     *
     * <extended_command> ::= EXTENDED_NAME <command_end>?
     * <command_end> ::= QUESTION
     *                |  EQUAL <parameters>?
     *                |  EQUAL_QUESTION
     * <parameters> ::= <param>
     *               | <param>? COMMA <parameters>
     * <param> ::= NUMBER
     *          | FLOAT
     *          | STRING
     */
    private final List<ATCommand> commandsRead = new ArrayList<>();
    private SyntacticParser syntacticParser;


    public List<ATCommand> parse(String toParse) {
        this.syntacticParser = new SyntacticParser(toParse);
        if (syntacticParser.current() == Tokens.SPACE)
            advance();
        if (syntacticParser.current() == Tokens.EOF) {
            commandsRead.add(ATCommand.emptyCommand());
            return commandsRead;
        }
        commands();
        if (syntacticParser.current() != Tokens.EOF)
            throwNewGrammarException();
        return commandsRead;
    }

    private void throwNewGrammarException() {
        throw new GrammarException("Unexpected token '" + syntacticParser.current() + "'");
    }

    private void commands() {
        /*
         * <commands> ::= <basic_command> <commands>?
         *             |  <extended_command>
         *             |  <extended_command> SEMICOLON <commands>
         */
        if (syntacticParser.current() == Tokens.BASIC_NAME || syntacticParser.current() == Tokens.S_NAME) {
            basicCommand();
            if (syntacticParser.current() != Tokens.EOF)
                commands();
        } else {
            extendedCommand();
            if (syntacticParser.current() == Tokens.SEMICOLON) {
                advance();
                if (syntacticParser.current() != Tokens.EOF)
                    commands();
            }
        }
    }

    private void basicCommand() {
        /*
         * <basic_command> ::= BASIC_NAME NUMBER?
         *                  |  S_NAME EQUALS NUMBER
         */
        if (syntacticParser.current() == Tokens.BASIC_NAME) {
            String cmd = syntacticParser.getLastCommand();
            advance();
            int number = 0;
            if (syntacticParser.current() == Tokens.NUMBER) {
                number = syntacticParser.getLastNumber();
                advance();
            }
            commandsRead.add(ATCommand.basicCommand(cmd.charAt(0), number));
            return;
        }
        if (syntacticParser.current() != Tokens.S_NAME)
            throwNewGrammarException();
        String cmd = syntacticParser.getLastCommand();
        advance();

        if (syntacticParser.current() != Tokens.EQUAL)
            throw new GrammarException("Expected EQUAL token but get '" + syntacticParser.current() + "'");
        advance();

        if (syntacticParser.current() != Tokens.NUMBER)
            throw new GrammarException("Expected NUMBER token but get '" + syntacticParser.current() + "'");
        int number = syntacticParser.getLastNumber();
        advance();

        int register = Integer.parseInt(cmd.substring(1));
        commandsRead.add(ATCommand.sSetCommand(register, number));
    }

    private void extendedCommand() {
        /*
         * <extended_command> ::= EXTENDED_NAME <command_end>?
         */
        if (syntacticParser.current() != Tokens.EXTENDED_NAME)
            throw new GrammarException("Expected EXTENDED_NAME token but get '" + syntacticParser.current() + "'");
        String name = syntacticParser.getLastCommand();
        advance();

        Tokens t = syntacticParser.current();
        if (t != Tokens.EQUAL && t != Tokens.EQUAL_QUESTION && t != Tokens.QUESTION) {
            commandsRead.add(ATCommand.extendedCommand(ATCommandType.ACTION, name));
        } else {
            ATCommand c = commandEnd(name);
            commandsRead.add(c);
        }
    }

    private void advance() {
        syntacticParser.advance();
        while (syntacticParser.current() == Tokens.SPACE)
            syntacticParser.advance();
    }

    private ATCommand commandEnd(String cmdName) {
        /*
         * <command_end> ::= QUESTION
         *                |  EQUAL_QUESTION
         *                |  EQUAL <parameters>?
         */
        Tokens t = syntacticParser.current();
        if (t == Tokens.QUESTION) {
            advance();
            ATCommandType type = ATCommandType.READ;
            return ATCommand.extendedCommand(type, cmdName);
        }
        if (t == Tokens.EQUAL_QUESTION) {
            advance();
            ATCommandType type = ATCommandType.TEST;
            return ATCommand.extendedCommand(type, cmdName);
        }
        if (t == Tokens.EQUAL) {
            advance();
            List<String> params = null;
            t = syntacticParser.current();
            if (t == Tokens.NUMBER || t == Tokens.FLOAT || t == Tokens.STRING || t == Tokens.COMMA) {
                params = new ArrayList<>();
                parameters(params);
            }
            return ATCommand.extendedSetCommand(cmdName, params);
        }

        throwNewGrammarException();
        // Should not get here. Exception must be thrown before
        return null;
    }

    private void parameters(List<String> params) {
        /*
         * <parameters> ::= <param>
         *               | <param>? COMMA <parameters>
         * <param> ::= NUMBER
         *          | FLOAT
         *          | STRING
         */
        if (syntacticParser.current() == Tokens.NUMBER) {
            int number = syntacticParser.getLastNumber();
            params.add(Integer.toString(number));
            advance();
        } else if (syntacticParser.current() == Tokens.FLOAT) {
            double lastFloat = syntacticParser.getLastFloat();
            params.add(Double.toString(lastFloat));
            advance();
        } else if (syntacticParser.current() == Tokens.STRING) {
            String param = syntacticParser.getLastString();
            params.add(param);
            advance();
        } else if (syntacticParser.current() == Tokens.COMMA) {
            params.add("");
        }
        if (syntacticParser.current() == Tokens.COMMA) {
            advance();
            parameters(params);
        }
    }
}
