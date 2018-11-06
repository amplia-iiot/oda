package es.amplia.oda.operation.get;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;
import es.amplia.oda.operation.get.DatastreamsGetterFinder.Return;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class DatastreamsGettersFinderImplTest {
    
    private DatastreamsGettersFinderImpl datastreamsGettersFinder;
    
    private static final String ID1= "id1";
    private static final String ID2= "id2";
    private static final String ID3= "id3";
    private static final String ID4= "id4";
    
    @Mock private DatastreamsGettersLocator datastreamsGettersLocator;
    @Mock private DatastreamsGetter datastreamsGetterForId1;
    @Mock private DatastreamsGetter datastreamsGetterForId2;
    @Mock private DatastreamsGetter datastreamsGetterForId3;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        datastreamsGettersFinder = new DatastreamsGettersFinderImpl(datastreamsGettersLocator);
        
        when(datastreamsGetterForId1.getDatastreamIdSatisfied()).thenReturn(ID1);
        when(datastreamsGetterForId2.getDatastreamIdSatisfied()).thenReturn(ID2);
        when(datastreamsGetterForId3.getDatastreamIdSatisfied()).thenReturn(ID3);
        
        when(datastreamsGetterForId1.getDevicesIdManaged()).thenReturn(Collections.singletonList(""));
        when(datastreamsGetterForId2.getDevicesIdManaged()).thenReturn(Collections.singletonList(""));
        when(datastreamsGetterForId3.getDevicesIdManaged()).thenReturn(Collections.singletonList(""));

        List<DatastreamsGetter> allGettersInSystem = Arrays.asList(datastreamsGetterForId1, datastreamsGetterForId2, datastreamsGetterForId3);
        when(datastreamsGettersLocator.getDatastreamsGetters()).thenReturn(allGettersInSystem);
    }

    @SafeVarargs
    private  static <T> Set<T> asSet(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }

    @Test
    public void withAnEmptySetOfIdentifiersNothingIsReturned() {
        
        Return actual = datastreamsGettersFinder.getGettersSatisfying("", asSet());
        
        assertTrue(actual.getGetters().isEmpty());
        assertTrue(actual.getNotFoundIds().isEmpty());
    }
    
    @Test
    public void ifThereAreNoDatastreamForTheOnlyOneIdSearchedThatIdIsReturnedInNotFoundIds() {
        when(datastreamsGettersLocator.getDatastreamsGetters()).thenReturn(Collections.emptyList());
        
        Return actual = datastreamsGettersFinder.getGettersSatisfying("", asSet(ID1));
        
        Return expected = new Return(Collections.emptyList(), asSet(ID1));
        assertEquals(expected, actual);
    }

    @Test
    public void ifThereIsADatastreamForTheOnlyOneIdSearchedThatIsReturned() {
        when(datastreamsGettersLocator.getDatastreamsGetters()).thenReturn(Collections.singletonList(datastreamsGetterForId1));
        
        Return actual = datastreamsGettersFinder.getGettersSatisfying("", asSet(ID1));
        
        Return expected = new Return(Collections.singletonList(datastreamsGetterForId1), asSet());
        assertEquals(expected, actual);
    }

    @Test
    public void ifThereAreNoDatastreamForTheOnlyOneIdSearchedThatManageTheDeviceIdThatIdIsReturnedInNotFoundIds() {
        when(datastreamsGettersLocator.getDatastreamsGetters()).thenReturn(Collections.singletonList(datastreamsGetterForId1));
        when(datastreamsGetterForId1.getDevicesIdManaged()).thenReturn(Collections.singletonList(""));
        
        Return actual = datastreamsGettersFinder.getGettersSatisfying("aDevice", asSet(ID1));
        
        Return expected = new Return(Collections.emptyList(), asSet(ID1));
        assertEquals(expected, actual);
    }
    
    @Test
    public void gettersThatDoesntManageAnyOfTheSpecifiedIdsAreNotReturned() {
        Return actual = datastreamsGettersFinder.getGettersSatisfying("", asSet(ID2, ID3, ID4));
        
        Return expected = new Return(Arrays.asList(datastreamsGetterForId2, datastreamsGetterForId3), asSet(ID4));
        assertEquals(expected, actual);
    }
    
    @Test
    public void ifAnyGetterInTheSystemThrowsTheExceptionIsNicelyHandled() {
        when(datastreamsGetterForId1.getDevicesIdManaged()).thenThrow(new IllegalArgumentException("whatever"));
        
        Return actual = datastreamsGettersFinder.getGettersSatisfying("", asSet(ID1, ID2));
        
        Return expected = new Return(Collections.emptyList(), asSet(ID1, ID2));
        assertEquals(expected, actual);
    }
}
