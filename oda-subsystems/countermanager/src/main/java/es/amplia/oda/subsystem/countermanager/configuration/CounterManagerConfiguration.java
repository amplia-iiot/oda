package es.amplia.oda.subsystem.countermanager.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CounterManagerConfiguration {
    @NonNull
    private String enable;
    private int initialSize;
    private int slotTime;
    private int storeLimit;
    @NonNull
    private String format;
    private int setTotal;
    private int setRatio;
    private int setAcc;
    private int setAvg;
    private int setVar;
}
