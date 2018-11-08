package es.amplia.oda.dispatcher.opengate;

interface JsonWriter {
    <T> byte[] dumpOutput(T output);
}
