package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DatastreamSetterTypeMapper;
import es.amplia.oda.dispatcher.opengate.domain.*;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

class JsonParserImpl implements JsonParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonParserImpl.class);

    private static final String ID_ELEMENT = "id";
    private static final String DEVICE_ID_ELEMENT = "deviceId";
    private static final String TIMESTAMP_ELEMENT = "timestamp";
    private static final String PARAMETERS_ELEMENT = "parameters";
    private static final String VARIABLE_NAME_ELEMENT = "variableName";
    private static final String VARIABLE_VALUE_ELEMENT = "variableValue";

    private final CharsetDecoder decoder;
    private final Gson gson;

    JsonParserImpl(DatastreamSetterTypeMapper datastreamsTypeMapper) {
        this.decoder = Charset.forName("UTF-8").newDecoder();
        this.gson = new GsonBuilder().
                registerTypeHierarchyAdapter(Request.class, new RequestDeserializer()).
                registerTypeHierarchyAdapter(RequestSetDeviceParameters.VariableListElement.class,
                        new SetVariableListElementDeserializer(datastreamsTypeMapper)).
                create();
    }

    @Override
    public Input parseInput(byte[] input) {
        if (input == null) throw new IllegalArgumentException("Bad input: null");

        String json = convertByteArrayToStringCheckingErrors(input);
        try {
            LOGGER.debug("Translating '{}' to Operation", json);
            return gson.fromJson(json, Input.class);
        } catch (JsonParseException e) {
            LOGGER.info("Cannot parse message '{}' as json: {}", json, e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private String convertByteArrayToStringCheckingErrors(byte[] input) {
        try {
            CharBuffer buffer = decoder.decode(ByteBuffer.wrap(input));
            return buffer.toString();
        } catch (CharacterCodingException e) {
            LOGGER.info("Cannot parse bytes '{}' as UTF-8 String.", input);
            throw new IllegalArgumentException("Input string is not UTF-8 encoded");
        }
    }

    private static class RequestDeserializer implements JsonDeserializer<Request> {
        @Override
        public Request deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
            JsonObject requestObject = jsonElement.getAsJsonObject();
            JsonElement nameElement = requestObject.get("name");
            if (nameElement == null)
                throw new JsonParseException("The field 'name' is not present in the request object");
            String name = nameElement.getAsString();

            JsonElement idElement = requestObject.get(ID_ELEMENT);
            String id = idElement == null ? null : idElement.getAsString();
            JsonElement timestampElement = requestObject.get(TIMESTAMP_ELEMENT);
            Long timestamp = timestampElement == null ? null : timestampElement.getAsLong();
            JsonElement deviceIdElement = requestObject.get(DEVICE_ID_ELEMENT);
            String deviceId = deviceIdElement == null ? null : deviceIdElement.getAsString();

            switch (name) {
                case "REFRESH_INFO":
                    return new RequestRefreshInfo(id, deviceId, timestamp);
                case "GET_DEVICE_PARAMETERS":
                    List<RequestGetDeviceParameters.Parameter> getParameters =
                            context.deserialize(requestObject.get(PARAMETERS_ELEMENT),
                                    new TypeToken<List<RequestGetDeviceParameters.Parameter>>() {
                                    }.getType());
                    return new RequestGetDeviceParameters(id, deviceId, timestamp, getParameters);
                case "SET_DEVICE_PARAMETERS":
                    List<RequestSetDeviceParameters.Parameter> setParameters =
                            context.deserialize(requestObject.get(PARAMETERS_ELEMENT),
                                    new TypeToken<List<RequestSetDeviceParameters.Parameter>>() {
                                    }.getType());
                    return new RequestSetDeviceParameters(id, deviceId, timestamp, setParameters);
                case "UPDATE":
                    List<RequestUpdate.Parameter> updateParameters =
                            context.deserialize(requestObject.get(PARAMETERS_ELEMENT),
                                    new TypeToken<List<RequestUpdate.Parameter>>() {
                                    }.getType());
                    return new RequestUpdate(id, deviceId, timestamp, updateParameters);
                default:
                    return new RequestOperationNotSupported(id, deviceId, timestamp, name);
            }
        }
    }

    private static class SetVariableListElementDeserializer
            implements JsonDeserializer<RequestSetDeviceParameters.VariableListElement> {

        private final DatastreamSetterTypeMapper datastreamsTypeMapper;

        SetVariableListElementDeserializer(DatastreamSetterTypeMapper datastreamsTypeMapper) {
            this.datastreamsTypeMapper = datastreamsTypeMapper;
        }

        private static String getVariableName(JsonObject requestObject) {
            JsonElement nameElement = requestObject.get(VARIABLE_NAME_ELEMENT);
            if (nameElement == null) {
                throw new JsonParseException("The field 'variableName' is not present in a SET_DEVICE_PARAMETERS variable");
            }
            if (!nameElement.isJsonPrimitive()) {
                throw new JsonParseException("The field 'variableName' must be of type String");
            }
            JsonPrimitive nameAsAPrimitive = nameElement.getAsJsonPrimitive();
            if (!nameAsAPrimitive.isString()) {
                throw new JsonParseException("The field 'variableName' must be of type String");
            }
            return nameAsAPrimitive.getAsString();
        }

        @Override
        public RequestSetDeviceParameters.VariableListElement deserialize(JsonElement json, Type typeOfT,
                                                                          JsonDeserializationContext context) {
            JsonObject requestObject = json.getAsJsonObject();
            String variableName = getVariableName(requestObject);
            Type typeOfVariable = datastreamsTypeMapper.getTypeOf(variableName);
            Object variableValue = null;
            if (typeOfVariable != null) {
                JsonElement variableValueElement = requestObject.get(VARIABLE_VALUE_ELEMENT);
                variableValue = context.deserialize(variableValueElement, typeOfVariable);
            }
            return new RequestSetDeviceParameters.VariableListElement(variableName, variableValue);
        }
    }
}
