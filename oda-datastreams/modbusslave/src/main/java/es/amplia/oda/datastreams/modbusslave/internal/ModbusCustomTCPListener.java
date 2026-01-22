package es.amplia.oda.datastreams.modbusslave.internal;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.net.TCPConnectionHandler;
import com.ghgande.j2mod.modbus.net.TCPSlaveConnection;
import com.ghgande.j2mod.modbus.util.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Map;

public class ModbusCustomTCPListener extends AbstractModbusListener {

    private static final Logger logger = LoggerFactory.getLogger(ModbusCustomTCPListener.class);

    private ServerSocket serverSocket = null;
    private final ThreadPool threadPool;
    private Thread listener;
    private Map<String, CustomModbusRequestHandler> requestHandlers;
    private int timeoutMillies = Modbus.DEFAULT_TIMEOUT;


    public ModbusCustomTCPListener(int poolsize, Map<String, CustomModbusRequestHandler> requestHandlers) {
        this(poolsize);
        this.requestHandlers = requestHandlers;
    }

    private ModbusCustomTCPListener(int poolsize) {
        threadPool = new ThreadPool(poolsize);
        try {
            address = InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
        }
        catch (UnknownHostException ex) {
            // Can't happen -- size is fixed.
        }
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeoutMillies = timeout;
        super.setTimeout(timeout);
        if (serverSocket != null && listening) {
            try {
                serverSocket.setSoTimeout(timeout);
            }
            catch (SocketException e) {
                logger.error("Cannot set socket timeout", e);
            }
        }
    }

    @Override
    public void run() {
        try {
            /*
             * A server socket is opened with a connectivity queue of a size
             * specified in int floodProtection. Concurrent login handling under
             * normal circumstances should be alright, denial-of-service
             * attacks via massive parallel program logins can probably be
             * prevented.
             */
            int floodProtection = 100;
            serverSocket = new ServerSocket(port, floodProtection, address);
            serverSocket.setSoTimeout(timeoutMillies);
            logger.debug("Listening to {} (Port {})", serverSocket.toString(), port);
        }

        // Catch any fatal errors and set the listening flag to false to indicate an error
        catch (Exception e) {
            logger.error("Cannot start TCP listener : ", e);
            listening = false;
            return;
        }

        listener = Thread.currentThread();
        try {

            // Infinite loop, taking care of resources in case of a lot of
            // parallel logins
            listening = true;
            while (listening) {
                Socket incoming;
                try {
                    incoming = serverSocket.accept();
                }
                catch (SocketTimeoutException e) {
                    continue;
                }
                logger.debug("Making new connection {}", incoming.toString());
                if (listening) {
                    // obtener la ip del dispositivo que manda el mensaje
                    String ipRemota = incoming.getInetAddress().getHostAddress();
                    //logger.debug("New device connected from : {}", ipRemota);

                    // obtener el handler asociado a la ip del dispositivo remoto
                    CustomModbusRequestHandler requestHandler = requestHandlers.get(ipRemota);
                    TCPSlaveConnection slave = new TCPSlaveConnection(incoming);
                    slave.setTimeout(timeoutMillies);
                    threadPool.execute(new TCPConnectionHandler(requestHandler, slave));
                }
                else {
                    incoming.close();
                }
            }
        }
        catch (IOException e) {
            logger.error("Problem handling connection : ", e);
        }
    }

    @Override
    public void stop() {
        listening = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (listener != null) {
                listener.join(timeoutMillies);
            }
            if (threadPool != null) {
                threadPool.close();
            }
        }
        catch (Exception ex) {
            logger.error("Error while stopping ModbusTCPListener", ex);
        }
    }
}
