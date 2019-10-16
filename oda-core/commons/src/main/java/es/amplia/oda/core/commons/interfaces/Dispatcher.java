package es.amplia.oda.core.commons.interfaces;

import java.util.concurrent.CompletableFuture;

public interface Dispatcher {
    /**
     * Process the byte stream input coming from a connector
     * @param input Byte stream input coming from a connector
     * @return Processed output as byte stream
     * @throws IllegalArgumentException if the input is not valid
     */
    CompletableFuture<byte[]> process(byte[] input);
}
