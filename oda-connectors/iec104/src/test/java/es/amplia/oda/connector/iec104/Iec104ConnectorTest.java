package es.amplia.oda.connector.iec104;

import es.amplia.oda.connector.iec104.configuration.Iec104ConnectorConfiguration;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;
import io.netty.channel.socket.SocketChannel;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.InterrogationCommand;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Iec104ConnectorTest {

	private Iec104Connector connector;

	@Mock
	ScadaDispatcherProxy mockedDispatcher;
	@Mock
	Iec104ServerModule mockedModule;
	@Mock
	ServerKeepAlive mockedServer;
	@Mock
	SocketChannel mockedChannel;

	@Before
	public void prepareForTest() {
		connector = new Iec104Connector(mockedDispatcher);
		Whitebox.setInternalState(connector, "serverModule", mockedModule);
		Whitebox.setInternalState(connector, "server", mockedServer);
		Whitebox.setInternalState(connector, "socketChannel", mockedChannel);
		Whitebox.setInternalState(connector, "spontaneousEnabled", true);
		Whitebox.setInternalState(connector, "commonAddress", 0);
	}

	@Test
	public void testClearLastConfiguration() throws Exception {
		connector.close();

		verify(mockedModule).dispose();
		verify(mockedChannel).close();
		verify(mockedServer).close();
		assertNull(Whitebox.getInternalState(connector, "server"));
		assertNull(Whitebox.getInternalState(connector, "socketChannel"));
	}

	@Test
	public void testClearLastConfigurationWithException() {
		when(mockedChannel.close()).thenThrow(new NumberFormatException());

		connector.close();
	}

	@Test
	public void testUplink() {
		when(mockedModule.isConnected()).thenReturn(true);
		int index = 23;
		short value = 53;

		connector.uplink(index, value, InterrogationCommand.class.toString(), System.currentTimeMillis());

		verify(mockedModule).send(any(InterrogationCommand.class));
	}

	@Test
	public void testBadUplink() {
		when(mockedModule.isConnected()).thenReturn(false);
		int index = 23;
		short value = 53;

		connector.uplink(index, value, InterrogationCommand.class.toString(), System.currentTimeMillis());
	}

	@Test
	public void testLoadConfiguration() {
		Iec104ConnectorConfiguration configuration = Iec104ConnectorConfiguration.builder()
				.spontaneousEnabled(true)
				.commonAddress(1)
				.originatorAddress(0)
				.localPort(4000)
				.localAddress("localhost")
				.build();

		connector.loadConfiguration(configuration);

		assertEquals(1, Whitebox.getInternalState(connector, "commonAddress"));
		assertEquals(true, Whitebox.getInternalState(connector, "spontaneousEnabled"));
	}

	@Test
	public void testLoadConfigurationWithException() {
		Iec104ConnectorConfiguration configuration = Iec104ConnectorConfiguration.builder()
				.spontaneousEnabled(true)
				.commonAddress(1)
				.originatorAddress(0)
				.localPort(4000)
				.localAddress("invalidHost")
				.build();

		connector.loadConfiguration(configuration);
	}
}
