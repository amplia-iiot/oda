package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DatastreamSetterTypeMapper;
import es.amplia.oda.dispatcher.opengate.domain.*;

import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static es.amplia.oda.operation.api.OperationUpdate.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonParserImplTest {
    private final String SET_HEADER =
            "{" +
                    "\"operation\": {" +
                    "\"request\": {" +
                    "\"id\": \"d9227691-c934-430e-a3d9-fffffffff\"," +
                    "\"timestamp\": 1519658054169," +
                    "\"name\": \"SET_DEVICE_PARAMETERS\"," +
                    "\"parameters\": [" +
                    "{" +
                    "\"name\": \"variableList\"";
    private final String SET_FOOTER =
            "}" + //First parameter
                    "]" + //Array of parameters
                    "}" + //request
                    "}" + //operation
                    "}";
    private JsonParserImpl jsonParser;
    @Mock
    private DatastreamSetterTypeMapper datastreamsTypeMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jsonParser = new JsonParserImpl(datastreamsTypeMapper);
    }

    @Test(expected = IllegalArgumentException.class)
    public void forNull_ThrowsException() {
        jsonParser.parseInput(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void withAnInvalidJson_ThrowsException() {
        jsonParser.parseInput("kkkkk".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void anInvalidUTF8Sequence_ThrowsException() {
        byte[] b = {
                (byte) 0x07b, // {
                (byte) 0x022, // "
                (byte) 0x061, // a
                (byte) 0x022, // "
                (byte) 0x03a, // :
                (byte) 0x022, // "
                (byte) 0x0c3, //C3 B1 is the sequence for 'ñ'.
                //(byte)0x0b1, //But C3 C3 is an invalid sequence
                (byte) 0x0c3, //
                (byte) 0x0b1, // ñ
                (byte) 0x022, // "
                (byte) 0x07d, // }
        };
        jsonParser.parseInput(b);
    }

    @Test()
    public void aValidJsonWithUnknownStruct_ReturnsAnEmptyStruct() {
        Input actual = jsonParser.parseInput("{\"a\":\"b\"}".getBytes());
        assertNotNull(actual);
    }

    @Test
    public void aValidOperationWithoutRequest_ReturnsAnEmptyOperation() {
        Input actual = jsonParser.parseInput("{\"operation\": {} }".getBytes());

        Input expected = new Input(new InputOperation(null));
        assertEquals(actual, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void aRequestWithoutNameFieldThrows() {
        jsonParser.parseInput("{\"operation\": {\"request\": {} } }".getBytes());
    }

    @Test
    public void aRequestWithValidNameDoesNotThrowsException() {
        String json = "{\"operation\": {\"request\": {\"name\": \"REFRESH_INFO\"}}}";
        Request expected = new RequestRefreshInfo(null, null, null);

        Input actual = jsonParser.parseInput(json.getBytes());

        assertEquals(actual.getOperation().getRequest(), expected);
    }

    @Test
    public void aRequestWithInvalidNameThrows() {
        String json =
                "{" +
                        "\"operation\": {" +
                        "\"request\": {" +
                        "\"id\": \"d9227691-c934-430e-a3d9-fffffffff\"," +
                        "\"deviceId\": \"aDeviceId\"," +
                        "\"timestamp\": 1519658054169," +
                        "\"name\": \"UNKNOWN_OPERATION\"" +
                        "}" +
                        "}" +
                        "}";
        Request expected = new RequestOperationNotSupported(
                "d9227691-c934-430e-a3d9-fffffffff",
                "aDeviceId",
                1519658054169L,
                "UNKNOWN_OPERATION"
        );

        Input actual = jsonParser.parseInput(json.getBytes());

        assertEquals(actual.getOperation().getRequest(), expected);
    }

    @Test
    public void completeRefreshInfoIsParsed() {
        String json =
                "{" +
                        "\"operation\": {" +
                        "\"request\": {" +
                        "\"id\": \"d9227691-c934-430e-a3d9-fffffffff\"," +
                        "\"deviceId\": \"aDeviceId\"," +
                        "\"timestamp\": 1519658054169," +
                        "\"name\": \"REFRESH_INFO\"" +
                        "}" +
                        "}" +
                        "}";
        Request expected = new RequestRefreshInfo(
                "d9227691-c934-430e-a3d9-fffffffff", //id
                "aDeviceId",
                1519658054169L
        );

        Input actual = jsonParser.parseInput(json.getBytes());

        assertEquals(actual.getOperation().getRequest(), expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDevicesParametersWithTwoParametersThrows() {
        String json =
                "{" +
                        "\"operation\": {" +
                        "\"request\": {" +
                        "\"name\": \"GET_DEVICE_PARAMETERS\"," +
                        "\"parameters\": [" +
                        "{" +
                        "\"name\": \"variableList\"," +
                        "\"value\": \"b\"" +
                        "}," +
                        "{" +
                        "\"name\": \"variableList\"," +
                        "\"value\": \"b\"" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "}";

        jsonParser.parseInput(json.getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDevicesParametersWithAParameterWithoutNameThrows() {
        String json =
                "{" +
                        "\"operation\": {" +
                        "\"request\": {" +
                        "\"name\": \"GET_DEVICE_PARAMETERS\"," +
                        "\"parameters\": [" +
                        "{" +
                        "\"value\": \"b\"" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "}";

        jsonParser.parseInput(json.getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDevicesParametersWithOneInvalidParametersThrows() {
        String json =
                "{" +
                        "\"operation\": {" +
                        "\"request\": {" +
                        "\"name\": \"GET_DEVICE_PARAMETERS\"," +
                        "\"parameters\": [" +
                        "{" +
                        "\"name\": \"badName\"," +
                        "\"value\": \"b\"" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "}";

        jsonParser.parseInput(json.getBytes());
    }

    @Test()
    public void getDevicesParametersWithAParameterWithoutValue_Works() {
        String json =
                "{" +
                        "\"operation\": {" +
                        "\"request\": {" +
                        "\"name\": \"GET_DEVICE_PARAMETERS\"," +
                        "\"parameters\": [" +
                        "{" +
                        "\"name\": \"kkk\"" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "}";

        Input actual = jsonParser.parseInput(json.getBytes());

        Input expected = new Input(
                new InputOperation(
                        new RequestGetDeviceParameters(
                                null,
                                null,
                                null,
                                Collections.singletonList(new RequestGetDeviceParameters.Parameter("kkk", null))
                        )
                )
        );
        assertEquals(actual, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDevicesParametersWithAParameterWithInvalidTypeForValue_Throws() {
        String json =
                "{" +
                        "\"operation\": {" +
                        "\"request\": {" +
                        "\"name\": \"GET_DEVICE_PARAMETERS\"," +
                        "\"parameters\": [" +
                        "{" +
                        "\"name\": \"variableList\", " +
                        "\"value\": 42" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "}";
        jsonParser.parseInput(json.getBytes());
    }

    @Test
    public void getDevicesParametersWithAParameterWithAValueWithoutArray_Works() {
        String json =
                "{" +
                        "\"operation\": {" +
                        "\"request\": {" +
                        "\"name\": \"GET_DEVICE_PARAMETERS\"," +
                        "\"parameters\": [" +
                        "{" +
                        "\"name\": \"variableList\"," +
                        "\"value\": {}" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "}";
        Input actual = jsonParser.parseInput(json.getBytes());
        Input expected = new Input(
                new InputOperation(
                        new RequestGetDeviceParameters(
                                null,
                                null,
                                null,
                                Collections.singletonList(
                                        new RequestGetDeviceParameters.Parameter(
                                                "variableList",
                                                new RequestGetDeviceParameters.ValueArray(null)
                                        )
                                )
                        )
                )
        );
        assertEquals(actual, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDevicesParametersWithAParameterWithAValueWithAnArrayOfInvalidType_Throws() {
        String json =
                "{" +
                        "\"operation\": {" +
                        "\"request\": {" +
                        "\"name\": \"GET_DEVICE_PARAMETERS\"," +
                        "\"parameters\": [" +
                        "{" +
                        "\"name\": \"variableList\"," +
                        "\"value\": {" +
                        "\"array\": 42" +
                        "}" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "}";
        jsonParser.parseInput(json.getBytes());
    }

    @Test
    public void completeGetDevice_Works() {
        String json =
                "{" +
                        "\"operation\": {" +
                        "\"request\": {" +
                        "\"id\": \"d9227691-c934-430e-a3d9-fffffffff\"," +
                        "\"timestamp\": 1519658054169," +
                        "\"name\": \"GET_DEVICE_PARAMETERS\"," +
                        "\"parameters\": [" +
                        "{" +
                        "\"name\": \"ttt\"," +
                        "\"value\": {" +
                        "\"array\": [" +
                        "{" +
                        "\"variableName\": \"provision.device.serialNumber\"" +
                        "}," +
                        "{" +
                        "\"variableName\": \"id2\"" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "}";
        Input actual = jsonParser.parseInput(json.getBytes());
        Input expected = new Input(
                new InputOperation(
                        new RequestGetDeviceParameters(
                                "d9227691-c934-430e-a3d9-fffffffff",
                                null,
                                1519658054169L,
                                Collections.singletonList(
                                        new RequestGetDeviceParameters.Parameter(
                                                "ttt",
                                                new RequestGetDeviceParameters.ValueArray(Arrays.asList(
                                                        new RequestGetDeviceParameters.VariableListElement("provision.device.serialNumber"),
                                                        new RequestGetDeviceParameters.VariableListElement("id2")
                                                ))
                                        )
                                )
                        )
                )
        );
        assertEquals(actual, expected);
    }

    @Test
    public void completeUpdate_Works() {
        String json =
                "{" +
                        "\"operation\": {" +
                        "\"request\": {" +
                        "\"id\": \"d9227691-c934-430e-a3d9-fffffffff\"," +
                        "\"timestamp\": 1519658054169," +
                        "\"name\": \"UPDATE\"," +
                        "\"parameters\": [" +
                        "{" +
                        "\"name\": \"bundleName\"," +
                        "\"value\": {" +
                        "\"string\": \"updatetest_001\"" +
                        "}" +
                        "}," +
                        "{" +
                        "\"name\": \"bundleVersion\"," +
                        "\"value\": {" +
                        "\"string\": \"1\"" +
                        "}" +
                        "}," +
                        "{" +
                        "\"name\": \"deploymentElements\"," +
                        "\"value\": {" +
                        "\"array\": [" +
                        "{" +
                        "\"name\": \"org.apache.felix.eventadmin\"," +
                        "\"version\": \"1.4.8\"," +
                        "\"type\": \"SOFTWARE\"," +
                        "\"downloadUrl\": \"anUrl\"," +
                        "\"path\": \"/etc\"," +
                        "\"order\": 1," +
                        "\"operation\": \"INSTALL\"," +
                        "\"option\": \"MANDATORY\"," +
                        "\"validators\": []," +
                        "\"size\": 95550" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "]" +
                        "}" +
                        "}" +
                        "}";
        Input actual = jsonParser.parseInput(json.getBytes());

        RequestUpdate.VariableListElement deployElement1 = new RequestUpdate.VariableListElement("org.apache.felix.eventadmin", "1.4.8", DeploymentElementType.SOFTWARE, "anUrl", "/etc", DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 1L);
        RequestUpdate.Parameter bundleName = new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType("updatetest_001", null));
        RequestUpdate.Parameter bundleVersion = new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType("1", null));
        RequestUpdate.Parameter deploymentElements = new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType(null, Collections.singletonList(deployElement1)));
        Input expected = new Input(
                new InputOperation(
                        new RequestUpdate(
                                "d9227691-c934-430e-a3d9-fffffffff",
                                null,
                                1519658054169L,
                                Arrays.asList(bundleName, bundleVersion, deploymentElements)
                        )
                )
        );
        assertEquals(actual, expected);
    }

    @Test
    public void setDeviceParameterHeaderIsParsed() {
        String json = SET_HEADER + SET_FOOTER;
        Input actual = jsonParser.parseInput(json.getBytes());

        Input expected = new Input(
                new InputOperation(
                        new RequestSetDeviceParameters(
                                "d9227691-c934-430e-a3d9-fffffffff",
                                null,
                                1519658054169L,
                                Collections.singletonList(
                                        new RequestSetDeviceParameters.Parameter(
                                                "variableList",
                                                null
                                        )
                                )
                        )
                )
        );
        assertEquals(actual, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDeviceParametersNeedsFieldvariableNameForEveryElementInVariableList() {
        String json = SET_HEADER + "," +
                "\"value\": {" +
                "\"array\": [" +
                "{" +
                "\"VariableNameIsMissing\": \"id1\"" +
                "}" +
                "]" +
                "}" +
                SET_FOOTER;
        jsonParser.parseInput(json.getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void inASetDeviceParameters_VariableNameCannotBeAComplexJsonObject() {
        String json = SET_HEADER + "," +
                "\"value\": {" +
                "\"array\": [" +
                "{" +
                "\"variableName\": {\"a\":\"b\"}" +
                "}" +
                "]" +
                "}" +
                SET_FOOTER;
        jsonParser.parseInput(json.getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void inASetDeviceParameters_VariableNameMustBeAString() {
        String json = SET_HEADER + "," +
                "\"value\": {" +
                "\"array\": [" +
                "{" +
                "\"variableName\": 42" +
                "}" +
                "]" +
                "}" +
                SET_FOOTER;
        jsonParser.parseInput(json.getBytes());
    }

    @Test
    public void inASetDeviceParameters_VariableNameIsSearchedInDatastreamsTypeMapper() {
        String json = SET_HEADER + "," +
                "\"value\": {" +
                "\"array\": [" +
                "{" +
                "\"variableName\": \"id1\"" +
                "}" +
                "]" +
                "}" +
                SET_FOOTER;

        jsonParser.parseInput(json.getBytes());

        verify(datastreamsTypeMapper).getTypeOf("id1");
    }

    @Test
    public void inASetDeviceParameters_ANotRegisteredIdentifierLeavesTheValueToNull() {
        String json = SET_HEADER + "," +
                "\"value\": {" +
                "\"array\": [" +
                "{" +
                "\"variableName\": \"id1\"," +
                "\"variableValue\": {\"a\":\"b\",\"c\": 42}" +
                "}" +
                "]" +
                "}" +
                SET_FOOTER;
        RequestSetDeviceParameters actual = (RequestSetDeviceParameters) jsonParser.parseInput(json.getBytes()).getOperation().getRequest();


        RequestSetDeviceParameters.VariableListElement variableListElement = actual.getParameters().get(0).getValue().getArray().get(0);
        assertNotNull(variableListElement);
        assertEquals("id1", variableListElement.getVariableName());
        assertNull(variableListElement.getVariableValue());
    }

    @Test
    public void inASetDeviceParameters_ARegisteredIdentifierCreatesAnObjectOfTheCorrectType() {
        String variableValue = "hi";

        when(datastreamsTypeMapper.getTypeOf("id1")).thenReturn(new TypeToken<AClass>() {
        }.getType());

        String json = SET_HEADER + "," +
                "\"value\": {" +
                "\"array\": [" +
                "{" +
                "\"variableName\": \"id1\"," +
                "\"variableValue\": {\"a\":\"" + variableValue + "\",\"c\": 42}" +
                "}" +
                "]" +
                "}" +
                SET_FOOTER;
        RequestSetDeviceParameters actual = (RequestSetDeviceParameters) jsonParser.parseInput(json.getBytes()).getOperation().getRequest();


        RequestSetDeviceParameters.VariableListElement variableListElement = actual.getParameters().get(0).getValue().getArray().get(0);
        AClass expected = new AClass(variableValue, 42);
        assertEquals(variableListElement.getVariableValue(), expected);
    }

    @Test
    public void inASetDeviceParameters_ARegisteredListOfIntegersWorks() {
        when(datastreamsTypeMapper.getTypeOf("id1")).thenReturn(new TypeToken<List<Integer>>() {
        }.getType());

        String json = SET_HEADER + "," +
                "\"value\": {" +
                "\"array\": [" +
                "{" +
                "\"variableName\": \"id1\"," +
                "\"variableValue\": [12,34]" +
                "}" +
                "]" +
                "}" +
                SET_FOOTER;
        RequestSetDeviceParameters actual = (RequestSetDeviceParameters) jsonParser.parseInput(json.getBytes()).getOperation().getRequest();


        RequestSetDeviceParameters.VariableListElement variableListElement = actual.getParameters().get(0).getValue().getArray().get(0);
        List<Integer> expected = Arrays.asList(12, 34);
        assertEquals(variableListElement.getVariableValue(), expected);
    }

    @Test
    public void inASetDeviceParameters_ARegisteredStringWorks() {
        String variableValue = "hi";

        when(datastreamsTypeMapper.getTypeOf("id1")).thenReturn(new TypeToken<String>() {
        }.getType());

        String json = SET_HEADER + "," +
                "\"value\": {" +
                "\"array\": [" +
                "{" +
                "\"variableName\": \"id1\"," +
                "\"variableValue\": \"" + variableValue + "\"" +
                "}" +
                "]" +
                "}" +
                SET_FOOTER;
        RequestSetDeviceParameters actual = (RequestSetDeviceParameters) jsonParser.parseInput(json.getBytes()).getOperation().getRequest();


        RequestSetDeviceParameters.VariableListElement variableListElement = actual.getParameters().get(0).getValue().getArray().get(0);
        assertEquals(variableListElement.getVariableValue(), variableValue);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class AClass {
        String a;
        int c;
    }

}
