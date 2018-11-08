package es.amplia.oda.connector.mqtt.configuration;

public class QueuesConfiguration {

    private final String requestQueue;
    private final String responseQueue;
    private final String iotQueue;
    private final int qualityOfService;
    private final boolean retained;

    QueuesConfiguration(String requestQueue, String responseQueue, String iotQueue, int qualityOfService,
                        boolean retained) {
        this.requestQueue = requestQueue;
        this.responseQueue = responseQueue;
        this.iotQueue = iotQueue;
        this.qualityOfService = qualityOfService;
        this.retained = retained;
    }

    public String getRequestQueue() {
        return requestQueue;
    }

    public String getResponseQueue() {
        return responseQueue;
    }

    public String getIotQueue() {
        return iotQueue;
    }

    public int getQualityOfService() {
        return qualityOfService;
    }

    public boolean isRetained() {
        return retained;
    }
}
