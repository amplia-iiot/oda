package es.amplia.oda.comms.iec104;

import es.amplia.oda.comms.iec104.master.Iec104ResponseHandler;
import es.amplia.oda.comms.iec104.types.MeasuredValueNormalizedSingle;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.event.api.EventDispatcher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.eclipse.neoscada.protocol.iec60870.ASDUAddressType;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Iec104ResponseHandler.class)
public class Iec104ResponseHandlerTest {

    @Mock
    EventDispatcher mockedEventDispatcher;

    @Mock
    ChannelHandlerContext mockedChannelCtx;

    @Mock
    ScadaTableTranslator mockedScadaTablesTranslator;

    Iec104ResponseHandler responseHandler;

    Iec104Cache cache = new Iec104Cache();
    String TEST_DEVICE_CONNECTION = "testDevice";
    String TEST_DEVICE = "testDevice";
    String TEST_DATASTREAM_ID = "testDatastreamId";
    String TEST_FEED = "testFeed";
    Short TEST_VALUE = 2;
    Short TEST_COMMON_ADDRESS = 1;
    int TEST_ASDU_ADDRRES = 10;
    ProtocolOptions protocolOptions;
    ASDUAddress commonAddress;


    @Before
    public void prepareForTest() {
        responseHandler = new Iec104ResponseHandler(cache, TEST_DEVICE_CONNECTION, TEST_COMMON_ADDRESS,
                mockedEventDispatcher, mockedScadaTablesTranslator);

        // protocol options
        ProtocolOptions.Builder optionsBuilder = new ProtocolOptions.Builder();
        optionsBuilder.setAdsuAddressType(ASDUAddressType.SIZE_2);
        protocolOptions = optionsBuilder.build();

        // ASDU common address
        ByteBuf buffer =  Unpooled.buffer();
        buffer.writeShort(TEST_COMMON_ADDRESS); // commonAddress
        commonAddress = ASDUAddress.parse(protocolOptions, buffer);
    }

    @Test
    public void testChannelReadSpontaneous(){
        CauseOfTransmission transmissionCause = new CauseOfTransmission(() -> (short) 3); // 3 is spontaneous
        ASDUHeader asduHeader = new ASDUHeader(transmissionCause, commonAddress);

        // create ASDU message
        List<InformationEntry<Short>> valuesList = new ArrayList<>();
        InformationObjectAddress entryAddress = new InformationObjectAddress(TEST_ASDU_ADDRRES);
        long asduTime = System.currentTimeMillis();
        Value<Short> value = new Value<>(TEST_VALUE, asduTime, null);
        InformationEntry<Short> entry = new InformationEntry<>(entryAddress, value);
        valuesList.add(entry);
        Object asduReceived = MeasuredValueNormalizedSingle.create(asduHeader, valuesList);

        // conditions
        ScadaTableTranslator.ScadaTranslationInfo translationInfo =
                new ScadaTableTranslator.ScadaTranslationInfo(TEST_DEVICE, TEST_DATASTREAM_ID, TEST_FEED);
        when(mockedScadaTablesTranslator.getTranslationInfo(any())).thenReturn(translationInfo);
        when(mockedScadaTablesTranslator.transformValue(anyInt(), any(), any())).thenReturn(TEST_VALUE);


        // call function
        responseHandler.channelRead(mockedChannelCtx, asduReceived);

        // check
        List<Event> eventsToPublish = new ArrayList<>();
        Event eventExpected = new Event(TEST_DATASTREAM_ID, TEST_DEVICE, null, TEST_FEED, asduTime, TEST_VALUE);
        eventsToPublish.add(eventExpected);
        verify(mockedEventDispatcher).publishImmediately(eventsToPublish);
    }

    @Test
    public void testChannelReadSpontaneousNoDeviceId(){
        // when there is no deviceID assigned to the scada translation (scada tables), use the deviceId of the iec104 connection

        CauseOfTransmission transmissionCause = new CauseOfTransmission(() -> (short) 3); // 3 is spontaneous
        ASDUHeader asduHeader = new ASDUHeader(transmissionCause, commonAddress);

        // create ASDU message
        List<InformationEntry<Short>> valuesList = new ArrayList<>();
        InformationObjectAddress entryAddress = new InformationObjectAddress(TEST_ASDU_ADDRRES);
        long asduTime = System.currentTimeMillis();
        Value<Short> value = new Value<>(TEST_VALUE, asduTime, null);
        InformationEntry<Short> entry = new InformationEntry<>(entryAddress, value);
        valuesList.add(entry);
        Object asduReceived = MeasuredValueNormalizedSingle.create(asduHeader, valuesList);

        // conditions
        ScadaTableTranslator.ScadaTranslationInfo translationInfo =
                new ScadaTableTranslator.ScadaTranslationInfo(null, TEST_DATASTREAM_ID, TEST_FEED);
        when(mockedScadaTablesTranslator.getTranslationInfo(any())).thenReturn(translationInfo);
        when(mockedScadaTablesTranslator.transformValue(anyInt(), any(), any())).thenReturn(TEST_VALUE);


        // call function
        responseHandler.channelRead(mockedChannelCtx, asduReceived);

        // check
        List<Event> eventsToPublish = new ArrayList<>();
        Event eventExpected = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_CONNECTION, null, TEST_FEED, asduTime, TEST_VALUE);
        eventsToPublish.add(eventExpected);
        verify(mockedEventDispatcher).publishImmediately(eventsToPublish);
    }

    @Test
    public void testChannelReadInterrogationCommand(){
        CauseOfTransmission transmissionCause = new CauseOfTransmission(() -> (short) 20); // 20 is interrogation command
        ASDUHeader asduHeader = new ASDUHeader(transmissionCause, commonAddress);

        // create ASDU message
        List<InformationEntry<Short>> valuesList = new ArrayList<>();
        InformationObjectAddress entryAddress = new InformationObjectAddress(TEST_ASDU_ADDRRES);
        long asduTime = System.currentTimeMillis();
        Value<Short> value = new Value<>(TEST_VALUE, asduTime, null);
        InformationEntry<Short> entry = new InformationEntry<>(entryAddress, value);
        valuesList.add(entry);
        Object asduReceived = MeasuredValueNormalizedSingle.create(asduHeader, valuesList);

        // call function
        responseHandler.channelRead(mockedChannelCtx, asduReceived);

        Iec104CacheValue expectedValue = new Iec104CacheValue(TEST_VALUE, asduTime, true);
        Iec104CacheValue valueFromCache = cache.getValue("M_ME_NA_1", TEST_ASDU_ADDRRES);
        Assert.assertEquals(expectedValue.getValue(), valueFromCache.getValue());
        Assert.assertEquals(expectedValue.getValueTime(), valueFromCache.getValueTime());
    }

}
