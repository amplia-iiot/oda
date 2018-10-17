package es.amplia.oda.core.commons.interfaces;

import java.util.concurrent.CompletableFuture;

public interface Dispatcher {
	CompletableFuture<byte[]> process(byte[] input) throws IllegalArgumentException;
}
