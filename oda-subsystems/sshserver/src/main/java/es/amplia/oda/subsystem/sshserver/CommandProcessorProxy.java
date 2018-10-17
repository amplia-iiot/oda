package es.amplia.oda.subsystem.sshserver;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.osgi.framework.BundleContext;

import java.io.InputStream;
import java.io.OutputStream;

class CommandProcessorProxy implements CommandProcessor, AutoCloseable {

    private final OsgiServiceProxy<CommandProcessor> proxy;

    CommandProcessorProxy(BundleContext bundleContext) {
        this.proxy = new OsgiServiceProxy<>(CommandProcessor.class, bundleContext);
    }

    @Override
    public CommandSession createSession(InputStream in, OutputStream out, OutputStream err) {
        return proxy.callFirst(commandProcessor -> commandProcessor.createSession(in, out, err));
    }

    @Override
    public CommandSession createSession(CommandSession parent) {
        return proxy.callFirst(commandProcessor -> commandProcessor.createSession(parent));
    }

    @Override
    public void close() {
        proxy.close();
    }
}
