package es.amplia.oda.hardware.opcua.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaInfo;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaTranslationInfo;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableTranslatorProxy;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.event.api.EventDispatcher;
import es.amplia.oda.hardware.opcua.internal.OpcUaConnection;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem.ValueConsumer;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription.ItemCreationCallback;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OpcUaClientConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaClientConfigurationUpdateHandler.class);

    private static final String URL_PROPERTY_NAME = "url";
    private static final String PUBLISH_PROPERTY_NAME = "subscription.publish";
    private static final String DATAPOINTS_PROPERTY_NAME = "subscription.list";
    private static final String EMPTY_SCADA_TYPE = "*";

    private OpcUaConnection internalConnection;
    private EventDispatcher eventDispatcher;
    private ScadaTableTranslator scadaTranslator;
    private String url;
    private Integer publish;
    private OpcUaClient client;
    private List<MonitoredItemCreateRequest> requests = new ArrayList<>();
    private Long clientHandlerSeq;

    public OpcUaClientConfigurationUpdateHandler(EventDispatcher dispatcher, ScadaTableTranslatorProxy scadaTableTranslatorProxy, OpcUaConnection connection) {
        this.eventDispatcher = dispatcher;
        this.internalConnection = connection;
        this.scadaTranslator = scadaTableTranslatorProxy;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading new configuration");
        close();
        url = Optional.ofNullable((String)props.get(URL_PROPERTY_NAME))
                            .orElseThrow(() -> new ConfigurationException("Missing required parameter: " + URL_PROPERTY_NAME));
        publish = Optional.ofNullable((String)props.get(PUBLISH_PROPERTY_NAME)).map(Integer::parseInt)
                            .orElseThrow(() -> new ConfigurationException("Missing required parameter: " + PUBLISH_PROPERTY_NAME));
        String subscriptionListStr = Optional.ofNullable((String)props.get(DATAPOINTS_PROPERTY_NAME))
                            .orElseThrow(() -> new ConfigurationException("Missing required parameter: " + DATAPOINTS_PROPERTY_NAME));
        
        String[] arrayDp = subscriptionListStr.split("\\|");
        for (int i = 0; i < arrayDp.length; i++) {
            String[] meterParams = arrayDp[i].split(";");
            if (meterParams.length != 2) throw new ConfigurationException("Invalid meter configuration: " + arrayDp[i] + ", must be nodeId;samplingInterval");
            Integer nodeId = Integer.parseInt(meterParams[0]);
            Double samplingInterval = Double.parseDouble(meterParams[1]);
            requests.add(createRequest(nodeId, samplingInterval));
        }
                    
        LOGGER.info("New configuration loaded");
    }

    private MonitoredItemCreateRequest createRequest(Integer nodeId, Double samplingInterval) {
        ReadValueId readValueId = new ReadValueId(
             new NodeId(Unsigned.ushort(0), Unsigned.uint(nodeId)),
            AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE
        );

        MonitoringParameters parameters = new MonitoringParameters(
            UInteger.valueOf(clientHandlerSeq++),
            samplingInterval,     // sampling interval
            null,       // filter, null means use default
            UInteger.valueOf(10),   // queue size
            true        // discard oldest
        );

        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
            readValueId,
            MonitoringMode.Reporting,
            parameters
        );

        return request;
    }

    @Override
    public void applyConfiguration() {
        try {
            client = OpcUaClient.create(url);
            client.connect().get();

            internalConnection.setClient(client);

            UaSubscription subscription = client.getSubscriptionManager().createSubscription(publish).get();

            List<UaMonitoredItem> items = subscription.createMonitoredItems(
                TimestampsToReturn.Both,
                requests,
                new ItemCreationCallback() {
                    @Override
                    public void onItemCreated(UaMonitoredItem item, int index) {
                        item.setValueConsumer(new ValueConsumer() {

                            @Override
                            public void onValueArrived(UaMonitoredItem item, DataValue value) {
                                UInteger nodeId = (UInteger) item.getReadValueId().getNodeId().getIdentifier();
                                LOGGER.info("Subscription value received: NodeId=" + nodeId + ", value=" +  value.getValue());
                                ScadaTranslationInfo dsInfo = scadaTranslator.getTranslationInfo(new ScadaInfo(nodeId.intValue(), EMPTY_SCADA_TYPE), false);
                                Event e = new Event(dsInfo.getDatastreamId(), dsInfo.getDeviceId(), null, dsInfo.getFeed(), System.currentTimeMillis(), value.getValue().getValue());
                                LOGGER.info("Sending collection: " + e);
                                eventDispatcher.publish(Arrays.asList(e));
                            }
                            
                        });
                    }
                }
            ).get();

            for (UaMonitoredItem item : items) {
                if (item.getStatusCode().isGood()) {
                    LOGGER.info("item created for nodeId=" + item.getReadValueId().getNodeId());
                } else {
                    LOGGER.error(
                        "failed to create item for nodeId=" + item.getReadValueId().getNodeId() + "(status=" +  item.getStatusCode() + ")");
                }
            }
        } catch (Exception e) {
            
        }
    }

    public void close() {
        if (client != null) {
            client.getSubscriptionManager().cancelWatchdogTimers();
            client.disconnect();
        }
        requests.clear();
        clientHandlerSeq = 0l;
    }
}
