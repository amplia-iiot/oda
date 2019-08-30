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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DeviceInfoDatastreamsGetter.class)
public class DeviceInfoDatastreamsGetterTest {

    private static final String A_DEVICE_ID = "aDeviceId";
    private static final String A_SERIAL_NUMBER = "aSerialNumber";
    private static final String AN_API_KEY = "anApiKey";
    private static final String A_PATH = "aPath";
    private static final String ECHO_COMMAND = "echo ";
    private static final String SERIAL_NUMBER_COMMAND = ECHO_COMMAND + A_SERIAL_NUMBER;
    private static final DeviceInfoConfiguration TEST_CONFIGURATION =
            new DeviceInfoConfiguration(A_DEVICE_ID, AN_API_KEY, SERIAL_NUMBER_COMMAND, A_PATH);

    @Mock
    private CommandProcessor mockedCommandProcessor;
    @InjectMocks
    private DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter;
    @Mock
    private File mockedFile;


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void loadConfigurationCachesValuesOfDeviceInfoConfiguration() throws Exception {
        when(mockedCommandProcessor.execute(anyString())).thenReturn(A_SERIAL_NUMBER);
        whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
        when(mockedFile.listFiles()).thenReturn(new File[0]);

        deviceInfoDatastreamsGetter.loadConfiguration(TEST_CONFIGURATION);

        verify(mockedCommandProcessor).execute(SERIAL_NUMBER_COMMAND);
        assertEquals(A_DEVICE_ID, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "deviceId"));
        assertEquals(AN_API_KEY, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "apiKey"));
        assertEquals(A_SERIAL_NUMBER, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "serialNumber"));
        assertEquals(A_PATH, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "path"));
    }
    
    @Test
    public void loadConfigurationCaughtCommandProcessorException() throws Exception {
        doThrow(new CommandExecutionException("","", new RuntimeException())).when(mockedCommandProcessor)
                .execute(anyString());
        whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
        when(mockedFile.listFiles()).thenReturn(new File[0]);
        
        deviceInfoDatastreamsGetter.loadConfiguration(TEST_CONFIGURATION);

        verify(mockedCommandProcessor).execute(SERIAL_NUMBER_COMMAND);
        assertEquals(A_DEVICE_ID, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "deviceId"));
        assertEquals(AN_API_KEY, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "apiKey"));
        assertNull(Whitebox.getInternalState(deviceInfoDatastreamsGetter, "serialNumber"));
        assertEquals(A_PATH, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "path"));
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

    @Test
    public void getCpuTotal() throws CommandExecutionException {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "commandProcessor", mockedCommandProcessor);
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "path", "script");
        when(mockedCommandProcessor.execute(any())).thenReturn("4");

        int result = deviceInfoDatastreamsGetter.getCpuTotal();

        verify(mockedCommandProcessor).execute("script/" + CPU_TOTAL_SCRIPT);
        assertEquals(4, result);
    }

    @Test
    public void getClock() throws CommandExecutionException {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "commandProcessor", mockedCommandProcessor);
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "path", "script");
        when(mockedCommandProcessor.execute(any())).thenReturn("12:34:56");

        String result = deviceInfoDatastreamsGetter.getClock();

        verify(mockedCommandProcessor).execute("script/" + CLOCK_SCRIPT);
        assertEquals("12:34:56", result);
    }

    @Test
    public void getUptime() throws CommandExecutionException {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "commandProcessor", mockedCommandProcessor);
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "path", "script");
        when(mockedCommandProcessor.execute(any())).thenReturn("1234");

        long result = deviceInfoDatastreamsGetter.getUptime();

        verify(mockedCommandProcessor).execute("script/" + UPTIME_SCRIPT);
        assertEquals(1234L, result);
    }

    @Test
    public void getCpuStatus() throws CommandExecutionException {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "commandProcessor", mockedCommandProcessor);
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "path", "script");
        when(mockedCommandProcessor.execute(any())).thenReturn("OK");

        String result = deviceInfoDatastreamsGetter.getCpuStatus();

        verify(mockedCommandProcessor).execute("script/" + CPU_STATUS_SCRIPT);
        assertEquals("OK", result);
    }

    @Test
    public void getCpuUsage() throws CommandExecutionException {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "commandProcessor", mockedCommandProcessor);
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "path", "script");
        when(mockedCommandProcessor.execute(any())).thenReturn("15");

        int result = deviceInfoDatastreamsGetter.getCpuUsage();

        verify(mockedCommandProcessor).execute("script/" + CPU_USAGE_SCRIPT);
        assertEquals(15, result);
    }

    @Test
    public void getRamTotal() throws CommandExecutionException {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "commandProcessor", mockedCommandProcessor);
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "path", "script");
        when(mockedCommandProcessor.execute(any())).thenReturn("1024");

        long result = deviceInfoDatastreamsGetter.getRamTotal();

        verify(mockedCommandProcessor).execute("script/" + RAM_TOTAL_SCRIPT);
        assertEquals(1024L, result);
    }

    @Test
    public void getRamUsage() throws CommandExecutionException {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "commandProcessor", mockedCommandProcessor);
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "path", "script");
        when(mockedCommandProcessor.execute(any())).thenReturn("54");

        int result = deviceInfoDatastreamsGetter.getRamUsage();

        verify(mockedCommandProcessor).execute("script/" + RAM_USAGE_SCRIPT);
        assertEquals(54, result);
    }

    @Test
    public void getDiskTotal() throws CommandExecutionException {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "commandProcessor", mockedCommandProcessor);
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "path", "script");
        when(mockedCommandProcessor.execute(any())).thenReturn("2048");

        long result = deviceInfoDatastreamsGetter.getDiskTotal();

        verify(mockedCommandProcessor).execute("script/" + DISK_TOTAL_SCRIPT);
        assertEquals(2048L, result);
    }

    @Test
    public void getDiskUsage() throws CommandExecutionException {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "commandProcessor", mockedCommandProcessor);
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "path", "script");
        when(mockedCommandProcessor.execute(any())).thenReturn("98");

        int result = deviceInfoDatastreamsGetter.getDiskUsage();

        verify(mockedCommandProcessor).execute("script/" + DISK_USAGE_SCRIPT);
        assertEquals(98, result);
    }
}