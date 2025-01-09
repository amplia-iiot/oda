package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.entities.Software;
import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import static es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DeviceInfoDatastreamsGetter.class)
public class DeviceInfoDatastreamsGetterTest {

    private static final String A_DEVICE_ID = "aDeviceId";
    private static final String A_SERIAL_NUMBER = "aSerialNumber";
    private static final String AN_API_KEY = "anApiKey";
    private static final String A_SOURCE = "aSource";
    private static final String A_PATH = "aPath";
    private static final DeviceInfoConfiguration TEST_CONFIGURATION =
            new DeviceInfoConfiguration(A_DEVICE_ID, AN_API_KEY, A_SOURCE, A_PATH, new HashMap<String, String>());

    @Mock
    private CommandProcessor mockedCommandProcessor;
    @InjectMocks
    private DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter;
    @Mock
    private File mockedFile;
    @Mock
    private Bundle mockedBundle;
    @Mock
    private BundleContext mockedContext;
    @Mock
    private CommandExecutionException mockedException;

    @Test
    public void loadConfigurationCachesValuesOfDeviceInfoConfiguration() throws Exception {
        when(mockedCommandProcessor.execute(anyString())).thenReturn(A_SERIAL_NUMBER);
        whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
        when(mockedFile.listFiles()).thenReturn(new File[0]);

        deviceInfoDatastreamsGetter.loadConfiguration(TEST_CONFIGURATION);

        verify(mockedCommandProcessor).execute(A_PATH + "/" + SERIAL_NUMBER_SCRIPT);
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

        verify(mockedCommandProcessor).execute(A_PATH + "/" + SERIAL_NUMBER_SCRIPT);
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
    public void getSoftware() {
        Bundle[] bundles = new Bundle[1];
        bundles[0] = mockedBundle;
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "context", mockedContext);
        when(mockedContext.getBundles()).thenReturn(bundles);
        when(mockedBundle.getSymbolicName()).thenReturn("name");
        when(mockedBundle.getVersion()).thenReturn(new Version("1"));

        List<Software> result = deviceInfoDatastreamsGetter.getSoftware();

        assertTrue(result.contains(new Software("name", "1.0.0", "SOFTWARE")));
        assertEquals(1, result.size());
    }

}