package es.amplia.oda.comms.iec104;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.apci.Timer;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.DataTransmissionMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import es.amplia.oda.comms.iec104.slave.Iec104MessageChannelHandler;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Iec104MessageChannelHandler.class)
public class Iec104MessageChannelHandlerTest {

	@Mock
	ProtocolOptions mockedOptions;
	@InjectMocks
	Iec104MessageChannelHandler handler;
	@Mock
	ChannelHandlerContext mockedContext;
	@Mock
	ChannelPromise mockedPromise;
	@Mock
	Timer mockedTimer;
	@Mock
	ByteBufAllocator mockedAllocator;
	@Mock
	ByteBuf mockedBuf;

	@Test
	public void testChannelActive() throws Exception {
		int time = 1000;
		when(mockedContext.newPromise()).thenReturn(mockedPromise);
		whenNew(Timer.class).withAnyArguments().thenReturn(mockedTimer);
		when(mockedOptions.getTimeout1()).thenReturn(time);
		when(mockedOptions.getTimeout3()).thenReturn(time);

		handler.channelActive(mockedContext);

		verifyNew(Timer.class).withArguments(eq(mockedContext), eq("T1"), any());
		verifyNew(Timer.class).withArguments(eq(mockedContext), eq("T2"), any());
		verifyNew(Timer.class).withArguments(eq(mockedContext), eq("T3"), any());
		verify(mockedTimer, times(2)).start(eq((long)time));
	}

	@Test
	public void testWrite() throws Exception {
		DataTransmissionMessage msg = DataTransmissionMessage.REQUEST_STOP;
		when(mockedContext.alloc()).thenReturn(mockedAllocator);
		when(mockedAllocator.buffer()).thenReturn(mockedBuf);
		when(mockedBuf.order(any())).thenReturn(mockedBuf);

		handler.write(mockedContext, msg, mockedPromise);

		verify(mockedContext).flush();
	}
}
