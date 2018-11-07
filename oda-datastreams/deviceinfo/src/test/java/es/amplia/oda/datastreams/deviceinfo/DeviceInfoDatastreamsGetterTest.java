package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoDatastreamsGetterTest {

    private static final String A_DEVICE_ID = "aDeviceId";
    private static final String A_SERIAL_NUMBER = "aSerialNumber";
    private static final String AN_API_KEY = "anApiKey";
    private static final String ECHO_COMMAND = "echo ";
    private static final String SERIAL_NUMBER_COMMAND = ECHO_COMMAND + A_SERIAL_NUMBER;
    private static final DeviceInfoConfiguration TEST_CONFIGURATION =
            new DeviceInfoConfiguration(A_DEVICE_ID, AN_API_KEY, SERIAL_NUMBER_COMMAND);

    @Mock
    private CommandProcessor mockedCommandProcessor;
    @InjectMocks
    private DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter;


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void loadConfigurationCachesValuesOfDeviceInfoConfiguration() throws CommandExecutionException {
        when(mockedCommandProcessor.execute(anyString())).thenReturn(A_SERIAL_NUMBER);

        deviceInfoDatastreamsGetter.loadConfiguration(TEST_CONFIGURATION);

        verify(mockedCommandProcessor).execute(SERIAL_NUMBER_COMMAND);
        assertEquals(A_DEVICE_ID, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "deviceId"));
        assertEquals(AN_API_KEY, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "apiKey"));
        assertEquals(A_SERIAL_NUMBER, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "serialNumber"));
    }
    
    @Test
    public void loadConfigurationCaughtCommandProcessorException() throws CommandExecutionException {
        doThrow(new CommandExecutionException("","", new RuntimeException())).when(mockedCommandProcessor)
                .execute(anyString());
        
        deviceInfoDatastreamsGetter.loadConfiguration(TEST_CONFIGURATION);

        verify(mockedCommandProcessor).execute(SERIAL_NUMBER_COMMAND);
        assertEquals(A_DEVICE_ID, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "deviceId"));
        assertEquals(AN_API_KEY, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "apiKey"));
        assertNull(Whitebox.getInternalState(deviceInfoDatastreamsGetter, "serialNumber"));
    }
    
    @Test
    public void getDeviceIdReturnsDeviceIdIfConfigurationHasDeviceId() {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "deviceId", A_DEVICE_ID);

        String actual = deviceInfoDatastreamsGetter.getDeviceId();
        
        assertEquals(A_DEVICE_ID, actual);
    }
    
    @Test
    public void getDeviceIdReturnsSerialNumberIfConfigurationDoesntHaveDeviceId() {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "deviceId", null);
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "serialNumber", A_SERIAL_NUMBER);

        String actual = deviceInfoDatastreamsGetter.getDeviceId();
        
        assertEquals(A_SERIAL_NUMBER, actual);
    }
    
    @Test
    public void getApiKeyReturnsTheApiKeyOfTheConfigurationHandler() {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "apiKey", AN_API_KEY);

        String actual = deviceInfoDatastreamsGetter.getApiKey();
        
        assertEquals(AN_API_KEY, actual);
    }
    
    @Test
    public void idsSatisfiedForDeviceIdIsDeviceId() {
        String actual = deviceInfoDatastreamsGetter.getDatastreamsGetterForDeviceId().getDatastreamIdSatisfied();

        assertEquals(DEVICE_ID_DATASTREAM_ID, actual);
    }

    @Test
    public void idsSatisfiedForSerialNumberIsSerialNumber() {
        String actual = deviceInfoDatastreamsGetter.getDatastreamsGetterForSerialNumber().getDatastreamIdSatisfied();

        assertEquals(SERIAL_NUMBER_DATASTREAM_ID, actual);
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
    public void getOfDatastreamGetterForDeviceIdReturnsTheDeviceId() throws InterruptedException, ExecutionException {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "deviceId", A_DEVICE_ID);

        DatastreamsGetter getterForDeviceId = deviceInfoDatastreamsGetter.getDatastreamsGetterForDeviceId();
        CollectedValue collectedValue = getterForDeviceId.get(null).get();
        
        assertEquals(A_DEVICE_ID, collectedValue.getValue());
    }
    
    @Test
    public void getOfDatastreamGetterForSerialNumberReturnsTheSerialNumber()
            throws InterruptedException, ExecutionException {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "serialNumber", A_SERIAL_NUMBER);

        DatastreamsGetter getterForSerialNumber = deviceInfoDatastreamsGetter.getDatastreamsGetterForSerialNumber();
        CollectedValue collectedValue = getterForSerialNumber.get(null).get();
        
        assertEquals(A_SERIAL_NUMBER, collectedValue.getValue());
    }
}