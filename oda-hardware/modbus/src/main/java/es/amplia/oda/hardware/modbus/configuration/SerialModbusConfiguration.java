package es.amplia.oda.hardware.modbus.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SerialModbusConfiguration {
    private static final int DEFAULT_BAUD_RATE = 9600;
    private static final int DEFAULT_FLOW_CONTROL_IN = 0;
    private static final int DEFAULT_FLOW_CONTROL_OUT = 0;
    private static final int DEFAULT_DATA_BITS = 8;
    private static final int DEFAULT_STOP_BITS = 1;
    private static final int DEFAULT_PARITY = 0;
    private static final String DEFAULT_ENCODING = "ascii";
    private static final boolean DEFAULT_ECHO = false;
    private static final int DEFAULT_TIMEOUT = 3000;

    @NonNull
    String portName;
    @NonNull
    String deviceId;
    @Builder.Default
    int baudRate = DEFAULT_BAUD_RATE;
    @Builder.Default
    int flowControlIn = DEFAULT_FLOW_CONTROL_IN;
    @Builder.Default
    int flowControlOut = DEFAULT_FLOW_CONTROL_OUT;
    @Builder.Default
    int dataBits = DEFAULT_DATA_BITS;
    @Builder.Default
    int stopBits = DEFAULT_STOP_BITS;
    @Builder.Default
    int parity = DEFAULT_PARITY;
    @Builder.Default
    String encoding = DEFAULT_ENCODING;
    @Builder.Default
    boolean echo = DEFAULT_ECHO;
    @Builder.Default
    int timeout = DEFAULT_TIMEOUT;
}
