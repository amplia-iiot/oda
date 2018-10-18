package es.amplia.oda.operation.get;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;
import es.amplia.oda.operation.api.OperationGetDeviceParameters.Result;
import es.amplia.oda.operation.api.OperationGetDeviceParameters.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OperationGetDeviceParametersImplTest {
	private static final String DEVICE_ID = "aDeviceId";
	private static final String ID1 = "id1";
	private static final String ID2 = "id2";
	private static final String ID3 = "id3";
	private static final String ID4 = "id4";
	private static final String[] FOUND_IDS = {ID1, ID2};
	private static final String[] NOT_FOUND_IDS = {ID3, ID4};
	private static final Set<String> DATASTREAMS_IDENTIFIERS = asSet(unionOf(FOUND_IDS, NOT_FOUND_IDS));
	private static final long AT = 10000;
	private static final Integer VAL_FOR_ID_1 = 42;
	private static final String VAL_FOR_ID_2 = "maria";
	private static final CollectedValue VALUE_FOR_ID_1 = new CollectedValue(AT, VAL_FOR_ID_1);
	private static final CollectedValue VALUE_FOR_ID_2 = new CollectedValue(AT, VAL_FOR_ID_2);

	private OperationGetDeviceParametersImpl get;
	private CompletableFuture<CollectedValue> futureForId1;
	private CompletableFuture<CollectedValue> futureForId2;
	
	@Mock
    private DatastreamsGetterFinder datastreamsGetterFinder;
	@Mock
    private DatastreamsGetter datastreamsGetterForId1;
	@Mock
    private DatastreamsGetter datastreamsGetterForId2;

	@SafeVarargs
	private static <T> Set<T> asSet(T... ts) {
		return new HashSet<>(Arrays.asList(ts));
	}

	@SuppressWarnings("SameParameterValue")
    private static String[] unionOf(String[] a, String[] b) {
		return Stream.concat(Stream.of(a), Stream.of(b)).toArray(String[]::new);
	}
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		get = new OperationGetDeviceParametersImpl(datastreamsGetterFinder);
		List<DatastreamsGetter> getters = Arrays.asList(datastreamsGetterForId1, datastreamsGetterForId2);
		when(datastreamsGetterFinder.getGettersSatisfying(DEVICE_ID, DATASTREAMS_IDENTIFIERS)).thenReturn(new DatastreamsGetterFinder.Return(getters, asSet(NOT_FOUND_IDS)));
		futureForId1 = new CompletableFuture<>();
		futureForId2 = new CompletableFuture<>();
		when(datastreamsGetterForId1.get(DEVICE_ID)).thenReturn(futureForId1);
		when(datastreamsGetterForId2.get(DEVICE_ID)).thenReturn(futureForId2);
		when(datastreamsGetterForId1.getDatastreamIdSatisfied()).thenReturn(ID1);
		when(datastreamsGetterForId2.getDatastreamIdSatisfied()).thenReturn(ID2);
	}

	@Test
	public void datastreamsGetterFinderIsUsedToGetAllGetters() {
		
		get.getDeviceParameters(DEVICE_ID, DATASTREAMS_IDENTIFIERS);
		
		verify(datastreamsGetterFinder).getGettersSatisfying(DEVICE_ID, DATASTREAMS_IDENTIFIERS);
	}
	
	@Test
	public void forEveryGetterFoundItGetFunctionIsCalled() {
		
		get.getDeviceParameters(DEVICE_ID, DATASTREAMS_IDENTIFIERS);
		
		verify(datastreamsGetterForId1).get(DEVICE_ID);
		verify(datastreamsGetterForId2).get(DEVICE_ID);
	}
	
	@Test
	public void theFutureReturnedCompletesWhenAllDatastreamsGettersFoundAreCompleted() {
		CompletableFuture<OperationGetDeviceParameters.Result> future = get.getDeviceParameters(DEVICE_ID, DATASTREAMS_IDENTIFIERS);
		
		assertFalse(future.isDone());
		
		futureForId1.complete(VALUE_FOR_ID_1);
		futureForId2.complete(VALUE_FOR_ID_2);
		
		assertTrue(future.isDone());
	}
	
	private OperationGetDeviceParameters.Result executeGetCompletingFutures() throws InterruptedException, ExecutionException {
		CompletableFuture<OperationGetDeviceParameters.Result> future = get.getDeviceParameters(DEVICE_ID, DATASTREAMS_IDENTIFIERS);
		futureForId1.complete(VALUE_FOR_ID_1);
		futureForId2.complete(VALUE_FOR_ID_2);
		return future.get();
	}
	
	@Test
	public void ifAGetterThrowsAnExceptionInGetMethodTheExceptionMessageIsStoredAsError() throws InterruptedException, ExecutionException {
		List<DatastreamsGetter> listOfGetterForId1 = Collections.singletonList(datastreamsGetterForId1);
		when(datastreamsGetterFinder.getGettersSatisfying(DEVICE_ID, asSet(ID1))).thenReturn(
			new DatastreamsGetterFinder.Return(listOfGetterForId1 , asSet())
		);
		String exceptionMessage = "whatever";
		when(datastreamsGetterForId1.get(DEVICE_ID)).thenThrow(new RuntimeException(exceptionMessage));
		
		OperationGetDeviceParameters.Result actual = get.getDeviceParameters(DEVICE_ID, asSet(ID1)).get();
		
		OperationGetDeviceParameters.Result expected = new OperationGetDeviceParameters.Result(
                Collections.singletonList(
                        new OperationGetDeviceParameters.GetValue(ID1, Status.PROCESSING_ERROR, null, exceptionMessage)
                )
		);
		assertEquals(expected, actual);
	}
	
	@Test
	public void notFoundIdsAreCopiedToResponse() throws InterruptedException, ExecutionException {
		OperationGetDeviceParameters.Result actual = executeGetCompletingFutures();
		
		OperationGetDeviceParameters.GetValue notFoundId3 = new OperationGetDeviceParameters.GetValue(ID3, OperationGetDeviceParameters.Status.NOT_FOUND, null, null);
		OperationGetDeviceParameters.GetValue notFoundId4 = new OperationGetDeviceParameters.GetValue(ID4, OperationGetDeviceParameters.Status.NOT_FOUND, null, null);
		assertThat(actual.getValues(), hasItems(notFoundId3, notFoundId4));
	}

	@Test
	public void recollectedValuesAreCopiedToResponse() throws InterruptedException, ExecutionException {
		OperationGetDeviceParameters.Result actual = executeGetCompletingFutures();
		
		OperationGetDeviceParameters.GetValue valueForId1 = new OperationGetDeviceParameters.GetValue(ID1, OperationGetDeviceParameters.Status.OK, VAL_FOR_ID_1, null);
		OperationGetDeviceParameters.GetValue valueForId2 = new OperationGetDeviceParameters.GetValue(ID2, OperationGetDeviceParameters.Status.OK, VAL_FOR_ID_2, null);
		assertThat(actual.getValues(), hasItems(valueForId1, valueForId2));
	}

	@Test
	public void ifAGetFutureCompletesWithExceptionTheErrorIsCopiedToResponse() throws InterruptedException, ExecutionException {
		CompletableFuture<OperationGetDeviceParameters.Result> future = get.getDeviceParameters(DEVICE_ID, DATASTREAMS_IDENTIFIERS);
		String exceptionMessage = "whatever";
		futureForId1.completeExceptionally(new RuntimeException(exceptionMessage));
		futureForId2.complete(VALUE_FOR_ID_2);
		Result actual = future.get();
		
		OperationGetDeviceParameters.GetValue valueForId1 = new OperationGetDeviceParameters.GetValue(ID1, OperationGetDeviceParameters.Status.PROCESSING_ERROR, null, exceptionMessage);
		assertThat(actual.getValues(), hasItem(valueForId1));
	}

}
