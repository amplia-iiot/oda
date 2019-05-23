package es.amplia.oda.datastreams.mqtt;

public enum MqttDatastreamPermission {
    RD, WR, RW, NONE;

    boolean isReadable() {
        return this.equals(RD) || this.equals(RW);
    }

    boolean isWritable() {
        return this.equals(WR) || this.equals(RW);
    }
}
