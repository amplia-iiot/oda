package es.amplia.oda.connector.dnp3;

import com.automatak.dnp3.enums.ChannelState;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.*;

public class DNP3ChannelListenerTest {

    private static final ChannelState TEST_CHANNEL_STATE = ChannelState.OPENING;

    private static final String CURRENT_STATE_FIELD_NAME = "currentState";

    private final DNP3ChannelListener channelListener = new DNP3ChannelListener();

    @Test
    public void testOnStateChange() {
        channelListener.onStateChange(TEST_CHANNEL_STATE);

        assertEquals(TEST_CHANNEL_STATE, Whitebox.getInternalState(channelListener, CURRENT_STATE_FIELD_NAME));
    }

    @Test
    public void testIsOpenOpenCurrentChannelState() {
        Whitebox.setInternalState(channelListener, CURRENT_STATE_FIELD_NAME, ChannelState.OPEN);

        assertTrue(channelListener.isOpen());
    }

    @Test
    public void testIsOpenNotOpenCurrentChannelState() {
        Whitebox.setInternalState(channelListener, CURRENT_STATE_FIELD_NAME, ChannelState.CLOSED);

        assertFalse(channelListener.isOpen());
    }
}