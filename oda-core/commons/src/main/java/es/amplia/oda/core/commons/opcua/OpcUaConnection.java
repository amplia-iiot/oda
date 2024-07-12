package es.amplia.oda.core.commons.opcua;


public interface OpcUaConnection {

    void connect() throws OpcUaException;
    String browse(String tab, String nodeId) throws OpcUaException;
    String readNode(String nodeId) throws OpcUaException;
    Object readVariable(String nodeId) throws OpcUaException;
    void writeVariable(String nodeId, Object value) throws OpcUaException;
    Object[] method(String objectId, String methodId, Object[] params) throws OpcUaException;
    void disconnect() throws OpcUaException;

}
