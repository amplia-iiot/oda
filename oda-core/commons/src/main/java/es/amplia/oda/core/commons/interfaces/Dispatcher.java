package es.amplia.oda.core.commons.interfaces;

import es.amplia.oda.core.commons.entities.ContentType;

import java.util.concurrent.CompletableFuture;

public interface Dispatcher {
    /**
     * Process the byte stream input coming from a connector
     * @param input Byte stream input coming from a connector
     * @param contentType Input message content type
     * @return Processed output as byte stream with the same content type of the input message
     * @throws IllegalArgumentException if the input is not valid
     */
    CompletableFuture<byte[]> process(byte[] input, ContentType contentType);

    /**
     * Process the byte stream input coming from a connector assuming JSON format.
     * @param input Byte stream input coming from a connector
     * @return Processed output as byte stream with JSON format.
     * @throws IllegalArgumentException if the input is not valid
     */
    default CompletableFuture<byte[]> process(byte[] input) {
        return process(input, ContentType.JSON);
    }
}
