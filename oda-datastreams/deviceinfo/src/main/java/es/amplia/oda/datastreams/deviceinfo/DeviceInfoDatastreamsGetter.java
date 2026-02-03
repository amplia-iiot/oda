package es.amplia.oda.datastreams.deviceinfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.amplia.oda.core.commons.entities.Software;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfiguration;
import es.amplia.oda.datastreams.deviceinfo.datastreams.ClockInfo;
import es.amplia.oda.datastreams.deviceinfo.datastreams.DatastreamGetterTemplate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeviceInfoDatastreamsGetter implements DeviceInfoProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceInfoDatastreamsGetter.class);
    private final CommandProcessor commandProcessor;
    private final BundleContext context;

    static final String DEVICE_ID_DATASTREAM_ID = "device.identifier";
    static final String SERIAL_NUMBER_DATASTREAM_ID = "device.serialNumber";
    static final String SOFTWARE_DATASTREAM_ID = "device.software";
    static final String CLOCK_DATASTREAM_ID = "device.clock";


    static final String SERIAL_NUMBER_SCRIPT = "obtainSerialNumber.sh";

    private String path;

    private String deviceId;
    private String apiKey;
    private String serialNumber;

    private final List<ServiceRegistration<DatastreamsGetter>> getters = new ArrayList<>();

    private String configFilePath = "";

    DeviceInfoDatastreamsGetter(CommandProcessor commandProcessor, BundleContext bundleContext) {
        this.commandProcessor = commandProcessor;
        this.context = bundleContext;
    }

    public void loadConfiguration(DeviceInfoConfiguration configuration) {
        unregister();
        deviceId = configuration.getDeviceId();
        LOGGER.info("Load new device identifier: {}", deviceId);
        apiKey = configuration.getApiKey();
        LOGGER.info("Load new API key: {}", apiKey);
        path = configuration.getPath();
        LOGGER.info("Load new path to scripts directory: {}", path);

        // load scripts defined in configuration file
        for (String dsId: configuration.getDsScript().keySet()) {
            String []scriptType = configuration.getDsScript().get(dsId).split(";");

            ServiceRegistration<DatastreamsGetter> dsGetter;
            if (scriptType.length == 1) {
                dsGetter = context.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(dsId, scriptType[0], this::executeStript), null);
            } else {
                dsGetter = context.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(dsId, scriptType[0], scriptType[1], this::executeStript), null);
            }
            getters.add(dsGetter);
        }

        // load internal scripts (only if not defined in configuration file)
        if(!configuration.getDsScript().containsKey(DEVICE_ID_DATASTREAM_ID)){
            getters.add(context.registerService(DatastreamsGetter.class,
                    new DatastreamGetterTemplate(DEVICE_ID_DATASTREAM_ID, null,
                            this::getDeviceId), null));
        }
        if(!configuration.getDsScript().containsKey(SERIAL_NUMBER_DATASTREAM_ID)){
            getters.add(context.registerService(DatastreamsGetter.class,
                    new DatastreamGetterTemplate(SERIAL_NUMBER_DATASTREAM_ID, null,
                            this::getSerialNumber), null));
        }
        if(!configuration.getDsScript().containsKey(SOFTWARE_DATASTREAM_ID)){
            getters.add(context.registerService(DatastreamsGetter.class,
                    new DatastreamGetterTemplate(SOFTWARE_DATASTREAM_ID, null,
                            this::getSoftware), null));
        }
        if(!configuration.getDsScript().containsKey(CLOCK_DATASTREAM_ID)){
            getters.add(context.registerService(DatastreamsGetter.class,
                    new DatastreamGetterTemplate(CLOCK_DATASTREAM_ID, null,
                            this::getClock), null));
        }

        // get serial number
        try {
            LOGGER.info("Preparing scripts for run");

            File dir = new File(path);
            for (File script : Objects.requireNonNull(dir.listFiles())) {
                if(!script.setExecutable(true)) {
                    LOGGER.error("Script {} couldn't be set executable", script.getName());
                }
            }

            String snScript = configuration.getDsScript().get(SERIAL_NUMBER_DATASTREAM_ID);

            if ( (snScript != null) && !snScript.isEmpty() ) serialNumber = commandProcessor.execute(snScript);
            else serialNumber = commandProcessor.execute(path + "/" + SERIAL_NUMBER_SCRIPT);
            LOGGER.info("Load new serial number: {}", serialNumber);
        } catch (CommandExecutionException ex) {
            LOGGER.error("Error executing serial number command '{}':", SERIAL_NUMBER_SCRIPT,
                    ex);
        }
    }

    public void unregister() {
        getters.forEach(ServiceRegistration::unregister);
        getters.clear();
    }

    public void setConfigFilePath (String path) {
        this.configFilePath = path.replace("file:", "");
    }

    public void setDeviceId(String deviceId) {
        if (deviceId.equals(this.deviceId)) {
            LOGGER.info("Setting deviceId to the same identifier: {}", deviceId);
        } else {
            try {
                Path cfgPath = Paths.get(this.configFilePath);
                Stream<String> lines = Files.lines(cfgPath);
                List<String> list = lines.map(line -> line.startsWith("deviceId=") ? "deviceId="+deviceId : line).collect(Collectors.toList());
                lines.close();
                Files.write(cfgPath, list);
            } catch (IOException e) {
                LOGGER.error("Error updating deviceId to configuration file", e);
            }
        }
    }
    
    public String getDeviceId() {return getDeviceId(null, null);}
    public String getDeviceId(String scriptToExecute, String type) {
        return (deviceId != null && !deviceId.isEmpty()) ? deviceId : serialNumber;
    }

    public String getApiKey() {
        return getApiKey(null, null);
    }

    public String getApiKey(String scriptToExecute, String type) {
        return apiKey;
    }

    public List<Software> getSoftware() {
        return getSoftware(null, null);
    }

    public List<Software> getSoftware(String scriptToExecute, String type) {
        List<Software> software = new ArrayList<>();
        for (Bundle bundle : context.getBundles()) {
            software.add(new Software(bundle.getSymbolicName(), bundle.getVersion().toString(), "SOFTWARE"));
        }
        LOGGER.debug("Getting actual used Software: {}", software);
        return software;
    }

    public ClockInfo getClock() {
        return getClock(null, null);
    }

    public ClockInfo getClock(String scriptToExecute, String type) {
        ZoneId currentZoneId = ZoneId.systemDefault();
        Instant currentInstant = Instant.now();
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentInstant, currentZoneId);
        LocalDate currentDate = currentDateTime.toLocalDate();
        LocalTime currentTime = currentDateTime.toLocalTime().truncatedTo(ChronoUnit.SECONDS);
        boolean dstActive = currentZoneId.getRules().isDaylightSavings(currentInstant);
        return new ClockInfo(currentDate.toString(), currentTime.toString(), currentZoneId.getId(), (dstActive ? 1 : 0));
    }

    public String getSerialNumber(String scriptToExecute, String type) {
        return this.serialNumber;
    }

    public Object executeStript(String scriptToExecute, String type) {
        try {
            String value = commandProcessor.execute(scriptToExecute);
            if ((type != null) && !type.equals("STRING")) {
                if ((value != null) && !value.isEmpty()) {
                    Object ret;
                    switch (type) {
                        case "BOOLEAN":
                            ret = Boolean.parseBoolean(value);
                            break;
                        case "INTEGER":
                            ret = Integer.parseInt(value);
                            break;
                        case "LONG":
                            ret = Long.parseLong(value);
                            break;
                        case "FLOAT":
                            ret = Float.parseFloat(value);
                            break;
                        case "DOUBLE":
                            ret = Double.parseDouble(value);
                            break;
                        case "JSON":
                            ret = new ObjectMapper().readValue(value, Map.class);
                            break;
                        default:
                            ret = value;
                            break;
                    }
                    LOGGER.debug("Script {} returned value: {}", scriptToExecute, ret);
                    return ret;
                } else {
                    LOGGER.warn("Executing script '{}' return null", scriptToExecute);
                }
            } else {
                LOGGER.debug("Script {} returned value: {}", scriptToExecute, value);
                return value;
            }
        } catch (Throwable ex) {
            LOGGER.error("Error executing script '{}':", scriptToExecute,
                    ex);
        }
        return null;
    }

}
