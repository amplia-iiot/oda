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
     *          | STRING
     */
    private List<ATCommand> commandsRead;
    private SyntacticParser syntacticParser;

    public CommandsGrammar() {
        this.commandsRead = new ArrayList<>();
    }

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
            throw new GrammarException("Unexpected token '" + syntacticParser.current() + "'");
        return commandsRead;
    }

    private void commands() {
        /*
         * <commands> ::= <basic_command> <commands>?
         *             |  <extended_command>
         *             |  <extended_command> SEMICOLON <commands>
         */
        if (syntacticParser.current() == Tokens.BASIC_NAME || syntacticParser.current() == Tokens.S_NAME) {
            basic_command();
            if (syntacticParser.current() != Tokens.EOF)
                commands();
        } else {
            extended_command();
            if (syntacticParser.current() == Tokens.SEMICOLON) {
                advance();
                if (syntacticParser.current() != Tokens.EOF)
                    commands();
            }
        }
    }

    private void basic_command() {
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
            throw new GrammarException("Unexpected token '" + syntacticParser.current() + "'");
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
        return;
    }

    private void extended_command() {
        /*
         * <extended_command> ::= EXTENDED_NAME <command_end>?
         */
        if (syntacticParser.current() != Tokens.EXTENDED_NAME)
            throw new GrammarException("Expected EXTENDED_NAME token but get '" + syntacticParser.current() + "'");
        String name = syntacticParser.getLastCommand();
        advance();

        Tokens t = syntacticParser.current();
        if (t != Tokens.EQUAL && t != Tokens.EQUAL_QUESTION && t != Tokens.QUESTION) {
            commandsRead.add(ATCommand.extendedCommand(ATCommandType.ACTION, name.toString()));
        } else {
            ATCommand c = command_end(name.toString());
            commandsRead.add(c);
        }
    }

    private void advance() {
        syntacticParser.advance();
        while (syntacticParser.current() == Tokens.SPACE)
            syntacticParser.advance();
    }

    private ATCommand command_end(String cmdName) {
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
            if (t == Tokens.NUMBER || t == Tokens.STRING || t == Tokens.COMMA) {
                params = new ArrayList<>();
                parameters(params);
            }
            return ATCommand.extendedSetCommand(cmdName, params);
        }

        throw new GrammarException("Unexpected token '" + syntacticParser.current() + "'");
    }

    private void parameters(List<String> params) {
        /*
         * <parameters> ::= <param>
         *               | <param>? COMMA <parameters>
         * <param> ::= NUMBER
         *          | STRING
         */
        if (syntacticParser.current() == Tokens.NUMBER) {
            Integer number = syntacticParser.getLastNumber();
            params.add(number.toString());
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
