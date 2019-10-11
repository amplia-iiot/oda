package es.amplia.oda.connector.iec104;

import es.amplia.oda.connector.iec104.codecs.*;
import es.amplia.oda.connector.iec104.types.BitStringCommand;
import es.amplia.oda.connector.iec104.types.BitStringPointInformationSequence;
import es.amplia.oda.connector.iec104.types.BitStringPointInformationSingle;
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
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Iec104ServerModule.class)
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
	private Iec104MessageChannelHandler mockedChannelHandler;
	@Mock
	private Iec104CommandHandler mockedCommandHandler;
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
				any(SinglePointSingleCodec.class));
		verify(mockedManager).registerCodec(eq(SinglePointInformationSequence.class.getAnnotation(ASDU.class).id()),
				eq(SinglePointInformationSequence.class.getAnnotation(ASDU.class).informationStructure()),
				any(SinglePointSequenceCodec.class));
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
		whenNew(Iec104MessageChannelHandler.class).withAnyArguments().thenReturn(mockedChannelHandler);
		whenNew(Iec104CommandHandler.class).withAnyArguments().thenReturn(mockedCommandHandler);
		when(mockedSocketChannel.pipeline()).thenReturn(mockedPipeline);

		module.initializeChannel(mockedSocketChannel, mockedMessageChannel);

		verifyNew(Iec104MessageChannelHandler.class).withArguments(any(ProtocolOptions.class), any(MessageManager.class));
		verifyNew(Iec104CommandHandler.class)
				.withArguments(eq(mockedCache), any(ScadaDispatcherProxy.class), any(Integer.class));
		verify(mockedSocketChannel, times(3)).pipeline();
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
