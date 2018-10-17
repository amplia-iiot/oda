package es.amplia.oda.dispatcher.opengate.domain;

public interface RequestVisitor {
    void visit(RequestGetDeviceParameters requestGetDeviceParameters);

    void visit(RequestRefreshInfo requestRefreshInfo);

    void visit(RequestSetDeviceParameters requestSetDeviceParameters);

    void visit(RequestUpdate requestUpdate);

    void visit(RequestOperationNotSupported requestOperationNotSupported);
}
