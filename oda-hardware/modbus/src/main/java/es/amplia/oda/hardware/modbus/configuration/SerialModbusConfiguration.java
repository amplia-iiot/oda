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
    private String portName;
    @Builder.Default
    private int baudRate = DEFAULT_BAUD_RATE;
    @Builder.Default
    private int flowControlIn = DEFAULT_FLOW_CONTROL_IN;
    @Builder.Default
    private int flowControlOut = DEFAULT_FLOW_CONTROL_OUT;
    @Builder.Default
    private int dataBits = DEFAULT_DATA_BITS;
    @Builder.Default
    private int stopBits = DEFAULT_STOP_BITS;
    @Builder.Default
    private int parity = DEFAULT_PARITY;
    @Builder.Default
    private String encoding = DEFAULT_ENCODING;
    @Builder.Default
    private boolean echo = DEFAULT_ECHO;
    @Builder.Default
    private int timeout = DEFAULT_TIMEOUT;
}
