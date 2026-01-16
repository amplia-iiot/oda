package es.amplia.oda.comms.http.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HttpClientConfiguration {

    public static final String DEFAULT_KEY_STORE_TYPE = "JKS";
    public static final String DEFAULT_TRUST_STORE_TYPE = "JKS";

    String keyStorePath;
    String keyStoreType;
    String keyStorePassword;
    String trustStorePath;
    String trustStoreType;
    String trustStorePassword;
}
