package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.ParameterSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.RequestSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.ValueSetting;
import es.amplia.oda.operation.api.OperationSetClock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.*;
import java.util.ArrayList;
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
    private static final List<ValueSetting> TEST_CLOCK_LIST = new ArrayList<>();
    static {
        TEST_CLOCK_LIST.add(new ValueSetting("date", ZonedDateTime.now(GMT_ZONE_ID).toLocalDate().toString()));
        TEST_CLOCK_LIST.add(new ValueSetting("time", ZonedDateTime.now(GMT_ZONE_ID).toLocalTime().toString()));
    }

    @Mock
    private OperationSetClock mockedOperationSetClock;
    @InjectMocks
    private SetClockEquipmentProcessor testProcessor;

    @Test
    public void testParseParametersWithDateAndTime() {
        ZonedDateTime dateTime = ZonedDateTime.now(GMT_ZONE_ID);
        ParameterSetOrConfigureOperation parameters = new ParameterSetOrConfigureOperation(TEST_CLOCK_LIST);
        RequestSetOrConfigureOperation request = new RequestSetOrConfigureOperation(parameters);

        Long timestamp = testProcessor.parseParameters(request);

        assertNotNull(timestamp);
        assertTrue(Math.abs(timestamp - dateTime.toInstant().toEpochMilli()) < 100);
    }

    @Test
    public void testParseParametersWithAllParams() {
        List<ValueSetting> allParamsList = new ArrayList<>();
        ZonedDateTime dateTime = ZonedDateTime.of(2019, 1, 1, 10, 0, 0, 0, ZoneId.of("Europe/Madrid"));
        allParamsList.add(new ValueSetting("date", "2019-01-01"));
        allParamsList.add(new ValueSetting("time", "10:00:00"));
        allParamsList.add(new ValueSetting("timezone", "2.0"));
        allParamsList.add(new ValueSetting("dst", "-1.0"));
        RequestSetOrConfigureOperation request = new RequestSetOrConfigureOperation(new ParameterSetOrConfigureOperation(allParamsList));

        Long timestamp = testProcessor.parseParameters(request);

        assertNotNull(timestamp);
        assertEquals(dateTime.toInstant().toEpochMilli(), timestamp.longValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithNullParams() {
        ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.now(),GMT_ZONE_ID);

        RequestSetOrConfigureOperation request = new RequestSetOrConfigureOperation(null);

        Long timestamp = testProcessor.parseParameters(request);

        assertNotNull(timestamp);
        assertTrue(timestamp >= dateTime.toInstant().toEpochMilli());
        assertTrue(timestamp <= ZonedDateTime.of(LocalDateTime.now(),GMT_ZONE_ID).toInstant().toEpochMilli());
    }

    @Test
    public void testParseParametersWithEmptyParams() {
        ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.now(),GMT_ZONE_ID);

        RequestSetOrConfigureOperation request = new RequestSetOrConfigureOperation(new ParameterSetOrConfigureOperation(new ArrayList<>()));

        Long timestamp = testProcessor.parseParameters(request);

        assertNotNull(timestamp);
        assertTrue(timestamp >= dateTime.toInstant().toEpochMilli());
        assertTrue(timestamp <= ZonedDateTime.of(LocalDateTime.now(),GMT_ZONE_ID).toInstant().toEpochMilli());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithInvalidDateFormat() {
        List<ValueSetting> allParamsList = new ArrayList<>();
        allParamsList.add(new ValueSetting("date","this is not a date, sorry"));
        allParamsList.add(new ValueSetting("time","09:00:00"));
        RequestSetOrConfigureOperation request = new RequestSetOrConfigureOperation(new ParameterSetOrConfigureOperation(allParamsList));

        testProcessor.parseParameters(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithInvalidTimeFormat() {
        List<ValueSetting> allParamsList = new ArrayList<>();
        allParamsList.add(new ValueSetting("date","2019-01-01"));
        allParamsList.add(new ValueSetting("time","too late, we met one hour ago"));
        RequestSetOrConfigureOperation request = new RequestSetOrConfigureOperation(new ParameterSetOrConfigureOperation(allParamsList));

        testProcessor.parseParameters(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithInvalidTimezoneType() {
        List<ValueSetting> allParamsList = new ArrayList<>();
        allParamsList.add(new ValueSetting("date", "2019-01-01"));
        allParamsList.add(new ValueSetting("time", "10:00:00"));
        allParamsList.add(new ValueSetting("timezone", "Nepal"));
        allParamsList.add(new ValueSetting("dst", "-1.0"));
        RequestSetOrConfigureOperation request = new RequestSetOrConfigureOperation(new ParameterSetOrConfigureOperation(allParamsList));

        testProcessor.parseParameters(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersWithInvalidDSTType() {
        List<ValueSetting> allParamsList = new ArrayList<>();
        allParamsList.add(new ValueSetting("date", "2019-01-01"));
        allParamsList.add(new ValueSetting("time", "10:00:00"));
        allParamsList.add(new ValueSetting("timezone", "0.0"));
        allParamsList.add(new ValueSetting("dst", "No, we are in december"));
        RequestSetOrConfigureOperation request = new RequestSetOrConfigureOperation(new ParameterSetOrConfigureOperation(allParamsList));

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