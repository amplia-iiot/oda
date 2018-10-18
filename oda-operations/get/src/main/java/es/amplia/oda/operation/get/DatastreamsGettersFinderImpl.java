package es.amplia.oda.operation.get;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

class DatastreamsGettersFinderImpl implements DatastreamsGetterFinder {
	private static final Logger logger = LoggerFactory.getLogger(DatastreamsGettersFinderImpl.class);

	private final DatastreamsGettersLocator datastreamsGettersLocator;

	DatastreamsGettersFinderImpl(DatastreamsGettersLocator datastreamsGettersLocator) {
		this.datastreamsGettersLocator = datastreamsGettersLocator;
	}
	
	/**
	 * This function will try to find the DatastreamsGetters that can generate values for the parameters specified.
	 * Note that it is possible to return a set of DatastreamsGetters that, altogether, will generate more Datastreams
	 * than the ones specified in the parameters.
	 * @param deviceId The deviceId that the returned DatastreamsGetters must manage. Use "" for ODA itself.
	 * @param datastreamIdentifiers The Datastream identifiers that the returned DatastreamsGetters must generate. Not null.
	 * @return A list with all the datastreams found that will generate values for the parameters specified, and a set
	 *  of identifiers that no DatastreamsGetter manage.
	 */
	@Override
	public Return getGettersSatisfying(String deviceId, Set<String> datastreamIdentifiers) {
		try {
			final Set<String> notFoundIds = new HashSet<>(datastreamIdentifiers);
			List<DatastreamsGetter> providers = datastreamsGettersLocator.getDatastreamsGetters().stream()
					.filter(dsp-> datastreamIdentifiers.contains(dsp.getDatastreamIdSatisfied()))
					.filter(dsp-> dsp.getDevicesIdManaged().contains(deviceId))
					.peek(dsp-> notFoundIds.remove(dsp.getDatastreamIdSatisfied()))
					.collect(Collectors.toList());
			return new Return(providers, notFoundIds);
		} catch (Exception e) {
			logger.error("Exception when trying to determine providers satisfying {}/{}: {}", deviceId, datastreamIdentifiers, e.getMessage());
			return new Return(Collections.emptyList(), new HashSet<>(datastreamIdentifiers));
		}
	}

}
