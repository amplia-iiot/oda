package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.State;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.script.ScriptException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class RuleTest {

	Rule testRule;
	@Mock
	ScriptTranslator mockedTranslator;
	@Mock
	State mockedState;

	@Test
	public void testConstructor() throws ScriptException {
		testRule = new Rule("Norma", "Presión", mockedTranslator);

		verify(mockedTranslator).initScript("Norma");
	}

	@Test (expected = ScriptException.class)
	public void testConstructorException() throws ScriptException {
		doThrow(new ScriptException("")).when(mockedTranslator).initScript(anyString());

		testRule = new Rule("Norma", "Presión", mockedTranslator);
	}
	
	@Test
	public void testWhen() throws ScriptException {
		when(mockedTranslator.runMethod(anyString(), anyString(), any(), any())).thenReturn(true);
		testRule = new Rule("Norma", "Presión", mockedTranslator);
		DatastreamValue testDatastreamValue = new DatastreamValue("testDevice", "testDatastream", System.currentTimeMillis(), true, DatastreamValue.Status.OK, "");

		boolean result = testRule.when(mockedState, testDatastreamValue);

		assertTrue(result);
	}

	@Test
	public void testThen() throws ScriptException {
		when(mockedTranslator.runMethod(anyString(), anyString(), any(), any())).thenReturn(mockedState);
		testRule = new Rule("Norma", "Presión", mockedTranslator);
		DatastreamValue testDatastreamValue = new DatastreamValue("testDevice", "testDatastream", System.currentTimeMillis(), true, DatastreamValue.Status.OK, "");

		State result = testRule.then(mockedState, testDatastreamValue);

		assertEquals(mockedState, result);
	}
}
