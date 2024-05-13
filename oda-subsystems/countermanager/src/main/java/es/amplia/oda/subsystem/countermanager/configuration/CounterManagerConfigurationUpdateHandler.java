package es.amplia.oda.subsystem.countermanager.configuration;

import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.subsystem.countermanager.internal.CounterEngine;
import es.amplia.oda.subsystem.countermanager.internal.PrinterOutputManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;

public class CounterManagerConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CounterManagerConfigurationUpdateHandler.class);

    static final String ENABLE_PROPERTY_NAME = "countermanager.enable";
    static final String INITIALSIZE_PROPERTY_NAME = "countermanager.initialSize";
    static final String SLOTTIME_PROPERTY_NAME = "countermanager.store.slottime";
    static final String STORELIMIT_PROPERTY_NAME = "countermanager.store.limit";
    static final String FORMAT_PROPERTY_NAME = "printeroutputmanager.showdata.format";
    static final String SETTOTAL_PROPERTY_NAME = "printeroutputmanager.showdata.setTotal";
    static final String SETRATIO_PROPERTY_NAME = "printeroutputmanager.showdata.setRatio";
    static final String SETACC_PROPERTY_NAME = "printeroutputmanager.showdata.setAcc";
    static final String SETAVG_PROPERTY_NAME = "printeroutputmanager.showdata.setAvg";
    static final String SETVAR_PROPERTY_NAME = "printeroutputmanager.showdata.setVar";

    
    private CounterManagerConfiguration currentConfiguration;
    private CounterEngine engine;
    private PrinterOutputManager printerManager;

    public CounterManagerConfigurationUpdateHandler(CounterEngine engine, PrinterOutputManager printerManager) {
        this.engine = engine;
        this.printerManager = printerManager;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading new configuration");

        CounterManagerConfiguration.CounterManagerConfigurationBuilder builder = CounterManagerConfiguration.builder();

        Optional.ofNullable((String) props.get(ENABLE_PROPERTY_NAME)).ifPresent(builder::enable);
        Optional.ofNullable((String) props.get(INITIALSIZE_PROPERTY_NAME)).ifPresent(v -> builder.initialSize(Integer.parseInt(v)));
        Optional.ofNullable((String) props.get(SLOTTIME_PROPERTY_NAME)).ifPresent(v -> builder.slotTime(Integer.parseInt(v)));
        Optional.ofNullable((String) props.get(STORELIMIT_PROPERTY_NAME)).ifPresent(v -> builder.storeLimit(Integer.parseInt(v)));
        Optional.ofNullable((String) props.get(FORMAT_PROPERTY_NAME)).ifPresent(builder::format);
        Optional.ofNullable((String) props.get(SETTOTAL_PROPERTY_NAME)).ifPresent(v -> builder.setTotal(Integer.parseInt(v)));
        Optional.ofNullable((String) props.get(SETRATIO_PROPERTY_NAME)).ifPresent(v -> builder.setRatio(Integer.parseInt(v)));
        Optional.ofNullable((String) props.get(SETACC_PROPERTY_NAME)).ifPresent(v -> builder.setAcc(Integer.parseInt(v)));
        Optional.ofNullable((String) props.get(SETAVG_PROPERTY_NAME)).ifPresent(v -> builder.setAvg(Integer.parseInt(v)));
        Optional.ofNullable((String) props.get(SETVAR_PROPERTY_NAME)).ifPresent(v -> builder.setVar(Integer.parseInt(v)));

        currentConfiguration = builder.build();

        LOGGER.info("New configuration loaded");
    }

    @Override
    public void applyConfiguration() {
        engine.terminate(); // Por si sólo se ha cambiado la configuración y no es la primera vez que se aplica
        if (currentConfiguration != null) {
            engine.setEnable(currentConfiguration.getEnable());
            engine.setInitialSize(currentConfiguration.getInitialSize());
            engine.setSlotTime(currentConfiguration.getSlotTime());
            engine.setStoreLimit(currentConfiguration.getStoreLimit());

            printerManager.setFormat(currentConfiguration.getFormat());
            printerManager.setOffsetTotal(currentConfiguration.getSetTotal());
            printerManager.setOffsetRatio(currentConfiguration.getSetRatio());
            printerManager.setOffsetAcc(currentConfiguration.getSetAcc());
            printerManager.setOffsetAvg(currentConfiguration.getSetAvg());
            printerManager.setOffsetVar(currentConfiguration.getSetVar());

            engine.init();
        }
    }
}
