package es.amplia.oda.core.commons.entities;

public enum ContentType {
    CBOR,
    JSON,
    MESSAGE_PACK;

    public static final String PROPERTY_NAME = "contentType";

    public static ContentType getContentType(String contentTypeAsString) {
        switch (contentTypeAsString) {
            case "messagePack":
            case "msgPack":
                return ContentType.MESSAGE_PACK;
            default:
                return ContentType.valueOf(contentTypeAsString.toUpperCase());
        }
    }
}
