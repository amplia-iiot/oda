package es.amplia.oda.connector.dnp3;

import com.automatak.dnp3.*;
import com.automatak.dnp3.enums.CommandStatus;
import com.automatak.dnp3.enums.ControlCode;
import com.automatak.dnp3.enums.OperateType;
import es.amplia.oda.core.commons.interfaces.ScadaDispatcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static es.amplia.oda.core.commons.interfaces.ScadaDispatcher.ScadaOperation;
import static es.amplia.oda.core.commons.interfaces.ScadaDispatcher.ScadaOperationResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScadaCommandHandlerTest {

    private static final int TEST_INDEX = 1;

    @Mock
    private ScadaDispatcher mockedDispatcher;
    @InjectMocks
    private ScadaCommandHandler testHandler;

    @Test
    public void testStart() {
        testHandler.start();

        assertTrue("Nothing to test", true);
    }

    @SuppressWarnings("SameParameterValue")
    private <T, S> void testProcess(Function<T, CommandStatus> function, ScadaOperation operation, T value, S type) {
        when(mockedDispatcher.process(any(ScadaDispatcher.ScadaOperation.class), anyInt(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(ScadaOperationResult.SUCCESS));

        CommandStatus result = function.apply(value);

        assertEquals(CommandStatus.SUCCESS, result);
        verify(mockedDispatcher).process(eq(operation), eq(TEST_INDEX), eq(value), eq(type));
    }

    @Test
    public void testSelectCROB() {
        ControlRelayOutputBlock crob =
                new ControlRelayOutputBlock(ControlCode.LATCH_OFF, (short) 1, 1L, 2L, CommandStatus.SUCCESS);

        testProcess(value -> testHandler.selectCROB(value, TEST_INDEX), ScadaOperation.SELECT, crob, null);
    }

    @Test
    public void testSelectAOI32() {
        testProcess(value -> {
            AnalogOutputInt32 aoi32 = new AnalogOutputInt32(value, CommandStatus.SUCCESS);
            return testHandler.selectAOI32(aoi32, TEST_INDEX);
        }, ScadaOperation.SELECT, 10, null);
    }

    @Test
    public void testSelectAOI16() {
        testProcess(value -> {
            AnalogOutputInt16 aoi16 = new AnalogOutputInt16(value, CommandStatus.SUCCESS);
            return testHandler.selectAOI16(aoi16, TEST_INDEX);
        }, ScadaOperation.SELECT, (short) 2, null);
    }

    @Test
    public void testSelectAOF32() {
        testProcess(value -> {
            AnalogOutputFloat32 aof32 = new AnalogOutputFloat32(value, CommandStatus.SUCCESS);
            return testHandler.selectAOF32(aof32, TEST_INDEX);
        }, ScadaOperation.SELECT, 1.55f, null);
    }

    @Test
    public void testSelectAOD64() {
        testProcess(value -> {
            AnalogOutputDouble64 aod64 = new AnalogOutputDouble64(value, CommandStatus.SUCCESS);
            return testHandler.selectAOD64(aod64, TEST_INDEX);
        }, ScadaOperation.SELECT, 121.99, null);
    }

    @Test
    public void testOperateCROB() {
        ControlRelayOutputBlock crob =
                new ControlRelayOutputBlock(ControlCode.LATCH_OFF, (short) 1, 1L, 2L, CommandStatus.SUCCESS);

        testProcess(value -> testHandler.operateCROB(value, TEST_INDEX, OperateType.DirectOperate),
                ScadaOperation.DIRECT_OPERATE, crob, null);
    }

    @Test
    public void testOperateAOI32() {
        testProcess(value -> {
            AnalogOutputInt32 aoi32 = new AnalogOutputInt32(value, CommandStatus.SUCCESS);
            return testHandler.operateAOI32(aoi32, TEST_INDEX, OperateType.DirectOperate);
        }, ScadaOperation.DIRECT_OPERATE, 10, null);
    }

    @Test
    public void testOperateAOI16() {
        testProcess(value -> {
            AnalogOutputInt16 aoi16 = new AnalogOutputInt16(value, CommandStatus.SUCCESS);
            return testHandler.operateAOI16(aoi16, TEST_INDEX, OperateType.DirectOperateNoAck);
        }, ScadaOperation.DIRECT_OPERATE_NO_ACK, (short) 2, null);
    }

    @Test
    public void testOperateAOF32() {
        testProcess(value -> {
            AnalogOutputFloat32 aof32 = new AnalogOutputFloat32(value, CommandStatus.SUCCESS);
            return testHandler.operateAOF32(aof32, TEST_INDEX, OperateType.SelectBeforeOperate);
        }, ScadaOperation.SELECT_BEFORE_OPERATE, 1.55f, null);
    }

    @Test
    public void testOperateAOD64() {
        testProcess(value -> {
            AnalogOutputDouble64 aod64 = new AnalogOutputDouble64(value, CommandStatus.SUCCESS);
            return testHandler.operateAOD64(aod64, TEST_INDEX, OperateType.DirectOperate);
        }, ScadaOperation.DIRECT_OPERATE, 121.99, null);
    }

    @Test
    public void testOperationErrorResult() {
        AnalogOutputInt32 aoi32 = new AnalogOutputInt32(5, CommandStatus.SUCCESS);

        when(mockedDispatcher.process(any(ScadaDispatcher.ScadaOperation.class), anyInt(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(ScadaOperationResult.ERROR));

        CommandStatus result = testHandler.selectAOI32(aoi32, TEST_INDEX);

        assertEquals(CommandStatus.FORMAT_ERROR, result);
    }

    @Test
    public void testNotSupportedOperation() {
        AnalogOutputInt32 aoi32 = new AnalogOutputInt32(5, CommandStatus.SUCCESS);

        when(mockedDispatcher.process(any(ScadaDispatcher.ScadaOperation.class), anyInt(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(ScadaOperationResult.NOT_SUPPORTED));

        CommandStatus result = testHandler.selectAOI32(aoi32, TEST_INDEX);

        assertEquals(CommandStatus.NOT_SUPPORTED, result);
    }

    @Mock
    private CompletableFuture<ScadaOperationResult> futureWithException;

    @Test
    public void testOperationProcessException() throws ExecutionException, InterruptedException {
        AnalogOutputInt32 aoi32 = new AnalogOutputInt32(5, CommandStatus.SUCCESS);

        when(mockedDispatcher.process(any(ScadaDispatcher.ScadaOperation.class), anyInt(), any(), any()))
                .thenReturn(futureWithException);
        when(futureWithException.get()).thenThrow(new ExecutionException(null));

        CommandStatus result = testHandler.selectAOI32(aoi32, TEST_INDEX);

        assertEquals(CommandStatus.FORMAT_ERROR, result);
    }

    @Test
    public void testEnd() {
        testHandler.end();

        assertTrue("Nothing to test", true);
    }
}