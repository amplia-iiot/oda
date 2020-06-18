package es.amplia.oda.hardware.atserver.api;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.hardware.atmanager.api.ATCommand;
import es.amplia.oda.hardware.atmanager.api.ATEvent;
import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.hardware.atmanager.api.ATResponse;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class ATManagerProxy implements ATManager, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ATManagerProxy.class);

    private final OsgiServiceProxy<ATManager> proxy;

    public ATManagerProxy(BundleContext bundleContext) {
        this.proxy = new OsgiServiceProxy<>(ATManager.class, bundleContext);
    }

    @Override
    public void registerEvent(String atEvent, Consumer<ATEvent> eventHandler) {
        proxy.consumeFirst(atManager -> {
            try {
                atManager.registerEvent(atEvent, eventHandler);
            } catch (AlreadyRegisteredException atManagerException) {
                logger.warn(atManagerException.getMessage());
            }
        });
    }

    @Override
    public void unregisterEvent(String atEvent) {
        proxy.consumeFirst(atManager -> atManager.unregisterEvent(atEvent));
    }

    @Override
    public void registerCommand(String atCommand, Function<ATCommand, ATResponse> commandHandler) {
        proxy.consumeFirst(atManager -> {
            try {
                atManager.registerCommand(atCommand, commandHandler);
            } catch (AlreadyRegisteredException atManagerException) {
                logger.warn(atManagerException.getMessage());
            }
        });
    }

    @Override
    public void unregisterCommand(String atCommand) {
        proxy.consumeFirst(atManager -> atManager.unregisterCommand(atCommand));
    }

    @Override
    public void process(String line) {
        proxy.consumeFirst(atManager -> atManager.process(line));
    }

    @Override
    public CompletableFuture<ATResponse> send(ATCommand atCommand, long timeout, TimeUnit unit) {
        return Optional.ofNullable(proxy.callFirst(atManager -> atManager.send(atCommand, timeout, unit)))
                .orElse(CompletableFuture.completedFuture(ATResponse.error("AT Manager is not available")));
    }

    @Override
    public void send(ATEvent atEvent) {
        proxy.consumeFirst(atManager -> atManager.send(atEvent));
    }

    @Override
    public void close() {
        proxy.close();
    }
}
