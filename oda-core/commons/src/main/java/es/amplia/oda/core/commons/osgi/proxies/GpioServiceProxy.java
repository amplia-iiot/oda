package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.gpio.*;

import org.osgi.framework.BundleContext;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class GpioServiceProxy implements GpioService, AutoCloseable {

    private final OsgiServiceProxy<GpioService> proxy;


    public GpioServiceProxy(BundleContext bundleContext) {
        this.proxy = new OsgiServiceProxy<>(GpioService.class, bundleContext);
    }

    private <T> T returnValueOrExceptionIfNull(Supplier<T> supplier) {
        Optional<T> optional = Optional.ofNullable(supplier.get());
        return optional.orElseThrow(() -> new GpioDeviceException("No GPIO service available"));
    }

    @Override
    public GpioPin getPinByName(String pinName) throws GpioDeviceException {
        return returnValueOrExceptionIfNull(() -> proxy.callFirst(gpioService -> gpioService.getPinByName(pinName)));
    }

    @Override
    public GpioPin getPinByName(String pinName, GpioDirection direction, GpioMode mode, GpioTrigger trigger,
                                boolean activeLow, boolean initialValue)
            throws GpioDeviceException {
        return returnValueOrExceptionIfNull(() ->
                proxy.callFirst(gpioService -> gpioService.getPinByName(pinName, direction, mode, trigger, activeLow, initialValue)));
    }

    @Override
    public GpioPin getPinByIndex(int index) throws GpioDeviceException {
        return returnValueOrExceptionIfNull(() -> proxy.callFirst(gpioService -> gpioService.getPinByIndex(index)));
    }

    @Override
    public GpioPin getPinByIndex(int index, GpioDirection direction, GpioMode mode, GpioTrigger trigger,
                                 boolean activeLow, boolean initialValue) {
        return returnValueOrExceptionIfNull(() ->
                proxy.callFirst(gpioService -> gpioService.getPinByIndex(index, direction, mode, trigger, activeLow, initialValue)));
    }

    @Override
    public Map<Integer, GpioPin> getAvailablePins() {
        return proxy.callFirst(GpioService::getAvailablePins);
    }

    @Override
    public void close() {
        proxy.close();
    }
}
