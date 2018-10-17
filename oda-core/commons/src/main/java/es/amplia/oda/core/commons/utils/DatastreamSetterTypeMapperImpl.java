package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.interfaces.DatastreamSetterTypeMapper;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

import lombok.Value;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Arrays;

public class DatastreamSetterTypeMapperImpl implements DatastreamSetterTypeMapper, AutoCloseable {

	private final ServiceTracker<DatastreamsSetter, DatastreamsSetter> serviceTracker;
    private static final Logger logger = LoggerFactory.getLogger(DatastreamSetterTypeMapperImpl.class);

    public DatastreamSetterTypeMapperImpl(BundleContext bundleContext) {
        serviceTracker = new ServiceTracker<>(bundleContext, DatastreamsSetter.class, null);
        serviceTracker.open();
    }
    
    @Value
    private static class Pair {
    	String id;
    	Type type;
    }
	@Override
	public Type getTypeOf(String id) {
        Object[] setters = serviceTracker.getServices();
        if(setters == null) {
            logger.warn("No service implementation of {} available in OSGi", DatastreamsSetter.class.getName());
            return null;
        }
        
        Type found = Arrays.stream(setters)
                .map(o -> (DatastreamsSetter) o)
                .map(ds -> new Pair(ds.getDatastreamIdSatisfied(), ds.getDatastreamType()))
                .filter(p -> p.getId().equals(id))
                .map(Pair::getType)
                .findFirst()
                .orElse(null);

        if (found != null)
            logger.debug("The type of '{}' is '{}'", id, found.getTypeName());
        else
            logger.debug("The type of '{}' is not found", id);
		
        return found;
	}

	@Override
	public void close() {
		serviceTracker.close();
	}

}
