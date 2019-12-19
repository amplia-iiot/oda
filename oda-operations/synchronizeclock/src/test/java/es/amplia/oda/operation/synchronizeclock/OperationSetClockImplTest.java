package es.amplia.oda.operation.synchronizeclock;

import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.statemanager.api.StateManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.operation.synchronizeclock.OperationSynchronizeClockImpl.*;
import static es.amplia.oda.core.commons.utils.DatastreamValue.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OperationSetClockImplTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_SOURCE = "testSource";

    @Mock
    private StateManager mockedStateManager;
    @InjectMocks
    private OperationSynchronizeClockImpl testOperation;

    @Captor
    private ArgumentCaptor<Long> timeCaptor;

    @Test
    public void testSynchronizeClock() throws ExecutionException, InterruptedException {
        long beforeTest = System.currentTimeMillis();

        when(mockedStateManager.setDatastreamValue(anyString(), anyString(), any())).thenReturn(
                CompletableFuture.completedFuture(
                        new DatastreamValue(TEST_DEVICE_ID, CLOCK_DATASTREAM, System.currentTimeMillis(),
                                System.currentTimeMillis(), Status.OK, null)));

        CompletableFuture<Result> future = testOperation.synchronizeClock(TEST_DEVICE_ID, TEST_SOURCE);
        Result result = future.get();

        assertEquals(ResultCode.SUCCESSFUL, result.getResultCode());
        assertNull(result.getResultDescription());
        verify(mockedStateManager).setDatastreamValue(eq(TEST_DEVICE_ID), eq(CLOCK_DATASTREAM), timeCaptor.capture());
        long time = timeCaptor.getValue();
        assertTrue(time >= beforeTest);
        assertTrue(time <= System.currentTimeMillis());
    }

    @Test
    public void testSynchronizeClockStateManagerError() throws ExecutionException, InterruptedException {
        long beforeTest = System.currentTimeMillis();
        String error = "Error setting datastream " + CLOCK_DATASTREAM;

        when(mockedStateManager.setDatastreamValue(anyString(), anyString(), any())).thenReturn(
                CompletableFuture.completedFuture(
                        new DatastreamValue(TEST_DEVICE_ID, CLOCK_DATASTREAM, System.currentTimeMillis(), null,
                                Status.PROCESSING_ERROR, error)));

        CompletableFuture<Result> future = testOperation.synchronizeClock(TEST_DEVICE_ID, TEST_SOURCE);
        Result result = future.get();

        assertEquals(ResultCode.ERROR_PROCESSING, result.getResultCode());
        assertEquals(error, result.getResultDescription());
        verify(mockedStateManager).setDatastreamValue(eq(TEST_DEVICE_ID), eq(CLOCK_DATASTREAM), timeCaptor.capture());
        long time = timeCaptor.getValue();
        assertTrue(time >= beforeTest);
        assertTrue(time <= System.currentTimeMillis());
    }
}