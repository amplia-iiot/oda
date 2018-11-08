package es.amplia.oda.connector.thingstream;

import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration;
import es.amplia.oda.connector.thingstream.mqttsn.USSDModemImplementation;

import com.myriadgroup.iot.sdk.client.impl.mqtt.MQTTPahoMessageClientImpl;
import com.myriadgroup.iot.sdk.client.impl.mqttsn.MQTTSNModemMessageClientImpl;
import com.myriadgroup.iot.sdk.client.impl.ussd.USSDModemProtocolLayerImpl;
import com.myriadgroup.iot.sdk.client.message.IMessageClient;
import com.myriadgroup.iot.sdk.client.modem.IModem;
import com.myriadgroup.iot.sdk.client.protocol.layer.IProtocolLayer;
import com.myriadgroup.iot.sdk.model.impl.IoTSDKPackets;

class MessageClientFactory {

    // Hide constructor
    private MessageClientFactory() {}

    static IMessageClient createMessageClient(ConnectorConfiguration configuration, ATManager atManager) {
        switch (configuration.getClientType()) {
            case MQTTSN:
                return createMqttSnClient(configuration, atManager);
            case MQTT:
                return createMqttClient(configuration);
            default:
                throw new IllegalArgumentException("Unknown client type");
        }
    }

    private static IMessageClient createMqttSnClient(ConnectorConfiguration configuration, ATManager atManager) {
        IModem modem = new USSDModemImplementation(atManager);
        IProtocolLayer<IoTSDKPackets> protocolLayer = new USSDModemProtocolLayerImpl(modem, configuration.getShortCode());
        return new MQTTSNModemMessageClientImpl(configuration.getClientId(), protocolLayer);
    }

    private static IMessageClient createMqttClient(ConnectorConfiguration configuration) {
        return new MQTTPahoMessageClientImpl(configuration.getMqttHost(), configuration.getMqttPort(),
                configuration.getMqttClientId(), configuration.getMqttUsername(), configuration.getMqttPassword());
    }
}
