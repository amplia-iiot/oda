package es.amplia.oda.comms.iec104;

import es.amplia.oda.comms.iec104.codecs.*;
import es.amplia.oda.comms.iec104.master.Iec104ClientModule;
import es.amplia.oda.comms.iec104.slave.Iec104ServerModule;
import es.amplia.oda.comms.iec104.types.*;
import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.event.api.EventDispatcher;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageManager;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.*;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.client.Client;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;


@RunWith(PowerMockRunner.class)
@PrepareForTest(Iec104ServerModule.class)
public class Iec104ClientModuleTest {

    private Iec104ClientModule clientModule;
    private final Map<String, Iec104Cache> caches = new HashMap<>();
    @Mock
    private ProtocolOptions mockedOptions;
    @Mock
    private EventDispatcher mockedEventDispatcher;
    @Mock
    private EventPublisher mockedEventPublisher;
    @Mock
    private ScadaTableTranslator mockedScadaTranslator;
    @Mock
    private Client mockedClient;
    @Mock
    private MessageManager mockedManager;
    String deviceId = "testDevice";
    int commonAddress = 1;


    @Before
    public void prepareForTest() {
        clientModule = new Iec104ClientModule(caches, mockedOptions, deviceId, commonAddress,
                mockedEventDispatcher, mockedEventPublisher, mockedScadaTranslator);
    }

    @Test
    public void testInitializeClient() {
        clientModule.initializeClient(mockedClient, mockedManager);

        // single point information
        verify(mockedManager).registerCodec(eq(SinglePointInformationSingle.class.getAnnotation(ASDU.class).id()),
                eq(SinglePointInformationSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(SinglePointInformationSingleCodec.class));
        verify(mockedManager).registerCodec(eq(SinglePointInformationTimeSingle.class.getAnnotation(ASDU.class).id()),
                eq(SinglePointInformationTimeSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(SinglePointInformationTimeSingleCodec.class));
        verify(mockedManager).registerCodec(eq(SinglePointInformationSequence.class.getAnnotation(ASDU.class).id()),
                eq(SinglePointInformationSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(SinglePointInformationSequenceCodec.class));
        verify(mockedManager).registerCodec(eq(SinglePointInformationTimeSequence.class.getAnnotation(ASDU.class).id()),
                eq(SinglePointInformationTimeSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(SinglePointInformationTimeSequenceCodec.class));

        // DoublePointInformation
        verify(mockedManager).registerCodec(eq(DoublePointInformationSingle.class.getAnnotation(ASDU.class).id()),
                eq(DoublePointInformationSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(DoublePointInformationSingleCodec.class));
        verify(mockedManager).registerCodec(eq(DoublePointInformationTimeSingle.class.getAnnotation(ASDU.class).id()),
                eq(DoublePointInformationTimeSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(DoublePointInformationTimeSingleCodec.class));
        verify(mockedManager).registerCodec(eq(DoublePointInformationSequence.class.getAnnotation(ASDU.class).id()),
                eq(DoublePointInformationSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(DoublePointInformationSequenceCodec.class));
        verify(mockedManager).registerCodec(eq(DoublePointInformationTimeSequence.class.getAnnotation(ASDU.class).id()),
                eq(DoublePointInformationTimeSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(DoublePointInformationTimeSequenceCodec.class));

        // BitStringPointInformation
        verify(mockedManager).registerCodec(eq(BitStringPointInformationSingle.class.getAnnotation(ASDU.class).id()),
                eq(BitStringPointInformationSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(BitStringPointSingleCodec.class));
        verify(mockedManager).registerCodec(eq(BitStringPointInformationTimeSingle.class.getAnnotation(ASDU.class).id()),
                eq(BitStringPointInformationTimeSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(BitStringPointTimeSingleCodec.class));
        verify(mockedManager).registerCodec(eq(BitStringPointInformationSequence.class.getAnnotation(ASDU.class).id()),
                eq(BitStringPointInformationSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(BitStringPointSequenceCodec.class));
        verify(mockedManager).registerCodec(eq(BitStringPointInformationTimeSequence.class.getAnnotation(ASDU.class).id()),
                eq(BitStringPointInformationTimeSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(BitStringPointTimeSequenceCodec.class));

        // MeasuredValueScaled
        verify(mockedManager).registerCodec(eq(MeasuredValueScaledSingle.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueScaledSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueScaledSingleCodec.class));
        verify(mockedManager).registerCodec(eq(MeasuredValueScaledTimeSingle.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueScaledTimeSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueScaledTimeSingleCodec.class));
        verify(mockedManager).registerCodec(eq(MeasuredValueScaledSequence.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueScaledSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueScaledSequenceCodec.class));
        verify(mockedManager).registerCodec(eq(MeasuredValueScaledTimeSequence.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueScaledTimeSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueScaledTimeSequenceCodec.class));

        // MeasuredValueFloatingPoint
        verify(mockedManager).registerCodec(eq(MeasuredValueShortFloatingPointSingle.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueShortFloatingPointSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueFloatingPointSingleCodec.class));
        verify(mockedManager).registerCodec(eq(MeasuredValueShortFloatingPointTimeSingle.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueShortFloatingPointTimeSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueFloatingPointTimeSingleCodec.class));
        verify(mockedManager).registerCodec(eq(MeasuredValueShortFloatingPointSequence.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueShortFloatingPointSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueFloatingPointSequenceCodec.class));
        verify(mockedManager).registerCodec(eq(MeasuredValueShortFloatingPointTimeSequence.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueShortFloatingPointTimeSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueFloatingPointTimeSequenceCodec.class));

        // Commands
        verify(mockedManager).registerCodec(eq(InterrogationCommand.class.getAnnotation(ASDU.class).id()),
                eq(InterrogationCommand.class.getAnnotation(ASDU.class).informationStructure()),
                any(InterrogationCommandCodec.class));
        verify(mockedManager).registerCodec(eq(BitStringCommand.class.getAnnotation(ASDU.class).id()),
                eq(BitStringCommand.class.getAnnotation(ASDU.class).informationStructure()),
                any(BitStringCommandCodec.class));

        // Step position
        verify(mockedManager).registerCodec(eq(StepPositionSingle.class.getAnnotation(ASDU.class).id()),
                eq(StepPositionSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(StepPositionSingleCodec.class));
        verify(mockedManager).registerCodec(eq(StepPositionTimeSingle.class.getAnnotation(ASDU.class).id()),
                eq(StepPositionTimeSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(StepPositionTimeSingleCodec.class));
        verify(mockedManager).registerCodec(eq(StepPositionSequence.class.getAnnotation(ASDU.class).id()),
                eq(StepPositionSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(StepPositionSequenceCodec.class));
        verify(mockedManager).registerCodec(eq(StepPositionTimeSequence.class.getAnnotation(ASDU.class).id()),
                eq(StepPositionTimeSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(StepPositionTimeSequenceCodec.class));

        // MeasureValueNormalized
        verify(mockedManager).registerCodec(eq(MeasuredValueNormalizedSingle.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueNormalizedSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueNormalizedSingleCodec.class));
        verify(mockedManager).registerCodec(eq(MeasuredValueNormalizedTimeSingle.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueNormalizedTimeSingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueNormalizedTimeSingleCodec.class));
        verify(mockedManager).registerCodec(eq(MeasuredValueNormalizedSequence.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueNormalizedSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueNormalizedSequenceCodec.class));
        verify(mockedManager).registerCodec(eq(MeasuredValueNormalizedTimeSequence.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueNormalizedTimeSequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueNormalizedTimeSequenceCodec.class));

        // MeasureValueNormalizedNoQuality
        verify(mockedManager).registerCodec(eq(MeasuredValueNormalizedNoQualitySingle.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueNormalizedNoQualitySingle.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueNormalizedNoQualitySingleCodec.class));
        verify(mockedManager).registerCodec(eq(MeasuredValueNormalizedNoQualitySequence.class.getAnnotation(ASDU.class).id()),
                eq(MeasuredValueNormalizedNoQualitySequence.class.getAnnotation(ASDU.class).informationStructure()),
                any(MeasuredValueNormalizedNoQualitySequenceCodec.class));
    }

    @Test
    public void testInterrogationCommandScheduling(){

        // call function
        ScheduledFuture<?> futureTask = clientModule.addInterrogationCommandScheduling(100, 2000);
        Assert.assertNotNull(futureTask);

        ScheduledFuture<?> interrogationCommandTask = Whitebox.getInternalState(clientModule,"interrogationCommandTask");
        Assert.assertFalse(interrogationCommandTask.isCancelled());

        // cancel tasks
        clientModule.cancelInterrogationCommandScheduling();

        interrogationCommandTask = Whitebox.getInternalState(clientModule,"interrogationCommandTask");
        Assert.assertTrue(interrogationCommandTask.isCancelled());
    }
}
