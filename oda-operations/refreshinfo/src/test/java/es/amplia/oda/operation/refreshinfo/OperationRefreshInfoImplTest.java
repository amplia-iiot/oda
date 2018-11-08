package es.amplia.oda.operation.refreshinfo;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;
import es.amplia.oda.operation.api.OperationRefreshInfo;
import es.amplia.oda.operation.api.OperationRefreshInfo.Result;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OperationRefreshInfoImplTest {
    
    private static final String DEVICE_ID = "aDeviceId";
    private static final List<String> LIST_WITH_DEVICE_ID = Collections.singletonList(DEVICE_ID);
    private static final String ID1 = "id1";
    private static final String ID2 = "id2";
    private static final String ID3 = "id3";
    private static final CollectedValue valueForId1 = new CollectedValue(1000L, "hi");
    private static final CollectedValue valueForId2 = new CollectedValue(2000L, 42);
    private static final CollectedValue valueForId3 = new CollectedValue(3000L, true);
    
    private OperationRefreshInfo operationRefreshInfo;
    
    @Mock
    private DatastreamsGettersLocator datastreamsGettersLocator;
    @Mock
    private DatastreamsGetter getterId1;
    @Mock
    private DatastreamsGetter getterId2;
    @Mock 
    private DatastreamsGetter getterId3;
    private CompletableFuture<CollectedValue> futureForId1;
    private CompletableFuture<CollectedValue> futureForId2;
    private CompletableFuture<CollectedValue> futureForId3;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        operationRefreshInfo = new OperationRefreshInfoImpl(datastreamsGettersLocator);
        
        when(datastreamsGettersLocator.getDatastreamsGetters()).thenReturn(Arrays.asList(getterId1, getterId2,getterId3));
        
        when(getterId1.getDatastreamIdSatisfied()).thenReturn(ID1);
        when(getterId2.getDatastreamIdSatisfied()).thenReturn(ID2);
        when(getterId3.getDatastreamIdSatisfied()).thenReturn(ID3);
        when(getterId1.getDevicesIdManaged()).thenReturn(LIST_WITH_DEVICE_ID);
        when(getterId2.getDevicesIdManaged()).thenReturn(LIST_WITH_DEVICE_ID);
        when(getterId3.getDevicesIdManaged()).thenReturn(LIST_WITH_DEVICE_ID);
        futureForId1 = new CompletableFuture<>();
        futureForId2 = new CompletableFuture<>();
        futureForId3 = new CompletableFuture<>();
        when(getterId1.get(DEVICE_ID)).thenReturn(futureForId1);
        when(getterId2.get(DEVICE_ID)).thenReturn(futureForId2);
        when(getterId3.get(DEVICE_ID)).thenReturn(futureForId3);
    }

    @Test
    public void datastreamsGettersLocatorIsUsedToGetACompleteListOfGetters() {
        operationRefreshInfo.refreshInfo(DEVICE_ID);
        
        verify(datastreamsGettersLocator).getDatastreamsGetters();
    }

    @Test
    public void forEveryGetFoundAGetIsIssued() {
        operationRefreshInfo.refreshInfo(DEVICE_ID);
        
        verify(getterId1).get(DEVICE_ID);
        verify(getterId2).get(DEVICE_ID);
        verify(getterId3).get(DEVICE_ID);
    }
    
    @Test
    public void gettersThatDoNotManageTheDeviceAreNotUsed() {
        when(getterId1.getDevicesIdManaged()).thenReturn(Collections.singletonList("otherDeviceId"));
        when(getterId2.getDevicesIdManaged()).thenReturn(LIST_WITH_DEVICE_ID);
        when(getterId3.getDevicesIdManaged()).thenReturn(LIST_WITH_DEVICE_ID);
        
        operationRefreshInfo.refreshInfo(DEVICE_ID);
        
        verify(getterId1, never()).get(DEVICE_ID);
        verify(getterId2).get(DEVICE_ID);
        verify(getterId3).get(DEVICE_ID);
    }
    
    @Test
    public void aCompletableFutureIsReturnedThatIsCompletedWhenAllGetsAreCompleted() {
        CompletableFuture<Result> future = operationRefreshInfo.refreshInfo(DEVICE_ID);
        
        assertFalse(future.isDone());
        futureForId1.complete(valueForId1);
        assertFalse(future.isDone());
        futureForId3.complete(valueForId3);
        assertFalse(future.isDone());
        futureForId2.complete(valueForId2);
        assertTrue(future.isDone());
    }
    
    @Test
    public void theResultHasAllValues() throws InterruptedException, ExecutionException {
        CompletableFuture<Result> future = operationRefreshInfo.refreshInfo(DEVICE_ID);
        futureForId1.complete(valueForId1);
        futureForId3.complete(valueForId3);
        futureForId2.complete(valueForId2);
        Result actual = future.get();
        
        assertEquals(valueForId1.getValue(), actual.getObtained().get(ID1));
        assertEquals(valueForId2.getValue(), actual.getObtained().get(ID2));
        assertEquals(valueForId3.getValue(), actual.getObtained().get(ID3));
    }

    @Test
    public void gettersThatCompleteExceptionallyAreNotStoredInResult() throws InterruptedException, ExecutionException {
        CompletableFuture<Result> future = operationRefreshInfo.refreshInfo(DEVICE_ID);
        futureForId1.complete(valueForId1);
        futureForId2.completeExceptionally(new Throwable());
        futureForId3.completeExceptionally(new Throwable());
        Result actual = future.get();
        
        assertEquals(1, actual.getObtained().size());
        assertEquals(valueForId1.getValue(), actual.getObtained().get(ID1));
    }

}
