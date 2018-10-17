package es.amplia.oda.hardware.atmanager.api;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ATEventTest {

    @Test
    public void withOneParameter() {
        String actual = ATEvent.event("+F", "1").asWireString();
        assertThat(actual, is("+F: 1"));
    }

    @Test(expected = AssertionError.class)
    public void eventsMustBeginWithPlus() {
        ATEvent.event("FOO");
    }

    @Test(expected = AssertionError.class)
    public void eventsMustHaveTwoChars() {
        ATEvent.event("+");
    }

    @Test(expected = AssertionError.class)
    public void eventsMustHaveOneParameter() {
        ATEvent.event("+F");
    }

    @Test
    public void eventsAreUppercased() {
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
