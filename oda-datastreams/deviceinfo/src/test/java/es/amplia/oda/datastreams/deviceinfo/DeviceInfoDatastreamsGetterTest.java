package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceInfoDatastreamsGetterTest {

    private static final String A_DEVICE_ID = "aDeviceId";
    private static final String A_SERIAL_NUMBER = "aSerialNumber";
    private static final String AN_API_KEY = "anApiKey";
    
    private static final String ECHO_COMMAND = "echo ";
    private static final String SERIAL_NUMBER_COMMAND = ECHO_COMMAND + A_SERIAL_NUMBER;

    private DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter;
    
    @Mock
    private DeviceInfoConfiguration deviceInfoConfiguration;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        deviceInfoDatastreamsGetter = new DeviceInfoDatastreamsGetter();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void loadConfigurationCachesValuesOfDeviceInfoConfiguration() throws CommandExecutionException {
        when(deviceInfoConfiguration.getSerialNumberCommand()).thenReturn(SERIAL_NUMBER_COMMAND);
        deviceInfoDatastreamsGetter.loadConfiguration(deviceInfoConfiguration);
        
        verify(deviceInfoConfiguration).getDeviceId();
        verify(deviceInfoConfiguration).getApiKey();
        verify(deviceInfoConfiguration).getSerialNumberCommand();
    }
    
    @Test
    public void loadConfigurationCallsCommandProcessor() throws CommandExecutionException {
        when(deviceInfoConfiguration.getSerialNumberCommand()).thenReturn(SERIAL_NUMBER_COMMAND);
        
        deviceInfoDatastreamsGetter.loadConfiguration(deviceInfoConfiguration);
    }
    
    @Test
    public void getDeviceIdReturnsDeviceIdIfConfigurationHasDeviceId() throws CommandExecutionException {
        when(deviceInfoConfiguration.getDeviceId()).thenReturn(A_DEVICE_ID);
        when(deviceInfoConfiguration.getSerialNumberCommand()).thenReturn(ECHO_COMMAND); // SerialNumberCommand does not matter
        
        deviceInfoDatastreamsGetter.loadConfiguration(deviceInfoConfiguration);
        String actual = deviceInfoDatastreamsGetter.getDeviceId();
        
        assertEquals(A_DEVICE_ID, actual);
    }
    
    @Test
    public void getDeviceIdReturnsSerialNumberIfConfigurationDoentHaveDeviceId() throws CommandExecutionException {
        when(deviceInfoConfiguration.getDeviceId()).thenReturn(null);
        when(deviceInfoConfiguration.getSerialNumberCommand()).thenReturn(SERIAL_NUMBER_COMMAND);
        
        deviceInfoDatastreamsGetter.loadConfiguration(deviceInfoConfiguration);
        String actual = deviceInfoDatastreamsGetter.getDeviceId();
        
        assertEquals(A_SERIAL_NUMBER, actual);
    }
    
    @Test
    public void getApiKeyReturnsTheApiKeyOfTheConfigurationHandler() throws CommandExecutionException {
        when(deviceInfoConfiguration.getApiKey()).thenReturn(AN_API_KEY);
        when(deviceInfoConfiguration.getSerialNumberCommand()).thenReturn(ECHO_COMMAND); // SerialNumberCommand does not matter
        
        deviceInfoDatastreamsGetter.loadConfiguration(deviceInfoConfiguration);
        String actual = deviceInfoDatastreamsGetter.getApiKey();
        
        assertEquals(AN_API_KEY, actual);
    }
    
    @Test
    public void idsSatisfiedForDeviceIdIsDeviceId() {
        String actual = deviceInfoDatastreamsGetter.getDatastreamsGetterForDeviceId().getDatastreamIdSatisfied();

        assertEquals(actual, DatastreamIds.DEVICE_ID);
    }

    @Test
    public void idsSatisfiedForSerialNumberIsSerialNumber() {
        String actual = deviceInfoDatastreamsGetter.getDatastreamsGetterForSerialNumber().getDatastreamIdSatisfied();

        assertEquals(actual, DatastreamIds.SERIAL_NUMBER);
    }
    
    @Test
    public void datastreamsGetterForDeviceIdManagesODA() {
        List<String> actual = deviceInfoDatastreamsGetter.getDatastreamsGetterForDeviceId().getDevicesIdManaged();
        
        List<String> expected = Collections.singletonList("");
        assertEquals(expected, actual);
    }

    @Test
    public void datastreamsGetterForSerialNumberManagesODA() {
        List<String> actual = deviceInfoDatastreamsGetter.getDatastreamsGetterForSerialNumber().getDevicesIdManaged();
        
        List<String> expected = Collections.singletonList("");
        assertEquals(expected, actual);
    }

    @Test
    public void getOfDatastreamGetterForDeviceIdReturnsTheDeviceId() throws InterruptedException, ExecutionException, CommandExecutionException {
        when(deviceInfoConfiguration.getDeviceId()).thenReturn(A_DEVICE_ID);
        when(deviceInfoConfiguration.getSerialNumberCommand()).thenReturn(ECHO_COMMAND); // SerialNumberCommand does not matter
        DatastreamsGetter getterForDeviceId = deviceInfoDatastreamsGetter.getDatastreamsGetterForDeviceId();
        
        deviceInfoDatastreamsGetter.loadConfiguration(deviceInfoConfiguration);
        CollectedValue collectedValue = getterForDeviceId.get(null).get();
        
        assertEquals(A_DEVICE_ID, collectedValue.getValue());
    }
    
    @Test
    public void getOfDatastreamGetterForSerialNumberReturnsTheSerialNumber() throws InterruptedException, ExecutionException, CommandExecutionException {
        when(deviceInfoConfiguration.getSerialNumberCommand()).thenReturn(SERIAL_NUMBER_COMMAND);
        DatastreamsGetter getterForSerialNumber = deviceInfoDatastreamsGetter.getDatastreamsGetterForSerialNumber();
        
        deviceInfoDatastreamsGetter.loadConfiguration(deviceInfoConfiguration);
        CollectedValue collectedValue = getterForSerialNumber.get(null).get();
        
        assertEquals(A_SERIAL_NUMBER, collectedValue.getValue());
    }
}