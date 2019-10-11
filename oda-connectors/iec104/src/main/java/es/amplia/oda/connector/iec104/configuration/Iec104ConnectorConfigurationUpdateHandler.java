package es.amplia.oda.connector.iec104.configuration;

import es.amplia.oda.connector.iec104.Iec104Connector;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;

public class Iec104ConnectorConfigurationUpdateHandler implements ConfigurationUpdateHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(Iec104ConnectorConfigurationUpdateHandler.class);

	private static final String LOCAL_ADDRESS_PROPERTY = "localAddress";
	private static final String LOCAL_PORT_PROPERTY = "localPort";
	private static final String ORIGINATOR_ADDRESS_PROPERTY = "originatorAddress";
	private static final String COMMON_ADDRESS_PROPERTY = "commonAddress";
	private static final String SPONTANEOUS_ENABLED_PROPERTY = "spontaneousEnabled";

	private final Iec104Connector connector;
	private Iec104ConnectorConfiguration currentConfiguration;

	public Iec104ConnectorConfigurationUpdateHandler(Iec104Connector connector) {
		this.connector = connector;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");

		Iec104ConnectorConfiguration.Iec104ConnectorConfigurationBuilder builder = Iec104ConnectorConfiguration.builder();

		Optional.ofNullable((String) props.get(LOCAL_ADDRESS_PROPERTY))
				.ifPresent(builder::localAddress);
		Optional.ofNullable((String) props.get(LOCAL_PORT_PROPERTY))
				.ifPresent(value -> builder.localPort(Integer.parseInt(value)));
		Optional.ofNullable((String) props.get(ORIGINATOR_ADDRESS_PROPERTY))
				.ifPresent(value -> builder.originatorAddress(Integer.parseInt(value)));
		Optional.ofNullable((String) props.get(COMMON_ADDRESS_PROPERTY))
				.ifPresent(value -> builder.commonAddress(Integer.parseInt(value)));
		Optional.ofNullable((String) props.get(SPONTANEOUS_ENABLED_PROPERTY))
				.ifPresent(value -> builder.spontaneousEnabled(Boolean.parseBoolean(value)));
		currentConfiguration = builder.build();

		LOGGER.info("New configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		if(currentConfiguration != null) {
			LOGGER.info("Applying last configuration");
			connector.loadConfiguration(currentConfiguration);
			LOGGER.info("Last configuration applied");
		}
	}
}
