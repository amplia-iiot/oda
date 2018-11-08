package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.dispatcher.opengate.domain.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JsonWriterImplTest {

    private final JsonWriter jsonWriter = new JsonWriterImpl();

    @Test
    public void genericResponse() {
        JsonElement val1 = new JsonPrimitive("aVariableValue1");
        JsonArray val2 = new JsonArray(2);
        val2.add("hi");
        val2.add(42);
        Output outputGetDeviceParam = new Output(
                "8.0",
                new OutputOperation(
                        new Response(
                                "anOperationId",
                                "aDeviceId",
                                "anOperationName",
                                OperationResultCode.ERROR_IN_PARAM,
                                "aResultDescription",
                                Collections.singletonList(
                                        new Step(
                                                "aStepName",
                                                StepResultCode.ERROR,
                                                "aStepDescription",
                                                1000L,
                                                Arrays.asList(
                                                        new OutputVariable("aVariableName1", val1, "aResultCode1", "aResultDescription1"),
                                                        new OutputVariable("aVariableName2", val2, "aResultCode2", "aResultDescription2")
                                                )
                                        )
                                )
                        )
                )
        );

        String actual = new String(jsonWriter.dumpOutput(outputGetDeviceParam));

        String expected =
                "{" +
                        "\"version\":\"8.0\"," +
                        "\"operation\":{" +
                        "\"response\":{" +
                        "\"id\":\"anOperationId\"," +
                        "\"deviceId\":\"aDeviceId\"," +
                        "\"name\":\"anOperationName\"," +
                        "\"resultCode\":\"ERROR_IN_PARAM\"," +
                        "\"resultDescription\":\"aResultDescription\"," +
                        "\"steps\":[" +
                        "{" +
                        "\"name\":\"aStepName\"," +
                        "\"result\":\"ERROR\"," +
                        "\"description\":\"aStepDescription\"," +
                        "\"timestamp\":1000," +
                        "\"response\":[" +
                        "{" +
                        "\"variableName\":\"aVariableName1\"," +
                        "\"variableValue\":\"aVariableValue1\"," +
                        "\"resultCode\":\"aResultCode1\"," +
                        "\"resultDescription\":\"aResultDescription1\"" +
                        "}," +
                        "{" +
                        "\"variableName\":\"aVariableName2\"," +
                        "\"variableValue\":[" +
                        "\"hi\"," +
                        "42" +
                        "]," +
                        "\"resultCode\":\"aResultCode2\"," +
                        "\"resultDescription\":\"aResultDescription2\"" +
                        "}" +
                        "]" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "}";
        assertEquals(actual, expected);
    }
}
