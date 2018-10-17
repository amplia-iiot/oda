package es.amplia.oda.connector.mqtt.configuration;

/**
 * Configuration of the MQTT queues
 */
public class QueuesConfiguration {

    /**
     * Request queue to subscribe.
     */
    private final String requestQueue;

    /**
     * Response queue to publish.
     */
    private final String responseQueue;

    /**
     * IOT queue to publish.
     */
    private final String iotQueue;

    /**
     * MQTT messages Quality of Service.
     */
    private final int qualityOfService;

    /**
     * MQTT messages retained policy.
     */
    private final boolean retained;

    /**
     * Constructor.
     *
     * @param requestQueue     Request queue to subscribe.
     * @param responseQueue    Response queue to publish.
     * @param iotQueue         IOT queue to publish.
     * @param qualityOfService MQTT message Quality of Service.
     * @param retained         MQTT message retained policy.
     */
    QueuesConfiguration(String requestQueue, String responseQueue, String iotQueue, int qualityOfService,
                        boolean retained) {
        this.requestQueue = requestQueue;
        this.responseQueue = responseQueue;
        this.iotQueue = iotQueue;
        this.qualityOfService = qualityOfService;
        this.retained = retained;
    }

    /**
     * Get the request queue to subscribe.
     *
     * @return Request queue to subscribe.
     */
    public String getRequestQueue() {
        return requestQueue;
    }

    /**
     * Get the response queue to publish.
     *
     * @return Response queue to publish.
     */
    public String getResponseQueue() {
        return responseQueue;
    }

    /**
     * Get the IOT data queue to publish.
     *
     * @return IOT data queue to publish.
     */
    public String getIotQueue() {
        return iotQueue;
    }

    /**
     * Get the MQTT message Quality of Service.
     *
     * @return MQTT message Quality of Service.
     */
    public int getQualityOfService() {
        return qualityOfService;
    }

    /**
     * Get the MQTT message retained policy.
     *
     * @return MQTT message retained policy.
     */
    public boolean isRetained() {
        return retained;
    }

}
