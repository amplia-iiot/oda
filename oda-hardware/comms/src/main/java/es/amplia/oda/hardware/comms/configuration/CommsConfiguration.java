package es.amplia.oda.hardware.comms.configuration;

import lombok.Value;

@Value
class CommsConfiguration {
    private String pin;
    private String apn;
    private String username;
    private String password;
    private int connectionTimeout;
    private long retryConnectionTimer;
    private String source;
    private String path;
}
