package es.amplia.oda.core.commons.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

public class OsgiContext {

    private final DatastreamsGettersFinder datastreamsGettersFinder;
    private final DatastreamsSettersFinder datastreamsSettersFinder;
    private final BundleContext bundleContext;
    private HashMap<String, ServiceLocatorOsgi<Object>> bundleContextMap;

    public OsgiContext(BundleContext bundleContext, DatastreamsGettersFinder gettersFinder, DatastreamsSettersFinder settersFinder) {
        this.datastreamsGettersFinder = gettersFinder;
        this.datastreamsSettersFinder = settersFinder;
        this.bundleContext = bundleContext;
        this.bundleContextMap = new HashMap<>();
    }

    public Object getBundle(String bundleName) {
        List<Object> aux = getBundles(bundleName);
        if ( (aux == null) || aux.isEmpty()) return null;
        else return aux.get(0);
    }

    public List<Object> getBundles(String bundleName) {
        ServiceLocatorOsgi<Object> ret = this.bundleContextMap.get(bundleName);
        if (ret == null) {
            try {
                ServiceLocatorOsgi serviceLocator = new ServiceLocatorOsgi<>(bundleContext, Class.forName(bundleName));
                this.bundleContextMap.put(bundleName, serviceLocator);
                return serviceLocator.findAll();
            } catch (Throwable e) {
                return null;
            }
        }
        return ret.findAll();
    }

    public DatastreamsGetter getGetter(String device, String datastreamId) {
        List<DatastreamsGetter> aux = getGetters(device, datastreamId);
        if ( (aux == null) || aux.isEmpty()) return null;
        else return aux.get(0);
    }

    public List<DatastreamsGetter> getGetters(String device, String datastreamId) {
        return datastreamsGettersFinder.getGettersOfDevice(device).stream().filter(getter -> getter.getDatastreamIdSatisfied().equals(datastreamId)).collect(Collectors.toList());
    }

    public DatastreamsSetter getSetter(String device, String datastreamId) {
        List<DatastreamsSetter> aux = getSetters(device, datastreamId);
        if ( (aux == null) || aux.isEmpty()) return null;
        else return aux.get(0);
    }

    public List<DatastreamsSetter> getSetters(String device, String datastreamId) {
        return new ArrayList<>(datastreamsSettersFinder.getSettersSatisfying(device, Collections.singleton(datastreamId)).getSetters().values());
    }

    public void close() {
        this.bundleContextMap.values().forEach(sl -> sl.close());
    }
}
