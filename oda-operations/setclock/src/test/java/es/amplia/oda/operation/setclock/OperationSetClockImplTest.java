package es.amplia.oda.operation.setclock;

import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.DatastreamValue.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.operation.setclock.OperationSetClockImpl.Result;
import static es.amplia.oda.operation.setclock.OperationSetClockImpl.ResultCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class OperationSetClockImplTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final Long TEST_TIMESTAMP = 123456789L;
    private static final String TEST_CLOCK_DATASTREAM = "sync";

    @Mock
    private StateManager mockedStateManager;
    @InjectMocks
    private OperationSetClockImpl testOperation;

    @Test
    public void testSetClock() throws ExecutionException, InterruptedException {
        Whitebox.setInternalState(testOperation, "clockDatastream", TEST_CLOCK_DATASTREAM);
        when(mockedStateManager.setDatastreamValue(anyString(), anyString(), any())).thenReturn(
                CompletableFuture.completedFuture(new DatastreamValue(TEST_DEVICE_ID, TEST_CLOCK_DATASTREAM, null,
                        System.currentTimeMillis(), TEST_TIMESTAMP, Status.OK, null, false, false)));

        CompletableFuture<Result> future = testOperation.setClock(TEST_DEVICE_ID, TEST_TIMESTAMP);
        Result result = future.get();

        assertEquals(ResultCode.SUCCESSFUL, result.getResultCode());
        assertNull(result.getResultDescription());
        verify(mockedStateManager).setDatastreamValue(eq(TEST_DEVICE_ID), eq(TEST_CLOCK_DATASTREAM), eq(TEST_TIMESTAMP));
    }

    @Test
    public void testSetClockStateManagerError() throws ExecutionException, InterruptedException {
        Whitebox.setInternalState(testOperation, "clockDatastream", TEST_CLOCK_DATASTREAM);
        String error = "Error setting value " + TEST_TIMESTAMP + " for " + TEST_CLOCK_DATASTREAM;

        when(mockedStateManager.setDatastreamValue(anyString(), anyString(), any())).thenReturn(
                CompletableFuture.completedFuture(new DatastreamValue(TEST_DEVICE_ID, TEST_CLOCK_DATASTREAM, null,
                        System.currentTimeMillis(), TEST_TIMESTAMP, Status.PROCESSING_ERROR, error, false, false)));

        CompletableFuture<Result> future = testOperation.setClock(TEST_DEVICE_ID, TEST_TIMESTAMP);
        Result result = future.get();

        assertEquals(ResultCode.ERROR_PROCESSING, result.getResultCode());
        assertEquals(error, result.getResultDescription());
        verify(mockedStateManager).setDatastreamValue(eq(TEST_DEVICE_ID), eq(TEST_CLOCK_DATASTREAM), eq(TEST_TIMESTAMP));
    }

    @Test
    public void testLoadConfiguration() {
        testOperation.loadConfiguration(TEST_CLOCK_DATASTREAM);

        assertEquals(TEST_CLOCK_DATASTREAM, Whitebox.getInternalState(testOperation, "clockDatastream"));
    }
}