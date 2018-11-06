package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.event.api.EventDispatcherProxy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

public class PollerImplTest {
    @SafeVarargs
    private static <T> Set<T> asSet(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }

    private static final DevicePattern DEVICE_ID_PATTERN = new DevicePattern("Device*");
    private static final String DEVICE_ID_1 = "Device1";
    private static final String DEVICE_ID_2 = "Device2";
    private static final String ID1 = "id1";
    private static final String ID2 = "id2";
    private static final Set<String> ID1_AND_ID2 = asSet(ID1, ID2);
    private static final String  VALUE_FOR_ID1 = "id1_value";
    private static final Integer VALUE_FOR_ID2 = 12;
    private static final long AT_FOR_ID1 = 1000;
    private static final long AT_FOR_ID2 = 2000;
    
    private static final List<String> DEVICE_ID_1_LIST = Collections.singletonList(DEVICE_ID_1);
    private static final List<String> DEVICE_ID_2_LIST = Collections.singletonList(DEVICE_ID_2);
    
    private static final CollectedValue COLLECTED_VALUE_OF_ID1 = new CollectedValue(AT_FOR_ID1, VALUE_FOR_ID1);
    private static final CollectedValue COLLECTED_VALUE_OF_ID2 = new CollectedValue(AT_FOR_ID2, VALUE_FOR_ID2);

    private CompletableFuture<CollectedValue> futureForId1;
    private CompletableFuture<CollectedValue> futureForId2;
    
    private PollerImpl collector;
    
    @Mock
    private DatastreamsGetterFinderImpl datastreamsGettersFinder;
    @Mock
    private DatastreamsGetter getterForId1;
    @Mock
    private DatastreamsGetter getterForId2;
    @Mock
    private EventDispatcherProxy eventDispatcher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        DatastreamsGetterFinder.Return gettersForId1AndId2 = new DatastreamsGetterFinder.Return(Arrays.asList(getterForId1, getterForId2), asSet());
        collector = new PollerImpl(datastreamsGettersFinder, eventDispatcher);
        futureForId1 = new CompletableFuture<>();
        futureForId2 = new CompletableFuture<>();
        
        when(datastreamsGettersFinder.getGettersSatisfying(DEVICE_ID_PATTERN, ID1_AND_ID2)).thenReturn(gettersForId1AndId2);
        when(getterForId1.getDatastreamIdSatisfied()).thenReturn(ID1);
        when(getterForId2.getDatastreamIdSatisfied()).thenReturn(ID2);
        when(getterForId1.getDevicesIdManaged()).thenReturn(DEVICE_ID_1_LIST);
        when(getterForId2.getDevicesIdManaged()).thenReturn(DEVICE_ID_2_LIST);
        when(getterForId1.get(DEVICE_ID_1)).thenReturn(futureForId1);
        when(getterForId2.get(DEVICE_ID_2)).thenReturn(futureForId2);
    }

    @Test
    public void runForGetsDatastreamsGettersFromDatastreamsGettersFinder() {
        collector.runFor(DEVICE_ID_PATTERN, ID1_AND_ID2);
        
        verify(datastreamsGettersFinder).getGettersSatisfying(DEVICE_ID_PATTERN, ID1_AND_ID2);
    }

    @Test
    public void afterDatastreamsGettersRunsCollectorStoresTheResults() {
        collector.runFor(DEVICE_ID_PATTERN, ID1_AND_ID2);
        futureForId1.complete(COLLECTED_VALUE_OF_ID1);
        futureForId2.complete(COLLECTED_VALUE_OF_ID2);
        
        verify(getterForId1).get(DEVICE_ID_1);
        verify(getterForId2).get(DEVICE_ID_2);
    }
    
    @Test
    public void ifADatastreamsGettersObtainsMoreValuesThanNeededOnlyWantedDatapointsAreStored() {
        when(getterForId1.getDatastreamIdSatisfied()).thenReturn(ID1);
        Set<String> listWithId1Only = asSet(ID1);
        when(datastreamsGettersFinder.getGettersSatisfying(DEVICE_ID_PATTERN, listWithId1Only))
                .thenReturn(new DatastreamsGetterFinder.Return(Collections.singletonList(getterForId1), asSet()));
        
        collector.runFor(DEVICE_ID_PATTERN, listWithId1Only);
        futureForId1.complete(
                new CollectedValue(AT_FOR_ID1, VALUE_FOR_ID1));
    }
    
    @Test
    public void ifThereAreNoGetterSatisfiyingAnIdListRunForDoesntDoAnything() {
        when(datastreamsGettersFinder.getGettersSatisfying(DEVICE_ID_PATTERN, ID1_AND_ID2))
                .thenReturn(new DatastreamsGetterFinder.Return(Collections.emptyList(), ID1_AND_ID2));
        
        collector.runFor(DEVICE_ID_PATTERN, ID1_AND_ID2);
        
        verify(getterForId1, never()).get(isA(String.class));
        verify(getterForId2, never()).get(isA(String.class));
    }
}
