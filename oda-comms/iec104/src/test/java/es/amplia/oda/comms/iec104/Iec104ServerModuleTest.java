package es.amplia.oda.comms.iec104;

import es.amplia.oda.comms.iec104.codecs.*;
import es.amplia.oda.comms.iec104.slave.*;
import es.amplia.oda.comms.iec104.types.BitStringCommand;
import es.amplia.oda.comms.iec104.types.BitStringPointInformationSequence;
import es.amplia.oda.comms.iec104.types.BitStringPointInformationSingle;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.apci.MessageChannel;
import org.eclipse.neoscada.protocol.iec60870.asdu.MessageManager;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.*;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.neoscada.protocol.iec60870.server.Server;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class Iec104ServerModuleTest {

	@Mock
	private Iec104Cache mockedCache;
	@Mock
	private ProtocolOptions mockedOptions;
	@Mock
	private ScadaDispatcherProxy mockedDispatcher;

	private Iec104ServerModule module;
	@Mock
	private Server mockedServer;
	@Mock
	private MessageManager mockedManager;
	@Mock
	private SocketChannel mockedSocketChannel;
	@Mock
	private MessageChannel mockedMessageChannel;
	@Mock
	private ChannelPipeline mockedPipeline;

	@Before
	public void prepareForTest() {
		module = new Iec104ServerModule(mockedCache, mockedOptions, mockedDispatcher, 0);
	}

	@Test
	public void testInitializeServer() {
		module.initializeServer(mockedServer, mockedManager);

		verify(mockedManager).registerCodec(eq(SinglePointInformationSingle.class.getAnnotation(ASDU.class).id()),
				eq(SinglePointInformationSingle.class.getAnnotation(ASDU.class).informationStructure()),
				any(SinglePointInformationSingleCodec.class));
		verify(mockedManager).registerCodec(eq(SinglePointInformationSequence.class.getAnnotation(ASDU.class).id()),
				eq(SinglePointInformationSequence.class.getAnnotation(ASDU.class).informationStructure()),
				any(SinglePointInformationSequenceCodec.class));
		verify(mockedManager).registerCodec(eq(BitStringPointInformationSingle.class.getAnnotation(ASDU.class).id()),
				eq(BitStringPointInformationSingle.class.getAnnotation(ASDU.class).informationStructure()),
				any(BitStringPointSingleCodec.class));
		verify(mockedManager).registerCodec(eq(BitStringPointInformationSequence.class.getAnnotation(ASDU.class).id()),
				eq(BitStringPointInformationSequence.class.getAnnotation(ASDU.class).informationStructure()),
				any(BitStringPointSequenceCodec.class));
		verify(mockedManager).registerCodec(eq(MeasuredValueScaledSingle.class.getAnnotation(ASDU.class).id()),
				eq(MeasuredValueScaledSingle.class.getAnnotation(ASDU.class).informationStructure()),
				any(MeasuredValueScaledSingleCodec.class));
		verify(mockedManager).registerCodec(eq(MeasuredValueScaledSequence.class.getAnnotation(ASDU.class).id()),
				eq(MeasuredValueScaledSequence.class.getAnnotation(ASDU.class).informationStructure()),
				any(MeasuredValueScaledSequenceCodec.class));
		verify(mockedManager).registerCodec(eq(InterrogationCommand.class.getAnnotation(ASDU.class).id()),
				eq(InterrogationCommand.class.getAnnotation(ASDU.class).informationStructure()),
				any(InterrogationCommandCodec.class));
		verify(mockedManager).registerCodec(eq(BitStringCommand.class.getAnnotation(ASDU.class).id()),
				eq(BitStringCommand.class.getAnnotation(ASDU.class).informationStructure()),
				any(BitStringCommandCodec.class));
	}

	@Test
	public void testInitializeChannel() throws Exception {
		List<List<?>> commandHandlerArgs = new ArrayList<>();
		try (MockedConstruction<Iec104MessageChannelHandler> channelHandlerCons =
					 mockConstruction(Iec104MessageChannelHandler.class);
			 MockedConstruction<Iec104CommandHandler> commandHandlerCons =
					 mockConstruction(Iec104CommandHandler.class,
							 (mock, mctx) -> commandHandlerArgs.add(new ArrayList<>(mctx.arguments())))) {
			when(mockedSocketChannel.pipeline()).thenReturn(mockedPipeline);

			module.initializeChannel(mockedSocketChannel, mockedMessageChannel);

			assertEquals(1, channelHandlerCons.constructed().size());
			assertEquals(1, commandHandlerCons.constructed().size());
			assertEquals(mockedCache, commandHandlerArgs.get(0).get(0));
			verify(mockedSocketChannel, times(3)).pipeline();
		}
	}

	@Test
	public void testDispose() {
		module.dispose();

		assertNull(Whitebox.getInternalState(module, "messageChannel"));
		assertNull(Whitebox.getInternalState(module, "socketChannel"));
		assertNull(Whitebox.getInternalState(module, "messageManager"));
		assertNull(Whitebox.getInternalState(module, "server"));
	}

	@Test
	public void testIsConnectedTrue() {
		Whitebox.setInternalState(module, "server", mockedServer);
		Whitebox.setInternalState(module, "messageManager", mockedManager);
		Whitebox.setInternalState(module, "socketChannel", mockedSocketChannel);
		Whitebox.setInternalState(module, "messageChannel", mockedMessageChannel);

		boolean result = module.isConnected();

		assertTrue(result);
	}

	@Test
	public void testSend() throws Exception {
		Whitebox.setInternalState(module, "messageChannel", mockedMessageChannel);
		Whitebox.setInternalState(module, "socketChannel", mockedSocketChannel);
		Object asdu = new BitStringCommand(null, null);
		when(mockedSocketChannel.pipeline()).thenReturn(mockedPipeline);

		module.send(asdu);

		verify(mockedMessageChannel).write(any(), eq(asdu), any());
	}

	@Test
	public void testSendWithExceptionIsCaught() {
		Whitebox.setInternalState(module, "messageChannel", mockedMessageChannel);
		Whitebox.setInternalState(module, "socketChannel", mockedSocketChannel);
		Object asdu = new BitStringCommand(null, null);
		when(mockedSocketChannel.pipeline()).thenThrow(new NullPointerException());

		module.send(asdu);

		assertTrue("Exception should be caught", true);
	}
}
