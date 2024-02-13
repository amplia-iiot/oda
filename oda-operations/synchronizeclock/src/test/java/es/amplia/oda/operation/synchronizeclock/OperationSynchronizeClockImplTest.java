package es.amplia.oda.operation.synchronizeclock;

import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.core.commons.utils.DatastreamValue.Status;
import static es.amplia.oda.operation.synchronizeclock.OperationSynchronizeClockImpl.Result;
import static es.amplia.oda.operation.synchronizeclock.OperationSynchronizeClockImpl.ResultCode;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class OperationSynchronizeClockImplTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_SOURCE = "testSource";
    private static final String TEST_CLOCK_DATASTREAM = "sync";

    @Mock
    private StateManager mockedStateManager;
    @InjectMocks
    private OperationSynchronizeClockImpl testOperation;

    @Captor
    private ArgumentCaptor<Long> timeCaptor;

    @Test
    public void testSynchronizeClock() throws ExecutionException, InterruptedException {
        Whitebox.setInternalState(testOperation, "clockDatastream", TEST_CLOCK_DATASTREAM);
        long beforeTest = System.currentTimeMillis();

        when(mockedStateManager.setDatastreamValue(anyString(), anyString(), any())).thenReturn(
                CompletableFuture.completedFuture(
                        new DatastreamValue(TEST_DEVICE_ID, TEST_CLOCK_DATASTREAM, null, System.currentTimeMillis(),
                                System.currentTimeMillis(), Status.OK, null, false, false)));

        CompletableFuture<Result> future = testOperation.synchronizeClock(TEST_DEVICE_ID, TEST_SOURCE);
        Result result = future.get();

        assertEquals(ResultCode.SUCCESSFUL, result.getResultCode());
        assertNull(result.getResultDescription());
        verify(mockedStateManager).setDatastreamValue(eq(TEST_DEVICE_ID), eq(TEST_CLOCK_DATASTREAM), timeCaptor.capture());
        long time = timeCaptor.getValue();
        assertTrue(time >= beforeTest);
        assertTrue(time <= System.currentTimeMillis());
    }

    @Test
    public void testSynchronizeClockStateManagerError() throws ExecutionException, InterruptedException {
        Whitebox.setInternalState(testOperation, "clockDatastream", TEST_CLOCK_DATASTREAM);
        long beforeTest = System.currentTimeMillis();
        String error = "Error setting datastream " + TEST_CLOCK_DATASTREAM;

        when(mockedStateManager.setDatastreamValue(anyString(), anyString(), any())).thenReturn(
                CompletableFuture.completedFuture(
                        new DatastreamValue(TEST_DEVICE_ID, TEST_CLOCK_DATASTREAM, null, System.currentTimeMillis(), null,
                                Status.PROCESSING_ERROR, error, false, false)));

        CompletableFuture<Result> future = testOperation.synchronizeClock(TEST_DEVICE_ID, TEST_SOURCE);
        Result result = future.get();

        assertEquals(ResultCode.ERROR_PROCESSING, result.getResultCode());
        assertEquals(error, result.getResultDescription());
        verify(mockedStateManager).setDatastreamValue(eq(TEST_DEVICE_ID), eq(TEST_CLOCK_DATASTREAM), timeCaptor.capture());
        long time = timeCaptor.getValue();
        assertTrue(time >= beforeTest);
        assertTrue(time <= System.currentTimeMillis());
    }

    @Test
    public void testLoadConfiguration() {
        testOperation.loadConfiguration(TEST_CLOCK_DATASTREAM);

        assertEquals(TEST_CLOCK_DATASTREAM, Whitebox.getInternalState(testOperation, "clockDatastream"));
    }
}