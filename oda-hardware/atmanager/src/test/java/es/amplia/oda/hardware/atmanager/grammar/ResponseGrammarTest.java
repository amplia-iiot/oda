package es.amplia.oda.hardware.atmanager.grammar;

import es.amplia.oda.hardware.atmanager.api.ATEvent;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResponseGrammarTest {
    private ResponseGrammar responseGrammar;

    @Before
    public void setUp() {
        responseGrammar = new ResponseGrammar();
    }

    @Test
    public void responseLine() {
        ATEvent actual = responseGrammar.parse("+CREG: 2");

        ATEvent expected = ATEvent.event("+CREG", "2");
        assertEquals(actual, expected);
    }

    @Test(expected = GrammarException.class)
    public void responseLineWithoutColon() {
        responseGrammar.parse("+CREG");
    }

    @Test
    public void responseLineWithStringParameters() {
        ATEvent actual = responseGrammar.parse("+CREG: \"This is a test\",2,\" full of parameters\"");

        ATEvent expected = ATEvent.event("+CREG", "This is a test", "2", " full of parameters");
        assertEquals(actual, expected);
    }

    @Test
    public void responseLineWithFloatParameter() {
        ATEvent actual = responseGrammar.parse("+CREG: \"This is a test\",20.0,\" full of parameters\"");

        ATEvent expected = ATEvent.event("+CREG", "This is a test", "20.0", " full of parameters");
        assertEquals(actual, expected);
    }

    @Test
    public void colonWithoutValues() {
        ATEvent actual = responseGrammar.parse("+CREG: ,");

        ATEvent expected = ATEvent.event("+CREG", "", "");
        assertEquals(actual, expected);
    }

    @Test
    public void colonWithOrWithoutValues() {
        ATEvent actual = responseGrammar.parse(" +CREG:  ,  1 ,  , 2 , ");

        ATEvent expected = ATEvent.event("+CREG", "", "1", "", "2", "");
        assertEquals(actual, expected);
    }

    @Test(expected = GrammarException.class)
    public void responseLineWithErrorInParameters() {
        responseGrammar.parse("+CREG: ");
    }

    @Test(expected = GrammarException.class)
    public void responseLineWithErrorInName() {
        responseGrammar.parse("+CREG+");
    }

}
