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
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import es.amplia.oda.comms.iec104.slave.Iec104MessageChannelHandler;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
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
	ByteBufAllocator mockedAllocator;
	@Mock
	ByteBuf mockedBuf;

	@Test
	public void testChannelActive() throws Exception {
		int time = 1000;
		List<List<?>> timerArgs = new ArrayList<>();
		try (MockedConstruction<Timer> timerCons = mockConstruction(Timer.class,
				(mock, mctx) -> timerArgs.add(new ArrayList<>(mctx.arguments())))) {
			when(mockedContext.newPromise()).thenReturn(mockedPromise);
			when(mockedOptions.getTimeout1()).thenReturn(time);
			when(mockedOptions.getTimeout3()).thenReturn(time);

			handler.channelActive(mockedContext);

			assertEquals(3, timerCons.constructed().size());
			assertEquals(mockedContext, timerArgs.get(0).get(0));
			assertEquals("T1", timerArgs.get(0).get(1));
			assertEquals(mockedContext, timerArgs.get(1).get(0));
			assertEquals("T2", timerArgs.get(1).get(1));
			assertEquals(mockedContext, timerArgs.get(2).get(0));
			assertEquals("T3", timerArgs.get(2).get(1));
			long timerStarts = timerCons.constructed().stream()
					.filter(timer -> mockingDetails(timer).getInvocations().stream()
							.anyMatch(invocation -> "start".equals(invocation.getMethod().getName())
									&& Long.valueOf(time).equals(invocation.getArgument(0))))
					.count();
			assertEquals(2, timerStarts);
		}
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
