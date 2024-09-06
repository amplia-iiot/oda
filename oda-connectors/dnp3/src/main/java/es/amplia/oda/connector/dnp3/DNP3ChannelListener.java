package es.amplia.oda.connector.dnp3;

import com.automatak.dnp3.ChannelListener;
import com.automatak.dnp3.enums.ChannelState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DNP3ChannelListener implements ChannelListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DNP3ChannelListener.class);

    private ChannelState currentState = ChannelState.SHUTDOWN;

    @Override
    public void onStateChange(ChannelState channelState) {
        currentState = channelState;
        LOGGER.debug("Channel state: {}", channelState);
    }

    boolean isOpen() {
        return currentState.equals(ChannelState.OPEN);
    }
}
