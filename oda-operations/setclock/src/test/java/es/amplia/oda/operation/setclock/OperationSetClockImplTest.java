package es.amplia.oda.operation.setclock;

import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.DatastreamValue.Status;
import es.amplia.oda.statemanager.api.StateManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.operation.setclock.OperationSetClockImpl.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OperationSetClockImplTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final Long TEST_TIMESTAMP = 123456789L;

    @Mock
    private StateManager mockedStateManager;
    @InjectMocks
    private OperationSetClockImpl testOperation;

    @Test
    public void testSetClock() throws ExecutionException, InterruptedException {
        when(mockedStateManager.setDatastreamValue(anyString(), anyString(), any())).thenReturn(
                CompletableFuture.completedFuture(new DatastreamValue(TEST_DEVICE_ID, CLOCK_DATASTREAM,
                        System.currentTimeMillis(), TEST_TIMESTAMP, Status.OK, null, false)));

        CompletableFuture<Result> future = testOperation.setClock(TEST_DEVICE_ID, TEST_TIMESTAMP);
        Result result = future.get();

        assertEquals(ResultCode.SUCCESSFUL, result.getResultCode());
        assertNull(result.getResultDescription());
        verify(mockedStateManager).setDatastreamValue(eq(TEST_DEVICE_ID), eq(CLOCK_DATASTREAM), eq(TEST_TIMESTAMP));
    }

    @Test
    public void testSetClockStateManagerError() throws ExecutionException, InterruptedException {
        String error = "Error setting value " + TEST_TIMESTAMP + " for " + CLOCK_DATASTREAM;

        when(mockedStateManager.setDatastreamValue(anyString(), anyString(), any())).thenReturn(
                CompletableFuture.completedFuture(new DatastreamValue(TEST_DEVICE_ID, CLOCK_DATASTREAM,
                        System.currentTimeMillis(), TEST_TIMESTAMP, Status.PROCESSING_ERROR, error, false)));

        CompletableFuture<Result> future = testOperation.setClock(TEST_DEVICE_ID, TEST_TIMESTAMP);
        Result result = future.get();

        assertEquals(ResultCode.ERROR_PROCESSING, result.getResultCode());
        assertEquals(error, result.getResultDescription());
        verify(mockedStateManager).setDatastreamValue(eq(TEST_DEVICE_ID), eq(CLOCK_DATASTREAM), eq(TEST_TIMESTAMP));
    }
}