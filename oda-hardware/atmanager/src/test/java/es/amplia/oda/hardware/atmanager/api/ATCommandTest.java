package es.amplia.oda.hardware.atmanager.api;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ATCommandTest {

    @Test
    public void basic() {
        String actual = ATCommand.basicCommand('A').asWireString();
        assertThat(actual, is("A0"));
    }

    @Test
    public void basicIsUppercased() {
        String actual = ATCommand.basicCommand('a').asWireString();
        assertThat(actual, is("A0"));
    }

    @Test(expected = AssertionError.class)
    public void basicCommandsCannotBeS() {
        ATCommand.basicCommand('S');
    }

    @Test
    public void basicWithParameter() {
        String actual = ATCommand.basicCommand('D', 917429966).asWireString();
        assertThat(actual, is("D917429966"));
    }

    @Test
    public void ampersandCommand() {
        String actual = ATCommand.ampersandCommand('M').asWireString();
        assertThat(actual, is("&M"));
    }

    @Test
    public void ampersandWithParameter() {
        String actual = ATCommand.ampersandCommand('M', 35).asWireString();
        assertThat(actual, is("&M35"));
    }

    @Test(expected = AssertionError.class)
    public void ampersandCommandsCannotBeS() {
        ATCommand.ampersandCommand('S');
    }

    @Test
    public void sRead() {
        String actual = ATCommand.sReadCommand(13).asWireString();
        assertThat(actual, is("S13?"));
    }

    @Test
    public void sSet() {
        String actual = ATCommand.sSetCommand(13, 24).asWireString();
        assertThat(actual, is("S13=24"));
    }

    @Test
    public void extendedCommand() {
        String actual = ATCommand.extendedCommand(ATCommandType.ACTION, "+FOO").asWireString();
        assertThat(actual, is("+FOO"));
    }

    @Test(expected = AssertionError.class)
    public void extendedCommandsMustBeginWithPlus() {
        ATCommand.extendedCommand(ATCommandType.ACTION, "FOO");
    }

    @Test(expected = AssertionError.class)
    public void extendedCommandsMustHaveTwoChars() {
        ATCommand.extendedCommand(ATCommandType.ACTION, "+");
    }

    @Test(expected = AssertionError.class)
    public void extendedCommandsMustBeginWithLetter() {
        ATCommand.extendedCommand(ATCommandType.ACTION, "+0");
    }

    @Test
    public void extendedReadCommand() {
        String actual = ATCommand.extendedCommand(ATCommandType.READ, "+FOO").asWireString();
        assertThat(actual, is("+FOO?"));
    }

    @Test
    public void extendedTestCommand() {
        String actual = ATCommand.extendedCommand(ATCommandType.TEST, "+FOO").asWireString();
        assertThat(actual, is("+FOO=?"));
    }

    @Test
    public void extendedSetCommandWithoutParameters() {
        String actual = ATCommand.extendedSetCommand("+FOO").asWireString();
        assertThat(actual, is("+FOO="));
    }

    @Test
    public void extendedSetCommandWithParameters() {
        String actual = ATCommand.extendedSetCommand("+FOO", "1", "", "23").asWireString();
        assertThat(actual, is("+FOO=1,,23"));
    }

    @Test
    public void extendedSetCommandWithStringParameters() {
        String actual = ATCommand.extendedSetCommand("+FOO", "baz", "BAR", "10.1.1.85", "A1B2", "FF", "123", "-123", "1.23").asWireString();
        assertThat(actual, is("+FOO=\"baz\",BAR,10.1.1.85,A1B2,FF,123,\"-123\",1.23"));
    }

}
