package es.amplia.oda.hardware.comms.configuration;

import lombok.Value;

@Value
class CommsConfiguration {
    String pin;
    String apn;
    String username;
    String password;
    int connectionTimeout;
    long retryConnectionTimer;
    String source;
    String path;
}
