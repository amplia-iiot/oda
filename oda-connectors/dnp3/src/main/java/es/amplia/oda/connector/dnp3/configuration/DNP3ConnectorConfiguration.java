package es.amplia.oda.connector.dnp3.configuration;

import com.automatak.dnp3.LogMasks;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DNP3ConnectorConfiguration {
    public static final String DEFAULT_CHANNEL_IDENTIFIER = "tcpServerChannel";
    public static final String DEFAULT_OUTSTATION_IDENTIFIER = "outstation";
    public static final String DEFAULT_IP_ADDRESS = "0.0.0.0";
    public static final int DEFAULT_IP_PORT = 20000;
    public static final int DEFAULT_LOCAL_DEVICE_DNP3_ADDRESS = 1;
    public static final int DEFAULT_REMOTE_DEVICE_DNP3_ADDRESS = 1024;
    public static final boolean DEFAULT_UNSOLICITED_RESPONSE = false;
    public static final int DEFAULT_EVENT_BUFFER_SIZE = 5;
    public static final int DEFAULT_LOG_LEVEL = LogMasks.NORMAL;
    public static final boolean DEFAULT_ENABLE = false;

    @Builder.Default
    private String channelIdentifier = DEFAULT_CHANNEL_IDENTIFIER;
    @Builder.Default
    private String outstationIdentifier = DEFAULT_OUTSTATION_IDENTIFIER;
    @Builder.Default
    private String ipAddress = DEFAULT_IP_ADDRESS;
    @Builder.Default
    private int ipPort = DEFAULT_IP_PORT;
    @Builder.Default
    private int localDeviceDNP3Address = DEFAULT_LOCAL_DEVICE_DNP3_ADDRESS;
    @Builder.Default
    private int remoteDeviceDNP3Address = DEFAULT_REMOTE_DEVICE_DNP3_ADDRESS;
    @Builder.Default
    private boolean unsolicitedResponse = DEFAULT_UNSOLICITED_RESPONSE;
    @Builder.Default
    private int eventBufferSize = DEFAULT_EVENT_BUFFER_SIZE;
    @Builder.Default
    private int logLevel = DEFAULT_LOG_LEVEL;
    @Builder.Default
    private boolean enable = DEFAULT_ENABLE;
}
