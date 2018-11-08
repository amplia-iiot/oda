package es.amplia.oda.hardware.atserver;

import lombok.Value;

@Value
class ATServerConfiguration {
    private String appName;
    private int millisecondsToGetPort;
    private String portName;
    private int baudRate;
    private int dataBits;
    private int stopBits;
    private int parity;
}
