package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DatastreamsSettersLocatorOsgi implements DatastreamsSettersLocator {
    private static final Logger logger = LoggerFactory.getLogger(DatastreamsSettersLocatorOsgi.class);

    private ServiceTracker<DatastreamsSetter, DatastreamsSetter> datastreamsSetterTracker;
    
    public DatastreamsSettersLocatorOsgi(BundleContext bundleContext) throws InvalidSyntaxException {
        Filter serviceFilter = bundleContext.createFilter("(&(objectClass="+DatastreamsSetter.class.getName()+"))");
        datastreamsSetterTracker = new ServiceTracker<>(bundleContext, serviceFilter, null);
        datastreamsSetterTracker.open();
    }
    
    
    @Override
    public List<DatastreamsSetter> getDatastreamsSetters() {
        List<DatastreamsSetter> returned = new ArrayList<>();
        Object[] providers = datastreamsSetterTracker.getServices();
        if(providers==null) {
            logger.error("There are no OSGi bundles for DatastreamsSetter");
            return returned;
        }

        for(Object obj: providers) {
            if(!(obj instanceof DatastreamsSetter)) {
                logger.error("DatastreamsSetter found is not a subclass of DatastreamsSetter");
                continue;
            }
            DatastreamsSetter provider = (DatastreamsSetter) obj;
            returned.add(provider);
        }
        logger.debug("{} DatastreamsSetters currently registered in the system", returned.size());
        return returned;
    }
}
