package es.amplia.oda.operation.setclock;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
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
    private DatastreamsSettersFinder mockedFinder;
    @InjectMocks
    private OperationSetClockImpl testOperation;

    @Mock
    private DatastreamsSetter mockedSetter;

    @Test
    public void testSetClock() throws ExecutionException, InterruptedException {
        DatastreamsSettersFinder.Return satisfyingSetters =
                new DatastreamsSettersFinder.Return(Collections.singletonMap(CLOCK_DATASTREAM, mockedSetter),
                        Collections.emptySet());

        when(mockedFinder.getSettersSatisfying(anyString(), any())).thenReturn(satisfyingSetters);
        when(mockedSetter.set(anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<Result> future = testOperation.setClock(TEST_DEVICE_ID, TEST_TIMESTAMP);
        Result result = future.get();

        assertEquals(ResultCode.SUCCESSFUL, result.getResultCode());
        assertNull(result.getResultDescription());
        verify(mockedFinder).getSettersSatisfying(eq(TEST_DEVICE_ID), eq(Collections.singleton(CLOCK_DATASTREAM)));
        verify(mockedSetter).set(eq(TEST_DEVICE_ID), eq(TEST_TIMESTAMP));
    }

    @Test
    public void testSetClockDatastreamNotFound() throws ExecutionException, InterruptedException {
        DatastreamsSettersFinder.Return satisfyingSetters =
                new DatastreamsSettersFinder.Return(Collections.emptyMap(),
                        Collections.singleton(CLOCK_DATASTREAM));

        when(mockedFinder.getSettersSatisfying(anyString(), any())).thenReturn(satisfyingSetters);

        CompletableFuture<Result> future = testOperation.setClock(TEST_DEVICE_ID, TEST_TIMESTAMP);
        Result result = future.get();

        assertEquals(ResultCode.ERROR_PROCESSING, result.getResultCode());
        assertNotNull(result.getResultDescription());
        verify(mockedFinder).getSettersSatisfying(eq(TEST_DEVICE_ID), eq(Collections.singleton(CLOCK_DATASTREAM)));
        verifyZeroInteractions(mockedSetter);
    }

    @Test
    public void testSetClockExceptionSettingDatastream() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> futureWithException = new CompletableFuture<>();
        futureWithException.completeExceptionally(new RuntimeException("Exception"));

        DatastreamsSettersFinder.Return satisfyingSetters =
                new DatastreamsSettersFinder.Return(Collections.singletonMap(CLOCK_DATASTREAM, mockedSetter),
                        Collections.emptySet());

        when(mockedFinder.getSettersSatisfying(anyString(), any())).thenReturn(satisfyingSetters);
        when(mockedSetter.set(anyString(), any())).thenReturn(futureWithException);

        CompletableFuture<Result> future = testOperation.setClock(TEST_DEVICE_ID, TEST_TIMESTAMP);
        Result result = future.get();

        assertEquals(ResultCode.ERROR_PROCESSING, result.getResultCode());
        assertNotNull(result.getResultDescription());
        verify(mockedFinder).getSettersSatisfying(eq(TEST_DEVICE_ID), eq(Collections.singleton(CLOCK_DATASTREAM)));
        verify(mockedSetter).set(eq(TEST_DEVICE_ID), anyLong());
    }
}