package es.amplia.oda.hardware.atmanager.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ATManager {
    void registerEvent(String atEvent, Consumer<ATEvent> function) throws AlreadyRegisteredException;

    void unregisterEvent(String atEvent);

    void registerCommand(String atCmd, Function<ATCommand, ATResponse> commandHandler) throws AlreadyRegisteredException;

    void unregisterCommand(String atCmd);

    void process(String line); //Whenever a complete line (discarding \n) is received in the CommPort, this function MUST be called.

    CompletableFuture<ATResponse> send(ATCommand cmd, long timeout, TimeUnit unit); //Used to issue AT commands to peer.

    //TODO: CompletableFuture<ATResponse> send(List<ATCommand> cmd);
    void send(ATEvent evt); //Used to send responses, both solicited and unsolicited.

    public class EOFException extends Exception {
        private static final long serialVersionUID = -883441841752633569L;
    }

    public class AlreadyRegisteredException extends Exception {
        private static final long serialVersionUID = -3794438709003306250L;

        public AlreadyRegisteredException(String msg) {
            super(msg);
        }
    }
}
