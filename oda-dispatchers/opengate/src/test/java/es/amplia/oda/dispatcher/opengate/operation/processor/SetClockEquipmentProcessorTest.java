package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.operation.api.OperationSetClock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.dispatcher.opengate.operation.processor.SetClockEquipmentProcessor.SET_CLOCK_EQUIPMENT_OPERATION_NAME;
import static es.amplia.oda.operation.api.OperationSetClock.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SetClockEquipmentProcessorTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String[] TEST_PATH = new String[] { "path", "to", "device" };
    private static final Long TEST_TIMESTAMP = 123567789L;
    private static final ZoneId GMT_ZONE_ID = ZoneId.of("GMT+00:00");
    private static final String TEST_REQUEST_ID = "testRequest";

    @Mock
    private OperationSetClock mockedOperationSetClock;
    @InjectMocks
    private SetClockEquipmentProcessor testProcessor;

    @Test
    public void testParseParametersWithDateAndTime() {
        ZonedDateTime dateTime = ZonedDateTime.now(GMT_ZONE_ID);
        Parameter dateParameter =
                new Parameter("date", null, new ValueObject(dateTime.toLocalDate().toString(), null, null, null));
        Parameter timeParameter =
                new Parameter("time", null, new ValueObject(dateTime.toLocalTime().toString(), null, null, null));
        Request request = new Request("id", System.currentTimeMillis(), "testDevice", null,
                SET_CLOCK_EQUIPMENT_OPERATION_NAME, Arrays.asList(dateParameter, timeParameter));

        Long timestamp = testProcessor.parseParameters(request);

        assertNotNull(timestamp);
        assertEquals(dateTime.toInstant().toEpochMilli(), timestamp.longValue());
    }

    @Test
    public void testParseParametersWithAllParams() {
        ZonedDateTime dateTime = ZonedDateTime.of(2019, 1, 1, 10, 0, 0, 0, ZoneId.of("Europe/Madrid"));
        Parameter dateParameter =
                new Parameter("date", null, new ValueObject("2019-01-01", null, null, null));
        Parameter timeParameter =
                new Parameter("time", null, new ValueObject("10:00:00", null, null, null));
        Parameter timezoneParameter =
                new Parameter("timezone", null, new ValueObject(null, 2.0, null, null));
        Parameter dstParameter =
                new Parameter("dst", null, new ValueObject(null, -1.0, null, null));
        Request request = new Request("id", System.currentTimeMillis(), "testDevice", null,
                SET_CLOCK_EQUIPMENT_OPERATION_NAME,
                Arrays.asList(dateParameter, timeParameter, timezoneParameter, dstParameter));

        Long timestamp = testProcessor.parseParameters(request);

        assertNotNull(timestamp);
        assertEquals(dateTime.toInstant().toEpochMilli(), timestamp.longValue());
    }

    @Test
    public void testParseParametersWithNullParams() {
        ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.now(),GMT_ZONE_ID);

        Request request = new Request("id", System.currentTimeMillis(), "testDevice", null,
                SET_CLOCK_EQUIPMENT_OPERATION_NAME, null);

        Long timestamp = testProcessor.parseParameters(request);

        assertNotNull(timestamp);
        assertTrue(timestamp >= dateTime.toInstant().toEpochMilli());
        assertTrue(timestamp <= ZonedDateTime.of(LocalDateTime.now(),GMT_ZONE_ID).toInstant().toEpochMilli());
    }

    @Test
    public void testParseParametersWithEmptyParams() {
        ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.now(),GMT_ZONE_ID);

        Request request = new Request("id", System.currentTimeMillis(), "testDevice", null,
                SET_CLOCK_EQUIPMENT_OPERATION_NAME, Collections.emptyList());

        Long timestamp = testProcessor.parseParameters(request);

        assertNotNull(timestamp);
        assertTrue(timestamp >= dateTime.toInstant().toEpochMilli());
        assertTrue(timestamp <= ZonedDateTime.of(LocalDateTime.now(),GMT_ZONE_ID).toInstant().toEpochMilli());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithInvalidDateType() {
        Parameter dateParameter =
                new Parameter("date", null, new ValueObject(null, 99.0, null, null));
        Parameter timeParameter =
                new Parameter("time", null, new ValueObject("09:00:00", null, null, null));
        Request request = new Request("id", System.currentTimeMillis(), "testDevice", null,
                SET_CLOCK_EQUIPMENT_OPERATION_NAME, Arrays.asList(dateParameter, timeParameter));

        testProcessor.parseParameters(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithInvalidDateFormat() {
        Parameter dateParameter =
                new Parameter("date", null, new ValueObject("invalid", null, null, null));
        Parameter timeParameter =
                new Parameter("time", null, new ValueObject("09:00:00", null, null, null));
        Request request = new Request("id", System.currentTimeMillis(), "testDevice", null,
                SET_CLOCK_EQUIPMENT_OPERATION_NAME, Arrays.asList(dateParameter, timeParameter));

        testProcessor.parseParameters(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithInvalidTimeType() {
        Parameter dateParameter =
                new Parameter("date", null, new ValueObject("2019-01-01", null, null, null));
        Parameter timeParameter =
                new Parameter("time", null, new ValueObject(null, 99.0, null, null));
        Request request = new Request("id", System.currentTimeMillis(), "testDevice", null,
                SET_CLOCK_EQUIPMENT_OPERATION_NAME, Arrays.asList(dateParameter, timeParameter));

        testProcessor.parseParameters(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithInvalidTimeFormat() {
        Parameter dateParameter =
                new Parameter("date", null, new ValueObject("2019-01-01", null, null, null));
        Parameter timeParameter =
                new Parameter("time", null, new ValueObject("invalid", null, null, null));
        Request request = new Request("id", System.currentTimeMillis(), "testDevice", null,
                SET_CLOCK_EQUIPMENT_OPERATION_NAME, Arrays.asList(dateParameter, timeParameter));

        testProcessor.parseParameters(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithInvalidTimezoneType() {
        Parameter dateParameter =
                new Parameter("date", null, new ValueObject("2019-01-01", null, null, null));
        Parameter timeParameter =
                new Parameter("time", null, new ValueObject("09:00", null, null, null));
        Parameter timezoneParameter =
                new Parameter("timezone", null, new ValueObject("+02:00", null, null, null));
        Parameter dstParameter =
                new Parameter("dst", null, new ValueObject(null, -1.0, null, null));
        Request request = new Request("id", System.currentTimeMillis(), "testDevice", null,
                SET_CLOCK_EQUIPMENT_OPERATION_NAME,
                Arrays.asList(dateParameter, timeParameter, timezoneParameter, dstParameter));

        testProcessor.parseParameters(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithInvalidDSTType() {
        Parameter dateParameter =
                new Parameter("date", null, new ValueObject("2019-01-01", null, null, null));
        Parameter timeParameter =
                new Parameter("time", null, new ValueObject("09:00", null, null, null));
        Parameter timezoneParameter =
                new Parameter("timezone", null, new ValueObject(null, 2.0, null, null));
        Parameter dstParameter =
                new Parameter("dst", null, new ValueObject("+01:00", null, null, null));
        Request request = new Request("id", System.currentTimeMillis(), "testDevice", null,
                SET_CLOCK_EQUIPMENT_OPERATION_NAME,
                Arrays.asList(dateParameter, timeParameter, timezoneParameter, dstParameter));

        testProcessor.parseParameters(request);
    }

    @Test
    public void testProcessOperation() {
        CompletableFuture<Result> expectedResult =
                CompletableFuture.completedFuture(new Result(ResultCode.SUCCESSFUL, null));
        when(mockedOperationSetClock.setClock(anyString(), anyLong())).thenReturn(expectedResult);

        CompletableFuture<Result> result =
                testProcessor.processOperation(TEST_DEVICE_ID, TEST_TIMESTAMP);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testTranslateToOutputSuccessful() {
        Result successResult = new Result(ResultCode.SUCCESSFUL, null);

        Output output = testProcessor.translateToOutput(successResult, TEST_REQUEST_ID, TEST_DEVICE_ID, TEST_PATH);
        Response response = output.getOperation().getResponse();
        List<Step> steps = response.getSteps();

        assertNotNull(output);
        assertEquals(OPENGATE_VERSION, output.getVersion());
        assertEquals(TEST_REQUEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID, response.getDeviceId());
        assertEquals(SET_CLOCK_EQUIPMENT_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.SUCCESSFUL, response.getResultCode());
        assertNull(response.getResultDescription());
        assertEquals(1, steps.size());
        assertEquals(SET_CLOCK_EQUIPMENT_OPERATION_NAME, steps.get(0).getName());
        assertEquals(StepResultCode.SUCCESSFUL, steps.get(0).getResult());
    }

    @Test
    public void testTranslateToOutputError() {
        String errorDescription = "Error description";
        Result errorResult = new Result(ResultCode.ERROR_PROCESSING, errorDescription);

        Output output = testProcessor.translateToOutput(errorResult, TEST_REQUEST_ID, TEST_DEVICE_ID, TEST_PATH);
        Response response = output.getOperation().getResponse();
        List<Step> steps = response.getSteps();

        assertNotNull(output);
        assertEquals(OPENGATE_VERSION, output.getVersion());
        assertEquals(TEST_REQUEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID, response.getDeviceId());
        assertEquals(SET_CLOCK_EQUIPMENT_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.ERROR_PROCESSING, response.getResultCode());
        assertEquals(errorDescription, response.getResultDescription());
        assertEquals(1, steps.size());
        assertEquals(SET_CLOCK_EQUIPMENT_OPERATION_NAME, steps.get(0).getName());
        assertEquals(StepResultCode.ERROR, steps.get(0).getResult());
        assertEquals(errorDescription, steps.get(0).getDescription());
    }
}