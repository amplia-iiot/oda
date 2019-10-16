package es.amplia.oda.hardware.atmanager.grammar;

import es.amplia.oda.hardware.atmanager.api.ATCommand;
import es.amplia.oda.hardware.atmanager.api.ATCommandType;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CommandsGrammarTest {
    private CommandsGrammar commandsGrammar;

    @Before
    public void setUp() {
        commandsGrammar = new CommandsGrammar();
    }

    @Test
    public void characterCommand() {
        List<ATCommand> actual = commandsGrammar.parse("A");

        List<ATCommand> expected = Collections.singletonList(ATCommand.basicCommand('A'));
        assertThat(actual, is(expected));
    }

    @Test
    public void empty() {
        List<ATCommand> actual = commandsGrammar.parse("");

        List<ATCommand> expected = Collections.singletonList(ATCommand.emptyCommand());
        assertThat(actual, is(expected));
    }

    @Test
    public void characterCommandWithParameters() {
        List<ATCommand> actual = commandsGrammar.parse("D917429966");

        List<ATCommand> expected = Collections.singletonList(ATCommand.basicCommand('D', 917429966));
        assertThat(actual, is(expected));
    }

    @Test
    public void extendedCommand() {
        List<ATCommand> actual = commandsGrammar.parse("+CPIN");

        List<ATCommand> expected = Collections.singletonList(ATCommand.extendedCommand(ATCommandType.ACTION, "+CPIN"));
        assertThat(actual, is(expected));
    }

    @Test
    public void extendedCommandsConcatenatedWithEmpty() {
        List<ATCommand> actual = commandsGrammar.parse("+CPIN;");

        List<ATCommand> expected = Collections.singletonList(ATCommand.extendedCommand(ATCommandType.ACTION, "+CPIN"));
        assertThat(actual, is(expected));
    }

    @Test
    public void extendedCommandsConcatenatedWithBasicOne() {
        List<ATCommand> actual = commandsGrammar.parse("+CPIN;D917429966");

        List<ATCommand> expected = Arrays.asList(
                ATCommand.extendedCommand(ATCommandType.ACTION, "+CPIN"),
                ATCommand.basicCommand('D', 917429966)
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void extendedCommandsConcatenatedWithExtendedCommand() {
        List<ATCommand> actual = commandsGrammar.parse("+CPIN;+CMEE");

        List<ATCommand> expected = Arrays.asList(
                ATCommand.extendedCommand(ATCommandType.ACTION, "+CPIN"),
                ATCommand.extendedCommand(ATCommandType.ACTION, "+CMEE")
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void extendedReadCommand() {
        List<ATCommand> actual = commandsGrammar.parse("+CPIN?");

        List<ATCommand> expected = Collections.singletonList(ATCommand.extendedCommand(ATCommandType.READ, "+CPIN"));
        assertThat(actual, is(expected));
    }

    @Test
    public void extendedTestCommand() {
        List<ATCommand> actual = commandsGrammar.parse("+CPIN=?");

        List<ATCommand> expected = Collections.singletonList(ATCommand.extendedCommand(ATCommandType.TEST, "+CPIN"));
        assertThat(actual, is(expected));
    }

    @Test
    public void extendedSetCommandWithoutValue() {
        List<ATCommand> actual = commandsGrammar.parse("+CPIN=");

        List<ATCommand> expected = Collections.singletonList(ATCommand.extendedCommand(ATCommandType.SET, "+CPIN"));
        assertThat(actual, is(expected));
    }

    @Test
    public void extendedSetCommandWithUnusualParameters() {
        List<ATCommand> actual = commandsGrammar.parse("+CPIN=23,2.3,\"kkk;llll\"");

        List<ATCommand> expected =
                Collections.singletonList(ATCommand.extendedSetCommand("+CPIN",
                                          Arrays.asList("23", "2.3", "kkk;llll")));
        assertThat(actual, is(expected));
    }

    @Test
    public void extendedSetCommandWithFloatParameter() {
        List<ATCommand> expected = Collections.singletonList(ATCommand.extendedSetCommand("+BATTERY", "12.1234"));

        List<ATCommand> actual = commandsGrammar.parse("+BATTERY=12.1234");

        assertEquals(expected, actual);
    }

    @Test
    public void extendedSetCommandWithUnusualParametersFollowedBySemicolon() {
        List<ATCommand> actual = commandsGrammar.parse("+CPIN=23,\"kkk;llll\",77,\"pp\";");

        List<ATCommand> expected = Collections.singletonList(ATCommand.extendedSetCommand("+CPIN", Arrays.asList("23", "kkk;llll", "77", "pp")));
        assertThat(actual, is(expected));
    }

    @Test
    public void extendedSetCommandWithUnusualParametersFollowedByBasicCommandWithParameters() {
        List<ATCommand> actual = commandsGrammar.parse("+CPIN=23,\"kkk;llll\";D917429966");

        List<ATCommand> expected = Arrays.asList(
                ATCommand.extendedSetCommand("+CPIN", Arrays.asList("23", "kkk;llll")),
                ATCommand.basicCommand('D', 917429966)
        );
        assertThat(actual, is(expected));
    }

    @Test(expected = GrammarException.class)
    public void invalidCharacterAfterAT() {
        commandsGrammar.parse("0");
    }

    @Test(expected = GrammarException.class)
    public void invalidCharacterAfterEndOfCommand() {
        commandsGrammar.parse("+P=?1");

    }

    @Test(expected = GrammarException.class)
    public void invalidCharacterInBasicCommand() {
        commandsGrammar.parse("D\"A\"");

    }

    @Test(expected = GrammarException.class)
    public void atSemicolon() {
        commandsGrammar.parse(";");
    }

    @Test
    public void spacesAreOmittedOutsideStrings() {
        List<ATCommand> actual = commandsGrammar.parse("  +CPIN = 23 ,\"kkk ; llll\" ; D 917429966  ");

        List<ATCommand> expected = Arrays.asList(
                ATCommand.extendedSetCommand("+CPIN", Arrays.asList("23", "kkk ; llll")),
                ATCommand.basicCommand('D', 917429966)
        );
        assertThat(actual, is(expected));
    }
}
