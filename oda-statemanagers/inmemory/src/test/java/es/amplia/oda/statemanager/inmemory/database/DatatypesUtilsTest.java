package es.amplia.oda.statemanager.inmemory.database;

import es.amplia.oda.core.commons.interfaces.Serializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static es.amplia.oda.statemanager.inmemory.database.DatatypesUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatatypesUtils.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class DatatypesUtilsTest {
	private static final int POSITION_TO_TEST = 1;

	@Mock
	Serializer mockedSerializer;
	@InjectMocks
	DatatypesUtils testUtils;

	@Mock
	PreparedStatement mockedStatement;

	@Test
	public void insertNullTest() throws SQLException {
		testUtils.insertParameter(mockedStatement, POSITION_TO_TEST, null);
		verify(mockedStatement).setNull(POSITION_TO_TEST, 2);
	}

	@Test
	public void insertShortTest() throws SQLException {
		short testingMeasureShort = 165;
		testUtils.insertParameter(mockedStatement, POSITION_TO_TEST, testingMeasureShort);
		verify(mockedStatement).setShort(POSITION_TO_TEST, testingMeasureShort);
	}

	@Test
	public void insertIntTest() throws SQLException {
		int testingMeasureInt = 115;
		testUtils.insertParameter(mockedStatement, POSITION_TO_TEST, testingMeasureInt);
		verify(mockedStatement).setInt(POSITION_TO_TEST, testingMeasureInt);
	}

	@Test
	public void insertLongTest() throws SQLException {
		long testingMeasureLong = 123456789;
		testUtils.insertParameter(mockedStatement, POSITION_TO_TEST, testingMeasureLong);
		verify(mockedStatement).setLong(POSITION_TO_TEST, testingMeasureLong);
	}

	@Test
	public void insertFloatTest() throws SQLException {
		float testingMeasureFloat = 23.1f;
		testUtils.insertParameter(mockedStatement, POSITION_TO_TEST, testingMeasureFloat);
		verify(mockedStatement).setFloat(POSITION_TO_TEST, testingMeasureFloat);
	}

	@Test
	public void insertDoubleTest() throws SQLException {
		double testingMeasureDouble = 65.0;
		testUtils.insertParameter(mockedStatement, POSITION_TO_TEST, testingMeasureDouble);
		verify(mockedStatement).setDouble(POSITION_TO_TEST, testingMeasureDouble);
	}

	@Test
	public void insertBooleanTest() throws SQLException {
		testUtils.insertParameter(mockedStatement, POSITION_TO_TEST, true);
		verify(mockedStatement).setBoolean(POSITION_TO_TEST, true);
	}

	@Test
	public void insertByteTest() throws SQLException {
		byte testingMeasureByte = 0x42;
		testUtils.insertParameter(mockedStatement, POSITION_TO_TEST, testingMeasureByte);
		verify(mockedStatement).setByte(POSITION_TO_TEST, testingMeasureByte);
	}

	@Test
	public void insertStringTest() throws SQLException {
		String testingMeasureString = "hola, soy un dato";
		testUtils.insertParameter(mockedStatement, POSITION_TO_TEST, testingMeasureString);
		verify(mockedStatement).setString(POSITION_TO_TEST, testingMeasureString);
	}

	@Test
	public void insertAnotherTypeTest() throws SQLException {
		char testingMeasureChar = 'c';
		testUtils.insertParameter(mockedStatement, POSITION_TO_TEST, testingMeasureChar);
		verifyZeroInteractions(mockedStatement);
	}

	@Test
	public void getNameOfCharArrayType() {
		char[] charArray = {'c','a','r','a','r','r','a','y',',','b','e','e','p'};
		String name = testUtils.getClassNameOf(charArray);
		assertEquals(CHAR_ARRAY_PRIMITIVE_TYPE_NAME, name);
	}

	@Test
	public void getNameOfByteArrayType() {
		byte[] byteArray = {0x01, 0x02, 0x03, 0x05, 0x07, 0x13};
		String name = testUtils.getClassNameOf(byteArray);
		assertEquals(BYTE_ARRAY_PRIMITIVE_TYPE_NAME, name);
	}

	@Test
	public void getNameOfShortType() {
		short shortData = 42;
		String name = testUtils.getClassNameOf(shortData);
		assertEquals(SHORT_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfShortObjectType() {
		Short shortData = 42;
		String name = testUtils.getClassNameOf(shortData);
		assertEquals(SHORT_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfIntegerType() {
		int integer = 42;
		String name = testUtils.getClassNameOf(integer);
		assertEquals(INT_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfIntegerObjectType() {
		Integer integer = 42;
		String name = testUtils.getClassNameOf(integer);
		assertEquals(INT_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfLongType() {
		long longData = 42;
		String name = testUtils.getClassNameOf(longData);
		assertEquals(LONG_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfLongObjectType() {
		Long longData = 42L;
		String name = testUtils.getClassNameOf(longData);
		assertEquals(LONG_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfFloatType() {
		float floatData = 42.0f;
		String name = testUtils.getClassNameOf(floatData);
		assertEquals(FLOAT_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfFloatObjectType() {
		Float floatData = 42.0f;
		String name = testUtils.getClassNameOf(floatData);
		assertEquals(FLOAT_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfDoubleType() {
		double doubleData = 42.0;
		String name = testUtils.getClassNameOf(doubleData);
		assertEquals(DOUBLE_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfDoubleObjectType() {
		Double doubleData = 42.0;
		String name = testUtils.getClassNameOf(doubleData);
		assertEquals(DOUBLE_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfBooleanType() {
		String name = testUtils.getClassNameOf(true);
		assertEquals(BOOLEAN_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfBooleanObjectType() {
		String name = testUtils.getClassNameOf(true);
		assertEquals(BOOLEAN_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfCharType() {
		char character = 'f';
		String name = testUtils.getClassNameOf(character);
		assertEquals(CHAR_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfCharObjectType() {
		Character character = 'f';
		String name = testUtils.getClassNameOf(character);
		assertEquals(CHAR_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfByteType() {
		byte byteData = 0x42;
		String name = testUtils.getClassNameOf(byteData);
		assertEquals(BYTE_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfByteObjectType() {
		Byte byteData = 0x42;
		String name = testUtils.getClassNameOf(byteData);
		assertEquals(BYTE_OBJECT_TYPE_NAME, name);
	}

	@Test
	public void getNameOfStringType() {
		String string = "a string";
		String name = testUtils.getClassNameOf(string);
		assertEquals(STRING_TYPE_NAME, name);
	}

	@Test
	public void getNameOfArrayType() {
		ArrayList<Object> list = new ArrayList<>();
		list.add("si");
		list.add("tan");
		list.add("listo");
		list.add("era...");
		String name = testUtils.getClassNameOf(list);
		assertEquals(ARRAY_TYPE_NAME, name);
	}

	@Test
	public void getNameOfMapType() {
		HashMap<Object, Object> map = new HashMap<>();
		map.put(1, 'a');
		String name = testUtils.getClassNameOf(map);
		assertNull(name);
	}

	@Test
	public void parseShortData() throws IOException {
		Short value = 1;
		when(mockedSerializer.deserialize(any(), eq(Short.class))).thenReturn(value);
		assertEquals(value, testUtils.parseStoredData("", SHORT_OBJECT_TYPE_NAME));
	}

	@Test
	public void parseIntegerData() throws IOException {
		Integer value = 1;
		when(mockedSerializer.deserialize(any(), eq(Integer.class))).thenReturn(value);
		assertEquals(value, testUtils.parseStoredData("", INT_OBJECT_TYPE_NAME));
	}

	@Test
	public void parseLongData() throws IOException {
		Long value = 1L;
		when(mockedSerializer.deserialize(any(), eq(Long.class))).thenReturn(value);
		assertEquals(value, testUtils.parseStoredData("", LONG_OBJECT_TYPE_NAME));
	}

	@Test
	public void parseFloatData() throws IOException {
		Float value = 1f;
		when(mockedSerializer.deserialize(any(), eq(Float.class))).thenReturn(value);
		assertEquals(value, testUtils.parseStoredData("", FLOAT_OBJECT_TYPE_NAME));
	}

	@Test
	public void parseDoubleData() throws IOException {
		Double value = 1.0;
		when(mockedSerializer.deserialize(any(), eq(Double.class))).thenReturn(value);
		assertEquals(value, testUtils.parseStoredData("", DOUBLE_OBJECT_TYPE_NAME));
	}

	@Test
	public void parseBooleanData() throws IOException {
		when(mockedSerializer.deserialize(any(), eq(Boolean.class))).thenReturn(true);
		assertEquals(true, testUtils.parseStoredData("", BOOLEAN_OBJECT_TYPE_NAME));
	}

	@Test
	public void parseCharData() throws IOException {
		Character value = 'c';
		when(mockedSerializer.deserialize(any(), eq(Character.class))).thenReturn(value);
		assertEquals(value, testUtils.parseStoredData("", CHAR_OBJECT_TYPE_NAME));
	}

	@Test
	public void parseByteData() throws IOException {
		Byte value = 0x02;
		when(mockedSerializer.deserialize(any(), eq(Byte.class))).thenReturn(value);
		assertEquals(value, testUtils.parseStoredData("", BYTE_OBJECT_TYPE_NAME));
	}

	@Test
	public void parseCharArrayData() throws IOException {
		char[] value = {'c','a'};
		when(mockedSerializer.deserialize(any(), eq(char[].class))).thenReturn(value);
		assertEquals(value, testUtils.parseStoredData("", CHAR_ARRAY_PRIMITIVE_TYPE_NAME));
	}

	@Test
	public void parseByteArrayData() throws IOException {
		byte[] value = {0x02};
		when(mockedSerializer.deserialize(any(), eq(byte[].class))).thenReturn(value);
		assertEquals(value, testUtils.parseStoredData("", BYTE_ARRAY_PRIMITIVE_TYPE_NAME));
	}

	@Test
	public void parseStringData() throws IOException {
		String value = "value";
		when(mockedSerializer.deserialize(any(), eq(String.class))).thenReturn(value);
		assertEquals(value, testUtils.parseStoredData("", STRING_TYPE_NAME));
	}

	@Test
	public void parseArrayData() throws IOException {
		ArrayList<String> value = new ArrayList<>();
		value.add("element");
		when(mockedSerializer.deserialize(any(), eq(ArrayList.class))).thenReturn(value);
		assertEquals(value, testUtils.parseStoredData("", ARRAY_TYPE_NAME));
	}

	@Test
	public void parseAnotherData() throws IOException {
		assertNull(testUtils.parseStoredData("", "Map"));
	}
}
