package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.setclock.Datetime;
import es.amplia.oda.dispatcher.opengate.domain.setclock.ParameterSetClockOperation;
import es.amplia.oda.dispatcher.opengate.domain.setclock.RequestSetClockOperation;
import es.amplia.oda.operation.api.OperationSetClock;
import es.amplia.oda.operation.api.OperationSetClock.Result;
import es.amplia.oda.operation.api.OperationSetClock.ResultCode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.dispatcher.opengate.operation.processor.SetClockEquipmentProcessor.SET_CLOCK_EQUIPMENT_OPERATION_NAME;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SetClockEquipmentProcessorTest {

    private static final String TEST_ID = "testOperationId";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String[] TEST_PATH = new String[] { "path", "to", "device" };
    private static final Long TEST_TIMESTAMP = 123567789L;
    private static final ZoneId GMT_ZONE_ID = ZoneId.of("GMT+02:00");
    private static final String TEST_REQUEST_ID = "testRequest";
    private static final Datetime TEST_DATETIME = new Datetime();
    static {
        TEST_DATETIME.setDate(ZonedDateTime.now(GMT_ZONE_ID).toLocalDate().toString());
        TEST_DATETIME.setTime(ZonedDateTime.now(GMT_ZONE_ID).toLocalTime().toString());
        TEST_DATETIME.setTimezone(2);
        TEST_DATETIME.setDst(-1);
    }

    @Mock
    private OperationSetClock mockedOperationSetClock;
    @InjectMocks
    private SetClockEquipmentProcessor testProcessor;

    @Test
    public void testParseParametersWithDateAndTime() {
        ZonedDateTime dateTime = ZonedDateTime.now(GMT_ZONE_ID).plusHours(1);
        ParameterSetClockOperation parameters = new ParameterSetClockOperation(TEST_DATETIME);
        RequestSetClockOperation request = new RequestSetClockOperation(parameters);

        Long timestamp = testProcessor.parseParameters(request);

        assertNotNull(timestamp);
        assertTrue(Math.abs(timestamp - dateTime.toInstant().toEpochMilli()) < 1000);
    }

    @Test
    public void testParseParametersWithAllParams() {
        Datetime datetime = new Datetime();
        ZonedDateTime dateTime = ZonedDateTime.of(2019, 1, 1, 10, 0, 0, 0, ZoneId.of("Europe/Madrid"));
        datetime.setDate("2019-01-01");
        datetime.setTime("10:00:00");
        datetime.setTimezone(2);
        datetime.setDst(-1);
        RequestSetClockOperation request = new RequestSetClockOperation(new ParameterSetClockOperation(datetime));

        Long timestamp = testProcessor.parseParameters(request);

        assertNotNull(timestamp);
        assertEquals(dateTime.toInstant().toEpochMilli(), timestamp.longValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithNullParams() {
        RequestSetClockOperation request = new RequestSetClockOperation(null);

        testProcessor.parseParameters(request);
    }

    @Test
    public void testParseParametersWithEmptyParams() {
        ZonedDateTime dateTime = ZonedDateTime.now(GMT_ZONE_ID).plusHours(1);

        RequestSetClockOperation request = new RequestSetClockOperation(new ParameterSetClockOperation(new Datetime()));

        Long timestamp = testProcessor.parseParameters(request);

        assertNotNull(timestamp);
        assertTrue(timestamp >= dateTime.toInstant().toEpochMilli());
        assertTrue(timestamp <= ZonedDateTime.now(GMT_ZONE_ID).plusHours(2).toInstant().toEpochMilli());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithInvalidDateFormat() {
        Datetime datetime = new Datetime();
        datetime.setDate("this is not a date, sorry");
        datetime.setTime("09:00:00");
        RequestSetClockOperation request = new RequestSetClockOperation(new ParameterSetClockOperation(datetime));

        testProcessor.parseParameters(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithInvalidTimeFormat() {
        Datetime datetime = new Datetime();
        datetime.setDate("2019-01-01");
        datetime.setTime("ISO 8601 is too hard for me");
        RequestSetClockOperation request = new RequestSetClockOperation(new ParameterSetClockOperation(datetime));

        testProcessor.parseParameters(request);
    }

    @Test
    public void testProcessOperation() {
        CompletableFuture<Result> expectedResult =
                CompletableFuture.completedFuture(new Result(ResultCode.SUCCESSFUL, null));
        when(mockedOperationSetClock.setClock(anyString(), anyLong())).thenReturn(expectedResult);

        CompletableFuture<Result> result =
                testProcessor.processOperation(TEST_DEVICE_ID, TEST_ID, TEST_TIMESTAMP);

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