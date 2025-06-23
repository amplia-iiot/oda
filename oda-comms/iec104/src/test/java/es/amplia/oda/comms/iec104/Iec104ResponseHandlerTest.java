package es.amplia.oda.comms.iec104;

import es.amplia.oda.comms.iec104.master.Iec104ResponseHandler;
import es.amplia.oda.comms.iec104.types.MeasuredValueNormalizedSingle;
import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Iec104ResponseHandler.class)
public class Iec104ResponseHandlerTest {

    @Mock
    EventDispatcher mockedEventDispatcher;
    @Mock
    EventPublisher mockedEventPublisher;
    @Mock
    ChannelHandlerContext mockedChannelCtx;
    @Mock
    ScadaTableTranslator mockedScadaTablesTranslator;

    Iec104ResponseHandler responseHandler;

    Map<String, Iec104Cache> cache = new HashMap<>();
    String TEST_DEVICE_CONNECTION = "testDevice";
    String TEST_DEVICE = "testDevice";
    String TEST_DATASTREAM_ID = "testDatastreamId";
    String TEST_FEED = "testFeed";
    String TEST_EVENT_PUBLISH = "stateManager";
    Short TEST_VALUE = 2;
    Short TEST_COMMON_ADDRESS = 1;
    char[] TEST_QUALITY_BITS = {1, 1, 1, 1};
    boolean TEST_QUALITY_BITS_NOTIFY = false;
    int TEST_ASDU_ADDRRES = 10;
    ProtocolOptions protocolOptions;
    ASDUAddress commonAddress;


    @Before
    public void prepareForTest() {
        responseHandler = new Iec104ResponseHandler(cache, TEST_DEVICE_CONNECTION, TEST_COMMON_ADDRESS, TEST_QUALITY_BITS,
                TEST_QUALITY_BITS_NOTIFY, mockedEventDispatcher, mockedEventPublisher, mockedScadaTablesTranslator);

        // protocol options
        ProtocolOptions.Builder optionsBuilder = new ProtocolOptions.Builder();
        optionsBuilder.setAdsuAddressType(ASDUAddressType.SIZE_2);
        protocolOptions = optionsBuilder.build();

        // ASDU common address
        ByteBuf buffer =  Unpooled.buffer();
        buffer.writeShort(TEST_COMMON_ADDRESS); // commonAddress
        commonAddress = ASDUAddress.parse(protocolOptions, buffer);

        // ini caches
        cache.put(TEST_DEVICE, new Iec104Cache(TEST_DEVICE));
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
                new ScadaTableTranslator.ScadaTranslationInfo(TEST_DEVICE, TEST_DATASTREAM_ID, TEST_FEED, TEST_EVENT_PUBLISH);
        when(mockedScadaTablesTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(translationInfo);
        when(mockedScadaTablesTranslator.transformValue(anyInt(), any(), anyBoolean(), any())).thenReturn(TEST_VALUE);


        // call function
        responseHandler.channelRead(mockedChannelCtx, asduReceived);

        // check
        Map<Long, Object> datapointsMap = new HashMap<>();
        datapointsMap.put(asduTime, TEST_VALUE);
        Map<String, Map<Long, Object>> feedDatapointsMap = new HashMap<>();
        feedDatapointsMap.put(TEST_FEED, datapointsMap);
        Map<String, Map<String, Map<Long, Object>>> eventsByDatastreamId = new HashMap<>();
        eventsByDatastreamId.put(TEST_DATASTREAM_ID, feedDatapointsMap);
        verify(mockedEventPublisher).publishEvents(TEST_DEVICE, null, eventsByDatastreamId);
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
                new ScadaTableTranslator.ScadaTranslationInfo(null, TEST_DATASTREAM_ID, TEST_FEED, TEST_EVENT_PUBLISH);
        when(mockedScadaTablesTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(translationInfo);
        when(mockedScadaTablesTranslator.transformValue(anyInt(), any(), anyBoolean(), any())).thenReturn(TEST_VALUE);


        // call function
        responseHandler.channelRead(mockedChannelCtx, asduReceived);

        // check
        Map<Long, Object> datapointsMap = new HashMap<>();
        datapointsMap.put(asduTime, TEST_VALUE);
        Map<String, Map<Long, Object>> feedDatapointsMap = new HashMap<>();
        feedDatapointsMap.put(TEST_FEED, datapointsMap);
        Map<String, Map<String, Map<Long, Object>>> eventsByDatastreamId = new HashMap<>();
        eventsByDatastreamId.put(TEST_DATASTREAM_ID, feedDatapointsMap);
        verify(mockedEventPublisher).publishEvents(TEST_DEVICE, null, eventsByDatastreamId);
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

        // conditions
        ScadaTableTranslator.ScadaTranslationInfo translationInfo =
                new ScadaTableTranslator.ScadaTranslationInfo(TEST_DEVICE, TEST_DATASTREAM_ID, TEST_FEED, TEST_EVENT_PUBLISH);
        when(mockedScadaTablesTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(translationInfo);
        when(mockedScadaTablesTranslator.transformValue(anyInt(), any(), anyBoolean(), any())).thenReturn(TEST_VALUE);

        // call function
        responseHandler.channelRead(mockedChannelCtx, asduReceived);

        Iec104CacheValue expectedValue = new Iec104CacheValue(TEST_VALUE, asduTime, true);
        Iec104Cache deviceCache = cache.get(TEST_DEVICE);
        Iec104CacheValue valueFromCache = deviceCache.getValue("M_ME_NA_1", TEST_ASDU_ADDRRES);
        Assert.assertEquals(expectedValue.getValue(), valueFromCache.getValue());
        Assert.assertEquals(expectedValue.getValueTime(), valueFromCache.getValueTime());
    }

    @Test
    public void testChannelReadInterrogationCommandNoDeviceId(){
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

        // conditions
        ScadaTableTranslator.ScadaTranslationInfo translationInfo =
                new ScadaTableTranslator.ScadaTranslationInfo(null, TEST_DATASTREAM_ID, TEST_FEED, TEST_EVENT_PUBLISH);
        when(mockedScadaTablesTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(translationInfo);
        when(mockedScadaTablesTranslator.transformValue(anyInt(), any(), anyBoolean(), any())).thenReturn(TEST_VALUE);

        // call function
        responseHandler.channelRead(mockedChannelCtx, asduReceived);

        Iec104CacheValue expectedValue = new Iec104CacheValue(TEST_VALUE, asduTime, true);
        Iec104Cache deviceCache = cache.get(TEST_DEVICE_CONNECTION);
        Iec104CacheValue valueFromCache = deviceCache.getValue("M_ME_NA_1", TEST_ASDU_ADDRRES);
        Assert.assertEquals(expectedValue.getValue(), valueFromCache.getValue());
        Assert.assertEquals(expectedValue.getValueTime(), valueFromCache.getValueTime());
    }

}
