package es.amplia.oda.hardware.comms;

public interface CommsManager extends AutoCloseable{
    void connect(String pin, String apn, String username, String password, int connectionTimeout, long retryConnectionTimer);
    void close();
}
