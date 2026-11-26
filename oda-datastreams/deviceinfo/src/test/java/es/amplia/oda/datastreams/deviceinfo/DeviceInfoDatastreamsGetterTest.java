package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.entities.Software;
import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfiguration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import static es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DeviceInfoDatastreamsGetterTest {

    private static final String A_DEVICE_ID = "aDeviceId";
    private static final String A_SERIAL_NUMBER = "aSerialNumber";
    private static final String AN_API_KEY = "anApiKey";
    private static final String A_SOURCE = "aSource";
    // A real empty directory instead of mocking File: mocking java.io.File with the inline
    // mock maker also intercepts the File instances the JDK class loader builds, which breaks
    // class loading in whatever test runs next (order-dependent NoClassDefFoundError).
    private Path scriptsDir;
    private String scriptsPath;
    private DeviceInfoConfiguration testConfiguration;

    @Mock
    private CommandProcessor mockedCommandProcessor;
    @InjectMocks
    private DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter;
    @Mock
    private Bundle mockedBundle;
    @Mock
    private BundleContext mockedContext;
    @Mock
    private CommandExecutionException mockedException;

    @BeforeEach
    public void createScriptsDir() throws IOException {
        scriptsDir = Files.createTempDirectory("oda-deviceinfo-test");
        scriptsPath = scriptsDir.toString();
        testConfiguration =
                new DeviceInfoConfiguration(A_DEVICE_ID, AN_API_KEY, A_SOURCE, scriptsPath, new HashMap<String, String>());
    }

    @AfterEach
    public void removeScriptsDir() throws IOException {
        if (scriptsDir != null) {
            try (java.util.stream.Stream<Path> paths = Files.walk(scriptsDir)) {
                paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        }
    }

    @Test
    public void loadConfigurationCachesValuesOfDeviceInfoConfiguration() throws Exception {
        when(mockedCommandProcessor.execute(anyString())).thenReturn(A_SERIAL_NUMBER);
        deviceInfoDatastreamsGetter.loadConfiguration(testConfiguration);

        verify(mockedCommandProcessor).execute(scriptsPath + "/" + SERIAL_NUMBER_SCRIPT);
        assertEquals(A_DEVICE_ID, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "deviceId"));
        assertEquals(AN_API_KEY, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "apiKey"));
        assertEquals(A_SERIAL_NUMBER, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "serialNumber"));
        assertEquals(scriptsPath, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "path"));
    }
    
    @Test
    public void loadConfigurationCaughtCommandProcessorException() throws Exception {
        doThrow(new CommandExecutionException("","", new RuntimeException())).when(mockedCommandProcessor)
                .execute(anyString());
        deviceInfoDatastreamsGetter.loadConfiguration(testConfiguration);

        verify(mockedCommandProcessor).execute(scriptsPath + "/" + SERIAL_NUMBER_SCRIPT);
        assertEquals(A_DEVICE_ID, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "deviceId"));
        assertEquals(AN_API_KEY, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "apiKey"));
        assertNull(Whitebox.getInternalState(deviceInfoDatastreamsGetter, "serialNumber"));
        assertEquals(scriptsPath, Whitebox.getInternalState(deviceInfoDatastreamsGetter, "path"));
    }
    
    @Test
    public void getDeviceIdReturnsDeviceIdIfConfigurationHasDeviceId() {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "deviceId", A_DEVICE_ID);

        String actual = deviceInfoDatastreamsGetter.getDeviceId();
        
        assertEquals(A_DEVICE_ID, actual);
    }
    
    @Test
    public void getDeviceIdReturnsSerialNumberIfConfigurationDoesntHaveDeviceId() {
        Whitebox.setInternalState(deviceInfoDatastreamsGetter, "deviceId", (String) null);
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