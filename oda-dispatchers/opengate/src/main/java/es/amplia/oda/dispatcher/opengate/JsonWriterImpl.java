package es.amplia.oda.dispatcher.opengate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonWriterImpl implements JsonWriter {

    private final Gson gson;

    JsonWriterImpl() {
        gson = new GsonBuilder().create();
    }

    @Override
    public <T> byte[] dumpOutput(T output) {
        String out = gson.toJson(output);
        return out.getBytes();
    }
}
