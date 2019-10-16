package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;

import org.junit.Test;

import java.util.Dictionary;
import java.util.Optional;

import static org.junit.Assert.*;

public class ConfigurationUpdateHandlerTest {

    private final ConfigurationUpdateHandler testDefaultConfigHandler = new ConfigurationUpdateHandler() {
        @Override
        public void loadConfiguration(Dictionary<String, ?> props) {}

        @Override
        public void applyConfiguration() {}
    };

    @Test(expected = ConfigurationException.class)
    public void testLoadDefaultConfiguration() {
        testDefaultConfigHandler.loadDefaultConfiguration();

        fail("Configuration exception must be thrown");
    }

    @Test
    public void testGetTokensFromProperty() {
        String testProperties = "tokenOne: valueOne, tokenTwo: valueTwo, tokenThree: valueThree";

        String[] tokens = testDefaultConfigHandler.getTokensFromProperty(testProperties);

        assertEquals(3, tokens.length);
    }

    @Test
    public void testGetValueByToken() {
        String[] testTokens = { "tokenOne: valueOne" , "testToken: testValue" , "tokenThree: valueThree" };

        Optional<String> value = testDefaultConfigHandler.getValueByToken("testToken", testTokens);

        assertTrue(value.isPresent());
        assertEquals("testValue", value.get());
    }

    @Test
    public void testGetValueByTokenNotPresent() {
        String[] testTokens = { "tokenOne: valueOne" , "testToken: testValue" , "tokenThree: valueThree" };

        Optional<String> value = testDefaultConfigHandler.getValueByToken("noPresentToken", testTokens);

        assertFalse(value.isPresent());
    }
}