package es.amplia.oda.datastreams.lora.configuration;

import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.lora.LoraDatastreamsOrchestrator;
import es.amplia.oda.datastreams.lora.datastreams.LoraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;

public class LoraDatastreamsConfigurationHandler implements ConfigurationUpdateHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoraDatastreamsConfigurationHandler.class);

	/**
	 * No sería necesario para lo que queremos hacer ahora. Nos interesa coger el contenido del packet y mandar el JSON
	 * que corresponde, no desencriptar y demás.
	 * Entonces, este módulo pasaría a no tener configuración?
	 * No, pero déjalo por el momento. Quitarlo es fácil, pero aún debemos plantear el manejo de operaciones.
 	 */

	private static final String DEVICE_ID_PROPERTY_NAME = "deviceId";

	private final LoraDatastreamsOrchestrator loraDatastreamsOrchestrator;
	private LoraDatastreamsConfiguration currentConfiguration;

	public LoraDatastreamsConfigurationHandler(LoraDatastreamsOrchestrator loraDatastreamsOrchestrator) {
		this.loraDatastreamsOrchestrator = loraDatastreamsOrchestrator;
	}


	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");
		String deviceId = Optional.of(props.get(DEVICE_ID_PROPERTY_NAME)).map(String::valueOf).orElseThrow(
				() -> new LoraException("Missing required property " + DEVICE_ID_PROPERTY_NAME));

		currentConfiguration = new LoraDatastreamsConfiguration(deviceId);
		LOGGER.info("New configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		loraDatastreamsOrchestrator.loadConfiguration(this.currentConfiguration);
	}
}
