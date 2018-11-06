package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DatastreamsGettersLocatorOsgi implements DatastreamsGettersLocator {
    private static final Logger logger = LoggerFactory.getLogger(DatastreamsGettersLocatorOsgi.class);

    private ServiceTracker<DatastreamsGetter, DatastreamsGetter> datastreamsGetterTracker;
    
    public DatastreamsGettersLocatorOsgi(BundleContext bundleContext) {
        datastreamsGetterTracker = new ServiceTracker<>(bundleContext, DatastreamsGetter.class, null);
        datastreamsGetterTracker.open();
    }
    
    
    @Override
    public List<DatastreamsGetter> getDatastreamsGetters() {
        List<DatastreamsGetter> returned = new ArrayList<>();
        Object[] providers = datastreamsGetterTracker.getServices();
        if(providers==null) {
            logger.error("There are no OSGi bundles for DatastreamsGetter");
            return returned;
        }

        for(Object obj: providers) {
            if(!(obj instanceof DatastreamsGetter)) {
                logger.error("DatastreamsGetter found is not a subclass of DatastreamsGetter");
                continue;
            }
            DatastreamsGetter provider = (DatastreamsGetter) obj;
            returned.add(provider);
        }
        logger.debug("{} DatastreamsGetters currently registered in the system", returned.size());
        return returned;
    }

}
