package es.amplia.oda.core.commons.interfaces;

import es.amplia.oda.core.commons.entities.ContentType;

public interface SerializerProvider {
    Serializer getSerializer(ContentType contentType);
}
