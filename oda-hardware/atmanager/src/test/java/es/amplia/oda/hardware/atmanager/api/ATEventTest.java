package es.amplia.oda.hardware.atmanager.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ATEventTest {

    @Test
    public void withOneParameter() {
        String actual = ATEvent.event("+F", "1").asWireString();
        assertThat(actual, is("+F: 1"));
    }

    @Test
    public void eventsMustBeginWithPlus() {
        assertThrows(AssertionError.class, () -> ATEvent.event("FOO"));
    }

    @Test
    public void eventsMustHaveTwoChars() {
        assertThrows(AssertionError.class, () -> ATEvent.event("+"));
    }

    @Test
    public void eventsMustHaveOneParameter() {
        assertThrows(AssertionError.class, () -> ATEvent.event("+F"));
    }

    @Test
    public void eventsAreUppercase() {
        String actual = ATEvent.event("+foo", "1").asWireString();
        assertThat(actual, is("+FOO: 1"));
    }

    @Test
    public void stringParametersUseQuotes() {
        String actual = ATEvent.event("+foo", "baz").asWireString();
        assertThat(actual, is("+FOO: \"baz\""));
    }

    @Test
    public void emptyParametersAreNotDisplayed() {
        String actual = ATEvent.event("+foo", "", "baz").asWireString();
        assertThat(actual, is("+FOO: ,\"baz\""));
    }

    @Test
    public void nonIntegerParametersAreDisplayedAsStrings() {
        String actual = ATEvent.event("+foo", "1.23").asWireString();
        assertThat(actual, is("+FOO: \"1.23\""));
    }

    @Test
    public void completeTest() {
        String actual = ATEvent.event("+WIND", "10", "SM", "0", "FD", "0", "ON", "0", "SN", "0", "EN", "0").asWireString();
        assertThat(actual, is("+WIND: 10,\"SM\",0,\"FD\",0,\"ON\",0,\"SN\",0,\"EN\",0"));
    }

}
