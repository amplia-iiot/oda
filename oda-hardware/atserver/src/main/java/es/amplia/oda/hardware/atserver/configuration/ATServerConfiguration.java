package es.amplia.oda.hardware.atserver.configuration;

import lombok.Value;

@Value
public class ATServerConfiguration {
    private String appName;
    private int timeToGetPort;
    private String portName;
    private int baudRate;
    private int dataBits;
    private int stopBits;
    private int parity;
    private long timeBetweenCommands;
}
