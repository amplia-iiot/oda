package es.amplia.oda.datastreams.lora.datastreams;

import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.udp.UdpException;
import es.amplia.oda.core.commons.udp.UdpPacket;
import es.amplia.oda.core.commons.udp.UdpService;
import es.amplia.oda.datastreams.lora.datastructures.LoraDataPacket;
import es.amplia.oda.datastreams.lora.datastructures.LoraStatusPacket;
import es.amplia.oda.datastreams.lora.datastructures.Rxpk;
import es.amplia.oda.datastreams.lora.datastructures.Stat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoraDatastreamsEvent.class)
public class LoraDatastreamsEventTest {

	private static final byte[] LORA_STATUS_BYTE_ARRAY = {
			0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,	// This row should be skipped
			0x7b,0x22,0x73,0x74,0x61,0x74,0x22,0x3a,0x7b,0x22,0x74,0x69,0x6d,0x65,0x22,0x3a,0x22,0x32,0x30,0x32,0x30,
			0x2d,0x31,0x31,0x2d,0x31,0x30,0x20,0x31,0x35,0x3a,0x30,0x32,0x3a,0x35,0x39,0x20,0x47,0x4d,0x54,0x22,0x2c,
			0x22,0x72,0x78,0x6e,0x62,0x22,0x3a,0x30,0x2c,0x22,0x72,0x78,0x6f,0x6b,0x22,0x3a,0x30,0x2c,0x22,0x72,0x78,
			0x66,0x77,0x22,0x3a,0x30,0x2c,0x22,0x61,0x63,0x6b,0x72,0x22,0x3a,0x35,0x30,0x2e,0x30,0x2c,0x22,0x64,0x77,
			0x6e,0x62,0x22,0x3a,0x30,0x2c,0x22,0x74,0x78,0x6e,0x62,0x22,0x3a,0x30,0x2c,0x22,0x70,0x66,0x72,0x6d,0x22,
			0x3a,0x22,0x49,0x4d,0x53,0x54,0x20,0x2b,0x20,0x52,0x70,0x69,0x22,0x2c,0x22,0x6d,0x61,0x69,0x6c,0x22,0x3a,
			0x22,0x65,0x69,0x6e,0x61,0x72,0x40,0x73,0x70,0x6f,0x72,0x64,0x61,0x74,0x61,0x2e,0x6e,0x6f,0x22,0x2c,0x22,
			0x64,0x65,0x73,0x63,0x22,0x3a,0x22,0x53,0x70,0x6f,0x72,0x64,0x61,0x74,0x61,0x20,0x47,0x57,0x2c,0x41,0x73,
			0x6b,0x65,0x72,0x22,0x7d,0x7d
	};
	private static final byte[] LORA_STATUS_BYTE_ARRAY_EXPECTED_TO_DESERIALIZE = {
			0x7b,0x22,0x73,0x74,0x61,0x74,0x22,0x3a,0x7b,0x22,0x74,0x69,0x6d,0x65,0x22,0x3a,0x22,0x32,0x30,0x32,0x30,
			0x2d,0x31,0x31,0x2d,0x31,0x30,0x20,0x31,0x35,0x3a,0x30,0x32,0x3a,0x35,0x39,0x20,0x47,0x4d,0x54,0x22,0x2c,
			0x22,0x72,0x78,0x6e,0x62,0x22,0x3a,0x30,0x2c,0x22,0x72,0x78,0x6f,0x6b,0x22,0x3a,0x30,0x2c,0x22,0x72,0x78,
			0x66,0x77,0x22,0x3a,0x30,0x2c,0x22,0x61,0x63,0x6b,0x72,0x22,0x3a,0x35,0x30,0x2e,0x30,0x2c,0x22,0x64,0x77,
			0x6e,0x62,0x22,0x3a,0x30,0x2c,0x22,0x74,0x78,0x6e,0x62,0x22,0x3a,0x30,0x2c,0x22,0x70,0x66,0x72,0x6d,0x22,
			0x3a,0x22,0x49,0x4d,0x53,0x54,0x20,0x2b,0x20,0x52,0x70,0x69,0x22,0x2c,0x22,0x6d,0x61,0x69,0x6c,0x22,0x3a,
			0x22,0x65,0x69,0x6e,0x61,0x72,0x40,0x73,0x70,0x6f,0x72,0x64,0x61,0x74,0x61,0x2e,0x6e,0x6f,0x22,0x2c,0x22,
			0x64,0x65,0x73,0x63,0x22,0x3a,0x22,0x53,0x70,0x6f,0x72,0x64,0x61,0x74,0x61,0x20,0x47,0x57,0x2c,0x41,0x73,
			0x6b,0x65,0x72,0x22,0x7d,0x7d
	};
	private static final byte[] LORA_DATA_BYTE_ARRAY = {
			0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,	// This row should be skipped
			0x7b,0x22,0x72,0x78,0x70,0x6b,0x22,0x3a,0x5b,0x7b,0x22,0x74,0x6d,0x73,0x74,0x22,0x3a,0x32,0x31,0x35,0x38,
			0x30,0x33,0x37,0x36,0x38,0x34,0x2c,0x22,0x74,0x69,0x6d,0x65,0x22,0x3a,0x22,0x32,0x30,0x32,0x30,0x2d,0x31,
			0x31,0x2d,0x31,0x31,0x54,0x31,0x34,0x3a,0x34,0x38,0x3a,0x34,0x30,0x2e,0x37,0x39,0x37,0x36,0x35,0x39,0x5a,
			0x22,0x2c,0x22,0x63,0x68,0x61,0x6e,0x22,0x3a,0x32,0x2c,0x22,0x72,0x66,0x63,0x68,0x22,0x3a,0x31,0x2c,0x22,
			0x66,0x72,0x65,0x71,0x22,0x3a,0x34,0x33,0x33,0x2e,0x35,0x37,0x35,0x30,0x30,0x30,0x2c,0x22,0x73,0x74,0x61,
			0x74,0x22,0x3a,0x31,0x2c,0x22,0x6d,0x6f,0x64,0x75,0x22,0x3a,0x22,0x4c,0x4f,0x52,0x41,0x22,0x2c,0x22,0x64,
			0x61,0x74,0x72,0x22,0x3a,0x22,0x53,0x46,0x31,0x32,0x42,0x57,0x31,0x32,0x35,0x22,0x2c,0x22,0x63,0x6f,0x64,
			0x72,0x22,0x3a,0x22,0x34,0x2f,0x35,0x22,0x2c,0x22,0x6c,0x73,0x6e,0x72,0x22,0x3a,0x34,0x2e,0x32,0x2c,0x22,
			0x72,0x73,0x73,0x69,0x22,0x3a,0x2d,0x39,0x39,0x2c,0x22,0x73,0x69,0x7a,0x65,0x22,0x3a,0x33,0x37,0x2c,0x22,
			0x64,0x61,0x74,0x61,0x22,0x3a,0x22,0x51,0x4d,0x41,0x32,0x41,0x53,0x59,0x41,0x41,0x41,0x41,0x79,0x53,0x56,
			0x6d,0x43,0x69,0x74,0x69,0x33,0x46,0x4d,0x6d,0x38,0x44,0x35,0x69,0x76,0x69,0x2f,0x48,0x55,0x4f,0x73,0x50,
			0x76,0x4e,0x33,0x56,0x51,0x72,0x6a,0x35,0x46,0x45,0x41,0x38,0x62,0x31,0x67,0x3d,0x3d,0x22,0x7d,0x5d,0x7d
	};
	private static final byte[] LORA_DATA_BYTE_ARRAY_EXPECTED_TO_DESERIALIZE = {
			0x7b,0x22,0x72,0x78,0x70,0x6b,0x22,0x3a,0x5b,0x7b,0x22,0x74,0x6d,0x73,0x74,0x22,0x3a,0x32,0x31,0x35,0x38,
			0x30,0x33,0x37,0x36,0x38,0x34,0x2c,0x22,0x74,0x69,0x6d,0x65,0x22,0x3a,0x22,0x32,0x30,0x32,0x30,0x2d,0x31,
			0x31,0x2d,0x31,0x31,0x54,0x31,0x34,0x3a,0x34,0x38,0x3a,0x34,0x30,0x2e,0x37,0x39,0x37,0x36,0x35,0x39,0x5a,
			0x22,0x2c,0x22,0x63,0x68,0x61,0x6e,0x22,0x3a,0x32,0x2c,0x22,0x72,0x66,0x63,0x68,0x22,0x3a,0x31,0x2c,0x22,
			0x66,0x72,0x65,0x71,0x22,0x3a,0x34,0x33,0x33,0x2e,0x35,0x37,0x35,0x30,0x30,0x30,0x2c,0x22,0x73,0x74,0x61,
			0x74,0x22,0x3a,0x31,0x2c,0x22,0x6d,0x6f,0x64,0x75,0x22,0x3a,0x22,0x4c,0x4f,0x52,0x41,0x22,0x2c,0x22,0x64,
			0x61,0x74,0x72,0x22,0x3a,0x22,0x53,0x46,0x31,0x32,0x42,0x57,0x31,0x32,0x35,0x22,0x2c,0x22,0x63,0x6f,0x64,
			0x72,0x22,0x3a,0x22,0x34,0x2f,0x35,0x22,0x2c,0x22,0x6c,0x73,0x6e,0x72,0x22,0x3a,0x34,0x2e,0x32,0x2c,0x22,
			0x72,0x73,0x73,0x69,0x22,0x3a,0x2d,0x39,0x39,0x2c,0x22,0x73,0x69,0x7a,0x65,0x22,0x3a,0x33,0x37,0x2c,0x22,
			0x64,0x61,0x74,0x61,0x22,0x3a,0x22,0x51,0x4d,0x41,0x32,0x41,0x53,0x59,0x41,0x41,0x41,0x41,0x79,0x53,0x56,
			0x6d,0x43,0x69,0x74,0x69,0x33,0x46,0x4d,0x6d,0x38,0x44,0x35,0x69,0x76,0x69,0x2f,0x48,0x55,0x4f,0x73,0x50,
			0x76,0x4e,0x33,0x56,0x51,0x72,0x6a,0x35,0x46,0x45,0x41,0x38,0x62,0x31,0x67,0x3d,0x3d,0x22,0x7d,0x5d,0x7d
	};

	@Mock
	UdpService mockedService;
	@Mock
	Serializer mockedSerializer;
	@Mock
	EventPublisher mockedPublisher;
	@InjectMocks
	LoraDatastreamsEvent testDatastreamsEvent;

	@Mock
	CompletableFuture<UdpPacket> mockedFuturePacket;
	@Mock
	UdpPacket mockedPacket;
	@Mock
	LoraStatusPacket mockedStatus;
	@Mock
	Stat mockedStat;
	@Mock
	LoraDataPacket mockedData;
	@Mock
	Rxpk mockedRxpk;
	@Mock
	Thread mockedThread;

	@Test
	public void testRegisterToEventSource() throws Exception {
		when(mockedService.isBound()).thenReturn(false);

		testDatastreamsEvent.registerToEventSource();
		TimeUnit.SECONDS.sleep(3);

		Thread thread = Whitebox.getInternalState(testDatastreamsEvent, "readingThread");
		assertNotNull(thread);
		assertTrue(thread.isAlive());
		thread.interrupt();
	}

	@Test
	public void testRegisterToEventSourceWithException() throws Exception {
		doThrow(UdpException.class).when(mockedService).isBound();

		testDatastreamsEvent.registerToEventSource();
		TimeUnit.SECONDS.sleep(3);

		Thread thread = Whitebox.getInternalState(testDatastreamsEvent, "readingThread");
		assertNotNull(thread);
		assertFalse(thread.isAlive());
		assertFalse(thread.isInterrupted());
		thread.interrupt();
	}

	@Test
	public void testRegisterToEventSourceWithInterruptedException() throws Exception {
		when(mockedService.isBound()).thenReturn(false);

		testDatastreamsEvent.registerToEventSource();
		TimeUnit.SECONDS.sleep(3);

		Thread thread = Whitebox.getInternalState(testDatastreamsEvent, "readingThread");
		verify(mockedService, times(0)).receiveMessage();
		assertNotNull(thread);
		assertTrue(thread.isAlive());
		assertFalse(thread.isInterrupted());
		thread.interrupt();
	}

	@Test
	public void testRegisterToEventSourceWithExecutionException() throws Exception {
		doThrow(ExecutionException.class).when(mockedService).isBound();

		testDatastreamsEvent.registerToEventSource();
		TimeUnit.SECONDS.sleep(3);

		Thread thread = Whitebox.getInternalState(testDatastreamsEvent, "readingThread");
		verify(mockedService, times(0)).receiveMessage();
		assertNotNull(thread);
		assertTrue(thread.isAlive());
		assertFalse(thread.isInterrupted());
		thread.interrupt();
	}

	@Test
	public void testResgisterToEventSourceAndReadALoraStatus() throws InterruptedException, ExecutionException, IOException {
		Whitebox.setInternalState(testDatastreamsEvent, "deviceId", "gatewayForUnitTests");
		when(mockedService.isBound()).thenReturn(true);
		when(mockedService.receiveMessage()).thenReturn(mockedFuturePacket);
		when(mockedFuturePacket.get()).thenReturn(mockedPacket);
		when(mockedPacket.getDataAsBytes()).thenReturn(LORA_STATUS_BYTE_ARRAY);
		when(mockedSerializer.deserialize(eq(LORA_STATUS_BYTE_ARRAY_EXPECTED_TO_DESERIALIZE), eq(LoraStatusPacket.class)))
				.thenReturn(mockedStatus);
		when(mockedSerializer.deserialize(eq(LORA_STATUS_BYTE_ARRAY_EXPECTED_TO_DESERIALIZE), eq(LoraDataPacket.class)))
				.thenReturn(null);
		when(mockedStatus.getStat()).thenReturn(mockedStat);
		doNothing().when(mockedPublisher).publishEvent(any(), any(), any(), any(), any());

		testDatastreamsEvent.registerToEventSource();
		TimeUnit.SECONDS.sleep(3);

		verify(mockedSerializer, atLeastOnce()).deserialize(eq(LORA_STATUS_BYTE_ARRAY_EXPECTED_TO_DESERIALIZE), eq(LoraStatusPacket.class));
		verify(mockedSerializer, atLeastOnce()).deserialize(eq(LORA_STATUS_BYTE_ARRAY_EXPECTED_TO_DESERIALIZE), eq(LoraDataPacket.class));
		verify(mockedPublisher, atLeastOnce()).publishEvent(eq("gatewayForUnitTests"), eq("lora"), (String[]) isNull(), anyLong(), eq(mockedStatus));
		Thread thread = Whitebox.getInternalState(testDatastreamsEvent, "readingThread");
		assertNotNull(thread);
		assertTrue(thread.isAlive());
		thread.interrupt();
	}

	@Test
	public void testResgisterToEventSourceAndReadALoraData() throws InterruptedException, ExecutionException, IOException {
		Whitebox.setInternalState(testDatastreamsEvent, "deviceId", "gatewayForUnitTests");
		when(mockedService.isBound()).thenReturn(true);
		when(mockedService.receiveMessage()).thenReturn(mockedFuturePacket);
		when(mockedFuturePacket.get()).thenReturn(mockedPacket);
		when(mockedPacket.getDataAsBytes()).thenReturn(LORA_DATA_BYTE_ARRAY);
		when(mockedSerializer.deserialize(eq(LORA_DATA_BYTE_ARRAY_EXPECTED_TO_DESERIALIZE), eq(LoraStatusPacket.class)))
				.thenReturn(null);
		when(mockedSerializer.deserialize(eq(LORA_DATA_BYTE_ARRAY_EXPECTED_TO_DESERIALIZE), eq(LoraDataPacket.class)))
				.thenReturn(mockedData);
		when(mockedData.getRxpk()).thenReturn(Collections.singletonList(mockedRxpk));
		doNothing().when(mockedPublisher).publishEvent(any(), any(), any(), any(), any());

		testDatastreamsEvent.registerToEventSource();
		TimeUnit.SECONDS.sleep(3);

		verify(mockedSerializer, atLeastOnce()).deserialize(eq(LORA_DATA_BYTE_ARRAY_EXPECTED_TO_DESERIALIZE), eq(LoraStatusPacket.class));
		verify(mockedSerializer, atLeastOnce()).deserialize(eq(LORA_DATA_BYTE_ARRAY_EXPECTED_TO_DESERIALIZE), eq(LoraDataPacket.class));
		verify(mockedPublisher, atLeastOnce()).publishEvent(eq("gatewayForUnitTests"), eq("lora"), (String[]) isNull(), anyLong(), eq(mockedData));
		Thread thread = Whitebox.getInternalState(testDatastreamsEvent, "readingThread");
		assertNotNull(thread);
		assertTrue(thread.isAlive());
		thread.interrupt();
	}

	@Test
	public void testUnregisterFromEventSource() {
		Whitebox.setInternalState(testDatastreamsEvent, "readingThread", mockedThread);

		testDatastreamsEvent.unregisterFromEventSource();

		verify(mockedThread).interrupt();
	}
}