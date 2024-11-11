package es.amplia.oda.hardware.opcua.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.UaRuntimeException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodResult;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.amplia.oda.core.commons.opcua.OpcUaException;

public class OpcUaConnection implements es.amplia.oda.core.commons.opcua.OpcUaConnection {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaConnection.class);
    private static final String NODE_NOT_FOUND_MESSAGE = "Specified node not found";
    private static final String UNKOWN_FORMAT_NODEID = "Invalid format ID";

    private org.eclipse.milo.opcua.sdk.client.OpcUaClient client;

    public OpcUaConnection() {}

    @Override
    public String browse(String tab, String nodeId) throws OpcUaException {
        String errorMsg;
        try {
            NodeId id = NodeId.parse(nodeId);

            List<ReferenceDescription> nodes = client.getAddressSpace().browse(id);

            String treeResult = "";
            for (ReferenceDescription node : nodes) {
                treeResult += tab
                        + " Node="  + node.getBrowseName().getName()
                        + " ID=" + node.getNodeId().toParseableString() + "\n";
                // recursively browse to children
                treeResult += browse(tab + "  ", node.getNodeId().toParseableString());
            }

            return treeResult;
        } catch (UaException e) {
            errorMsg = "Browsing nodeId=" + nodeId + " failed: " + e.getMessage();
            logger.error(errorMsg);
        } catch (NullPointerException e) {
            errorMsg = NODE_NOT_FOUND_MESSAGE;
            logger.error(errorMsg);
        } catch (UaRuntimeException e) {
            errorMsg = UNKOWN_FORMAT_NODEID;
            logger.error(errorMsg);
        }
        throw new OpcUaException(errorMsg);
    }

    @Override
    public void connect() throws OpcUaException {
        if (client == null) throw new OpcUaException("Connection not created");
        try {
            client.connect().get();
        } catch (Exception e) {
            String msg = "Couldn't connect client with server";
            logger.error(msg);
            throw new OpcUaException(msg, e);
        }
    }

    @Override
    public void disconnect() throws OpcUaException {
        if (client == null) throw new OpcUaException("Connection not created");
        try {
            this.client.getSubscriptionManager().clearSubscriptions();
            this.client.disconnect().get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
            throw new OpcUaException(e.getMessage());
        }
    }

    @Override
    public String readNode(String nodeId) throws OpcUaException {
        if (client == null) throw new OpcUaException("Connection not created");
        NodeId id = NodeId.parse(nodeId);

        try {
            UaNode n = client.getAddressSpace().getNode(id);

            if(n == null) {
                throw new OpcUaException(NODE_NOT_FOUND_MESSAGE);
            }
            else {
                String result = "";
                result += ("ID of node: ns - " + n.getNodeId().getNamespaceIndex()
                        + "; " + n.getNodeId().getType()
                        + ":" + n.getNodeId().getIdentifier())
                        + "Name: " + n.getDisplayName().getText()
                        + "Description: " + n.getDescription().getText()
                        + "Browsename: " + n.getBrowseName().getName()
                        + "NodeClass: " + n.getNodeClass() + "\n";
                return result;
            }
        } catch (UaException e) {
            logger.error(e.getMessage());
            throw new OpcUaException(e.getMessage());
        } catch (UaRuntimeException e) {
            logger.error(e.getMessage());
            throw new OpcUaException(e.getMessage());
        }
    }

    @Override
    public Object readVariable(String nodeId) throws OpcUaException {
        if (client == null) throw new OpcUaException("Connection not created");
        try {
            NodeId id = NodeId.parse(nodeId);

            UaVariableNode n = client.getAddressSpace().getVariableNode(id);

            if(n == null) {
                new OpcUaException("Not valid ID or node is not a variable");
            }
            
            Object res = n.readValue().getValue().getValue();
            return res;
        } catch (UaException e) {
            logger.error(e.getMessage());
            throw new OpcUaException(e.getMessage());
        } catch (UaRuntimeException e) {
            logger.error(e.getMessage());
            throw new OpcUaException(e.getMessage());
        }
    }

    @Override
    public void writeVariable(String nodeId, Object value) throws OpcUaException {
        if (client == null) throw new OpcUaException("Connection not created");
        try {
            Variant v = new Variant(value);
            DataValue dv = new DataValue(v, null, null);
            NodeId id = NodeId.parse(nodeId);

            StatusCode sc = client.writeValue(id, dv).get();

            if (sc.isGood()) {
                logger.info("Node " + id + " was assigned value " + value);
            } else {
                throw new OpcUaException("Impossible assign new value to the node");
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
            throw new OpcUaException(e.getMessage());
        }
    }

    @Override
    public Object[] method(String objectIdStr, String methodIdStr, Object[] params) {
        if (client == null) throw new OpcUaException("Connection not created");
        try {
            NodeId objectId = NodeId.parse(objectIdStr);
            NodeId methodId = NodeId.parse(methodIdStr);
            ArrayList<Variant> paramsList = new ArrayList<>();

            Arrays.asList(params).forEach(p -> paramsList.add(new Variant(p)));

            CallMethodRequest request = new CallMethodRequest(
                objectId,
                methodId,
                (Variant[]) paramsList.toArray()
            );

            CallMethodResult result = client.call(request).get();

            if (result.getStatusCode().isGood()) {
                logger.info("Method " + methodIdStr + " was finished successful with params" + params);
                return result.getOutputArguments();
            } else {
                throw new OpcUaException("Impossible call method");
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
            throw new OpcUaException(e.getMessage());
        }
    }

    public void setClient(OpcUaClient client) {
        this.client = client;
    }
    
}
