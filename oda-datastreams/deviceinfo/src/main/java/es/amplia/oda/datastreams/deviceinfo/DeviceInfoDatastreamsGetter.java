package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DeviceInfoDatastreamsGetter implements DeviceInfoProvider {

    private static final Logger logger = LoggerFactory.getLogger(DeviceInfoDatastreamsGetter.class);

    static final String DEVICE_ID_DATASTREAM_ID = "device.identifier";
    static final String SERIAL_NUMBER_DATASTREAM_ID = "device.serialNumber";

    private final CommandProcessor commandProcessor;

    private String deviceId;
    private String apiKey;
    private String serialNumber;


    DeviceInfoDatastreamsGetter(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    public void loadConfiguration(DeviceInfoConfiguration configuration) {
        deviceId = configuration.getDeviceId();
        logger.info("Load new device identifier: {}", deviceId);
        apiKey = configuration.getApiKey();
        logger.info("Load new API key: {}", apiKey);
        try {
            serialNumber = commandProcessor.execute(configuration.getSerialNumberCommand());
            logger.info("Load new serial number: {}", serialNumber);
        } catch (CommandExecutionException ex) {
            logger.error("Error executing serial number command '{}': {}", configuration.getSerialNumberCommand(),
                    ex);
        }
    }

    @Override
    public String getDeviceId() {
        return deviceId != null ? deviceId : serialNumber;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }
    
    DatastreamsGetter getDatastreamsGetterForDeviceId() {
        return new DatastreamsGetter() {
            @Override
            public String getDatastreamIdSatisfied() {
                return DEVICE_ID_DATASTREAM_ID;
            }
            @Override
            public List<String> getDevicesIdManaged() {
                return Collections.singletonList("");
            }
            @Override
            public CompletableFuture<CollectedValue> get(String device) {
                return CompletableFuture.completedFuture(
                    new CollectedValue(System.currentTimeMillis(), getDeviceId())
                );
            }
        };
    }

    DatastreamsGetter getDatastreamsGetterForSerialNumber() {
        return new DatastreamsGetter() {
            @Override
            public String getDatastreamIdSatisfied() {
                return SERIAL_NUMBER_DATASTREAM_ID;
            }
            @Override
            public List<String> getDevicesIdManaged() {
                return Collections.singletonList("");
            }
            @Override
            public CompletableFuture<CollectedValue> get(String device) {
                return CompletableFuture.completedFuture(
                    new CollectedValue(System.currentTimeMillis(), serialNumber)
                );
            }
        };
    }
}
