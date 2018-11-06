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

/**
 * Datastreams getter with device information.
 */
public class DeviceInfoDatastreamsGetter implements DeviceInfoProvider {
    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(DeviceInfoDatastreamsGetter.class);

    private String deviceId;
    private String apiKey;
    private String serialNumber;

    /**
     * Load the configuration of the device info datastreams getter.
     * @param configuration Configuration to load.
     * @throws CommandExecutionException Exception executing the configured commands.
     */
    public void loadConfiguration(DeviceInfoConfiguration configuration) throws CommandExecutionException {
        deviceId = configuration.getDeviceId();
        logger.info("Load new device identifier: {}", deviceId);
        apiKey = configuration.getApiKey();
        logger.info("Load new API key: {}", apiKey);
        serialNumber = CommandProcessor.execute(configuration.getSerialNumberCommand());
        logger.info("Load new serial number: {}", serialNumber);
    }

    /**
     * Get the device identifier.
     * @return The preconfigured device identifier or, if not present, the serial number.
     */
    @Override
    public String getDeviceId() {
        return deviceId != null ? deviceId : serialNumber;
    }

    /**
     * Get the API key.
     * @return The device API key.
     */
    @Override
    public String getApiKey() {
        return apiKey;
    }
    
    DatastreamsGetter getDatastreamsGetterForDeviceId() {
        return new DatastreamsGetter() {
            @Override
            public String getDatastreamIdSatisfied() {
                return DatastreamIds.DEVICE_ID;
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
                return DatastreamIds.SERIAL_NUMBER;
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
