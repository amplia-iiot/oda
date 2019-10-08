package es.amplia.oda.connector.iec104;

import es.amplia.oda.connector.iec104.types.BitstringCommand;
import es.amplia.oda.core.commons.interfaces.ScadaDispatcher;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.DataTransmissionMessage;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.InterrogationCommand;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.SingleCommand;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Iec104CommandHandlerTest {

	Iec104CommandHandler commandHandler;

	@Mock
	ScadaDispatcherProxy mockedDispatcher;
	@Mock
	ChannelHandlerContext mockedContext;
	@Mock
	ChannelPromise mockedPromise;
	@Mock
	ASDUHeader mockedHeader;
	@Mock
	ASDUAddress mockedAsduAddress;
	@Mock
	IOException mockedException;

	@Before
	public void prepareForTest() {
		commandHandler = new Iec104CommandHandler(mockedDispatcher, 0);
	}

	@Test
	public void testChannelActive() throws Exception {
		when(mockedContext.write(any(), any())).thenReturn(null);
		when(mockedContext.newPromise()).thenReturn(mockedPromise);

		commandHandler.channelActive(mockedContext);

		verify(mockedContext).write(eq(DataTransmissionMessage.CONFIRM_START), eq(mockedPromise));
	}

	@Test
	public void testChannelReadInterrogationCommandActivated() throws Exception {
		InterrogationCommand ic = new InterrogationCommand(mockedHeader,  Integer.valueOf(0).shortValue());
		when(mockedHeader.getCauseOfTransmission()).thenReturn(new CauseOfTransmission(StandardCause.ACTIVATED));
		when(mockedHeader.getAsduAddress()).thenReturn(mockedAsduAddress);
		when(mockedContext.writeAndFlush(any(), any())).thenReturn(null);
		when(mockedContext.newPromise()).thenReturn(mockedPromise);

		commandHandler.channelRead(mockedContext, ic);

		verify(mockedContext).writeAndFlush(any(InterrogationCommand.class), eq(mockedPromise));
	}

	@Test
	public void testChannelReadInterrogationCommandDeactivated() throws Exception {
		InterrogationCommand ic = new InterrogationCommand(mockedHeader,  Integer.valueOf(0).shortValue());
		when(mockedHeader.getCauseOfTransmission()).thenReturn(new CauseOfTransmission(StandardCause.DEACTIVATED));
		when(mockedHeader.getAsduAddress()).thenReturn(null);
		when(mockedContext.writeAndFlush(any(), any())).thenReturn(null);
		when(mockedContext.newPromise()).thenReturn(mockedPromise);

		commandHandler.channelRead(mockedContext, ic);

		verify(mockedContext).writeAndFlush(any(InterrogationCommand.class), eq(mockedPromise));
	}

	@Test
	public void testChannelReadBitstringCommandActivated() throws Exception {
		BitstringCommand bc = new BitstringCommand(mockedHeader, InformationObjectAddress.DEFAULT, new byte[] {0x00, 0x01, 0x02, 0x03});
		when(mockedHeader.getCauseOfTransmission()).thenReturn(new CauseOfTransmission(StandardCause.ACTIVATED));
		when(mockedHeader.getAsduAddress()).thenReturn(mockedAsduAddress);
		when(mockedContext.writeAndFlush(any(), any())).thenReturn(null);
		when(mockedContext.newPromise()).thenReturn(mockedPromise);
		when(mockedDispatcher.process(any(), anyInt(), any(), any())).thenReturn(null);

		commandHandler.channelRead(mockedContext, bc);

		verify(mockedContext).writeAndFlush(any(BitstringCommand.class), eq(mockedPromise));
		verify(mockedDispatcher).process(eq(ScadaDispatcher.ScadaOperation.DIRECT_OPERATE_NO_ACK),
				eq(InformationObjectAddress.DEFAULT.getAddress()),
				eq(bc.parseBytestring()),
				eq(BitstringCommand.class.getAnnotation(ASDU.class).name()));
	}

	@Test
	public void testChannelReadBitstringCommandDeactivated() throws Exception {
		BitstringCommand bc = new BitstringCommand(mockedHeader, InformationObjectAddress.DEFAULT, new byte[] {0x00, 0x01, 0x02, 0x03});
		when(mockedHeader.getCauseOfTransmission()).thenReturn(new CauseOfTransmission(StandardCause.DEACTIVATED));
		when(mockedHeader.getAsduAddress()).thenReturn(null);
		when(mockedContext.writeAndFlush(any(), any())).thenReturn(null);
		when(mockedContext.newPromise()).thenReturn(mockedPromise);

		commandHandler.channelRead(mockedContext, bc);

		verify(mockedContext).writeAndFlush(any(BitstringCommand.class), eq(mockedPromise));
		verify(mockedDispatcher).process(eq(ScadaDispatcher.ScadaOperation.DIRECT_OPERATE_NO_ACK),
				eq(InformationObjectAddress.DEFAULT.getAddress()),
				eq(bc.parseBytestring()),
				eq(BitstringCommand.class.getAnnotation(ASDU.class).name()));
	}

	@Test
	public void testChannelReadUnknownRequest() throws Exception {
		SingleCommand sc = new SingleCommand(mockedHeader, InformationObjectAddress.DEFAULT, false);
		when(mockedHeader.getCauseOfTransmission()).thenReturn(new CauseOfTransmission(StandardCause.DEACTIVATED));
		when(mockedHeader.getAsduAddress()).thenReturn(null);
		when(mockedContext.writeAndFlush(any(), any())).thenReturn(null);
		when(mockedContext.newPromise()).thenReturn(mockedPromise);

		commandHandler.channelRead(mockedContext, sc);

		verifyZeroInteractions(mockedContext);
	}

	@Test
	public void testChannelReadWithException() throws Exception {
		BitstringCommand bc = new BitstringCommand(mockedHeader, InformationObjectAddress.DEFAULT, new byte[] {0x00, 0x01, 0x02, 0x03});
		when(mockedContext.writeAndFlush(any())).thenThrow(new ArrayIndexOutOfBoundsException());

		commandHandler.channelRead(mockedContext, bc);
	}
}
