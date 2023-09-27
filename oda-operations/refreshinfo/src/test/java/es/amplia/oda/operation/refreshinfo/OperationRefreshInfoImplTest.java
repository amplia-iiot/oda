package es.amplia.oda.operation.refreshinfo;

import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.operation.api.OperationRefreshInfo;
import es.amplia.oda.core.commons.utils.DatastreamValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationRefreshInfoImplTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_FEED = "testFeed";
    private static final String TEST_DATASTREAM_ID_1 = "d1";
    private static final String TEST_VALUE_1 = "Hello";
    private static final String TEST_DATASTREAM_ID_2 = "d2";
    private static final double TEST_VALUE_2 = 12.34;
    private static final String TEST_DATASTREAM_ID_3 = "d3";
    private static final String TEST_DATASTREAM_ID_4 = "d4";
    private static final long TEST_AT = System.currentTimeMillis();
    private static final String TEST_ERROR = "Error!";
    private static final Set<DatastreamValue> TEST_DATASTREAM_VALUES = new HashSet<>(
            Arrays.asList(
                    new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID_1, TEST_FEED, TEST_AT, TEST_VALUE_1, DatastreamValue.Status.OK, null, false, false),
                    new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID_2, TEST_FEED, TEST_AT, TEST_VALUE_2, DatastreamValue.Status.OK, null, false, false),
                    new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID_3, null, TEST_AT, null, DatastreamValue.Status.NOT_FOUND, null, false, false),
                    new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID_4, null, TEST_AT, null, DatastreamValue.Status.PROCESSING_ERROR, TEST_ERROR, false, false)
            )
    );
    private static final OperationRefreshInfo.RefreshInfoValue TEST_REFRESH_INFO_1 =
            new OperationRefreshInfo.RefreshInfoValue(
                    TEST_DATASTREAM_ID_1,
                    TEST_FEED,
                    OperationRefreshInfo.Status.OK,
                    TEST_AT, TEST_VALUE_1,
                    null
            );
    private static final OperationRefreshInfo.RefreshInfoValue TEST_REFRESH_INFO_2 =
            new OperationRefreshInfo.RefreshInfoValue(
                    TEST_DATASTREAM_ID_2,
                    TEST_FEED,
                    OperationRefreshInfo.Status.OK,
                    TEST_AT,
                    TEST_VALUE_2,
                    null
            );

    @Mock
    private StateManager mockedStateManager;
    @InjectMocks
    private OperationRefreshInfoImpl testRefreshInfo;

    @Test
    public void testRefreshInfo() throws ExecutionException, InterruptedException {
        when(mockedStateManager.getDeviceInformation(anyString()))
                .thenReturn(CompletableFuture.completedFuture(TEST_DATASTREAM_VALUES));

        CompletableFuture<OperationRefreshInfo.Result> future = testRefreshInfo.refreshInfo(TEST_DEVICE_ID);
        OperationRefreshInfo.Result result = future.get();

        assertNotNull(result);
        Map<String, List<OperationRefreshInfo.RefreshInfoValue>> datastreams = result.getValues();
        assertEquals(2, datastreams.size());
        assertTrue(datastreams.containsKey(TEST_DATASTREAM_ID_1));
        assertEquals(Collections.singletonList(TEST_REFRESH_INFO_1), datastreams.get(TEST_DATASTREAM_ID_1));
        assertTrue(datastreams.containsKey(TEST_DATASTREAM_ID_2));
        assertEquals(Collections.singletonList(TEST_REFRESH_INFO_2), datastreams.get(TEST_DATASTREAM_ID_2));
        verify(mockedStateManager).getDeviceInformation(eq(TEST_DEVICE_ID));
    }
}
