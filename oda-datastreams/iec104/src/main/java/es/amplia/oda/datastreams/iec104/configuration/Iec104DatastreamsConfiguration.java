package es.amplia.oda.datastreams.iec104.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(builderClassName = "Iec104DatastreamsConfigurationBuilder")
public class Iec104DatastreamsConfiguration {
    @NonNull
    private String deviceId;
    @NonNull
    private String ipAddress;
    @NonNull
    private Integer ipPort;
    @NonNull
    private Integer commonAddress;

    public static class Iec104DatastreamsConfigurationBuilder {
        private String deviceId;
        private String ipAddress;
        private int ipPort;
        private int commonAddress;

        public Iec104DatastreamsConfigurationBuilder deviceId (String deviceId) {
            this.deviceId = deviceId;
            return this;
        }
        
        public Iec104DatastreamsConfigurationBuilder ipAddress (String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Iec104DatastreamsConfigurationBuilder ipPort (int ipPort) {
            this.ipPort = ipPort;
            return this;
        }

        public Iec104DatastreamsConfigurationBuilder commonAddress (int commonAddress) {
            this.commonAddress = commonAddress;
            return this;
        }

        public Iec104DatastreamsConfiguration build () {
            return new Iec104DatastreamsConfiguration(deviceId, ipAddress, ipPort, commonAddress);
        }
    }
}
