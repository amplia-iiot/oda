package es.amplia.oda.hardware.atmanager.api;

import lombok.Data;

import java.util.List;

@Data
public class ATResponse {

    private final boolean isOk;
    private final String body;
    private final String errorMsg;
    private final Integer errorCode;
    private final List<ATEvent> partialResponses;

    private ATResponse(boolean isOk, String body, String errorMsg, Integer errorCode, List<ATEvent> partialResponses) {
        this.isOk = isOk;
        this.body = body;
        this.errorMsg = errorMsg;
        this.errorCode = errorCode;
        this.partialResponses = partialResponses;
    }

    public static ATResponse ok() {
        return new ATResponse(true, null, null, null, null);
    }

    public static ATResponse ok(String body) {
        return new ATResponse(true, body, null, null, null);
    }

    public static ATResponse ok(List<ATEvent> partialResponses) {
        return new ATResponse(true, null, null, null, partialResponses);
    }

    public static ATResponse ok(String body, List<ATEvent> partialResponses) {
        return new ATResponse(true, body, null, null, partialResponses);
    }

    public static ATResponse error() {
        return new ATResponse(false, null, null, null, null);
    }

    public static ATResponse error(int errorCode) {
        return new ATResponse(false, null, null, errorCode, null);
    }

    public static ATResponse error(String errorMsg) {
        return new ATResponse(false, null, errorMsg, null, null);
    }

    public String toWireString() {
        if (isOk()) {
            StringBuilder wireString = new StringBuilder();
            if (partialResponses != null) {
                for(ATEvent event : partialResponses) {
                    wireString.append(event.asWireString()).append("\r\n");
                }
            }
            if (body != null) {
                wireString.append(body);
            }
            wireString.append("\r\nOK\r\n");
            return wireString.toString();
        } else {
            if (errorMsg != null) {
                return "\r\nERROR: " + errorMsg + "\r\n";
            } else if (errorCode != null) {
                return "\r\nERROR: " + errorCode + "\r\n";
            } else {
                return "\r\nERROR\r\n";
            }
        }
    }

}
