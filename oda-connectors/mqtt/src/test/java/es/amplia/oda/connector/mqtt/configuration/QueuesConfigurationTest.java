package es.amplia.oda.connector.mqtt.configuration;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class QueuesConfigurationTest {

    private static final String REQUEST_QUEUE = "queue/request";
    private static final String RESPONSE_QUEUE = "queue/response";
    private static final String IOT_QUEUE = "queue/iot";
    private static final int QUALITY_OF_SERVICE = 1;
    private static final boolean RETAINED = true;

    private QueuesConfiguration testConfiguration;

    @Before
    public void setUp() {
        testConfiguration =
                new QueuesConfiguration(REQUEST_QUEUE, RESPONSE_QUEUE, IOT_QUEUE, QUALITY_OF_SERVICE, RETAINED);
    }

    @Test
    public void testConstructor() {
        assertNotNull(testConfiguration);
    }

    @Test
    public void testGetRequestQueue() {
        assertEquals(REQUEST_QUEUE, testConfiguration.getRequestQueue());
    }

    @Test
    public void testGetResponseQueue() {
        assertEquals(RESPONSE_QUEUE, testConfiguration.getResponseQueue());
    }

    @Test
    public void testGetIotQueue() {
        assertEquals(IOT_QUEUE, testConfiguration.getIotQueue());
    }

    @Test
    public void testGetQualityOfService() {
        assertEquals(QUALITY_OF_SERVICE, testConfiguration.getQualityOfService());
    }

    @Test
    public void testGetRetained() {
        assertEquals(RETAINED, testConfiguration.isRetained());
    }
}