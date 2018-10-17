package es.amplia.oda.hardware.atmanager;

import es.amplia.oda.hardware.atmanager.ATParser.LineType;
import es.amplia.oda.hardware.atmanager.ATParser.Result;
import es.amplia.oda.hardware.atmanager.api.ATCommand;
import es.amplia.oda.hardware.atmanager.api.ATEvent;
import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.hardware.atmanager.api.ATResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ATManagerImpl implements ATManager {
    private static final Logger logger = LoggerFactory.getLogger(ATManagerImpl.class);

    private final Map<String, Consumer<ATEvent>> registeredEvents = new HashMap<>();
    private final Map<String, Function<ATCommand, ATResponse>> registeredCommands = new HashMap<>();
    private final ATParser atParser;
    private final OutputStream outputStream;
    private final ScheduledExecutorService timemoutExecutor = Executors.newScheduledThreadPool(1);
    private final Semaphore semaphore = new Semaphore(1, true);

    private CompletableFuture<ATResponse> commandFuture = null;
    private List<ATEvent> partialResponses = null;
    private StringBuilder body = null;
    private ScheduledFuture timeoutTask;

    public ATManagerImpl(ATParser atParser, OutputStream outputStream) {
        this.atParser = atParser;
        this.outputStream = outputStream;
    }

    @Override
    public void registerEvent(String atEvent, Consumer<ATEvent> function) throws AlreadyRegisteredException {
        if (registeredEvents.containsKey(atEvent)) {
            logger.error("Trying to register '{}' two times.", atEvent);
            throw new AlreadyRegisteredException("Cannot register '" + atEvent + "' two times.");
        }
        if (registeredCommands.containsKey(atEvent)) {
            throw new AlreadyRegisteredException("Cannot register '" + atEvent + "' as is registered as a command.");
        }
        registeredEvents.put(atEvent, function);
    }

    @Override
    public void registerCommand(String atCmd, Function<ATCommand, ATResponse> commandHandler) throws AlreadyRegisteredException {
        if (registeredCommands.containsKey(atCmd)) {
            throw new AlreadyRegisteredException("Cannot register '" + atCmd + "' two times.");
        }
        if (registeredEvents.containsKey(atCmd)) {
            throw new AlreadyRegisteredException("Cannot register '" + atCmd + "' as is registered as an event.");
        }
        registeredCommands.put(atCmd, commandHandler);
    }

    @Override
    public void unregisterEvent(String atEvent) {
        registeredEvents.remove(atEvent);
    }

    @Override
    public void unregisterCommand(String atCmd) {
        registeredCommands.remove(atCmd);
    }

    @Override
    public void process(String line) {
        Result parserResult = atParser.process(line);
        LineType type = parserResult.getType();
        if (type == LineType.UNSOLICITED_RESPONSE) {
            handleUnsolicitedResponse(parserResult);
        } else if (type == LineType.COMMANDS) {
            handleCommands(parserResult);
        } else if (type == LineType.ERROR) {
            logger.warn("Received the malformed string '{}' (Error = '{}'). It is discarded.", line, parserResult.getErrorMsg());
        } else if (type == LineType.PARTIAL_RESPONSE) {
            ATEvent evt = ATEvent.event(parserResult.getResponseName(), parserResult.getResponseParameters());
            if (partialResponses == null) {
                partialResponses = new ArrayList<>();
            }
            partialResponses.add(evt);
        } else if (type == LineType.BODY_LINE) {
            if (body == null) {
                body = new StringBuilder();
            } else {
                body.append('\n');
            }
            body.append(parserResult.getBody());
        } else if (type == LineType.COMPLETE_RESPONSE) {
            if (commandFuture == null) {
                logger.error("No command future when a complete response is received.");
                return;
            }
            ATResponse response;
            if (parserResult.isCompleteResponseOk()) {
                if (body == null) {
                    response = ATResponse.ok(partialResponses);
                } else {
                    response = ATResponse.ok(body.toString(), partialResponses);
                }
            } else {
                response = ATResponse.error();
            }
            timeoutTask.cancel(true);
            commandFuture.complete(response);
            semaphore.release();
        }
    }

    private void handleUnsolicitedResponse(Result parserResult) {
        String responseName = parserResult.getResponseName();
        List<String> responseParameters = parserResult.getResponseParameters();
        ATEvent event = ATEvent.event(responseName, responseParameters);
        Consumer<ATEvent> consumer = registeredEvents.get(responseName);
        if (consumer != null) {
            consumer.accept(event);
        } else {
            logger.warn("Received an unsolicited message for '{}' but no handler registered", responseName);
        }
    }

    private void handleCommands(Result parserResult) {
        List<ATCommand> commands = parserResult.getCommands();
        if (commands.size() != 1) {
            //TODO: Manage a list of commands
            logger.error("It is currently not supported to handle AT strings with various commands");
            return;
        }
        ATCommand cmd = commands.get(0);
        String cmdName = cmd.getCommand();
        if (cmdName.equals("")) {
            sendToPeer(ATResponse.ok().toWireString());
        } else {
            ATResponse resp;
            Function<ATCommand, ATResponse> function = registeredCommands.get(cmdName);
            if (function != null) {
                resp = function.apply(cmd);
            } else {
                logger.warn("Received an AT command for '{}' but no handler registered", cmdName);
                resp = ATResponse.error();
            }
            sendToPeer(resp.toWireString());
        }
    }

    private void sendToPeer(String data) {
        logger.info("Send to comm port: \"{}\"", data);
        try {
            outputStream.write(data.getBytes());
        } catch (IOException e) {
            logger.error("Cannot send command response to peer", e);
        }
    }

    @Override
    public void send(ATEvent evt) {
        sendToPeer(evt.asWireString() + '\r');
    }

    @Override
    public synchronized CompletableFuture<ATResponse> send(ATCommand cmd, long timeout, TimeUnit unit) {
        try {
            if (semaphore.tryAcquire(timeout, TimeUnit.SECONDS)) {
                if (commandFuture != null && !commandFuture.isDone()) {
                    logger.error("Unexpected situation: AT manager is busy processing another command");
                    semaphore.release();
                    ATResponse response =
                            ATResponse.error("Unexpected situation: AT manager is busy processing another command");
                    return CompletableFuture.completedFuture(response);
                }
                commandFuture = new CompletableFuture<>();
                partialResponses = null;
                body = null;
                atParser.setResponseMode(cmd.getCommand());
                timeoutTask = timemoutExecutor.schedule(this::commandTimeout, timeout, unit);
                sendToPeer("AT" + cmd.asWireString() + '\r');

                return commandFuture;
            } else {
                return CompletableFuture.completedFuture(ATResponse.error("AT command timeout"));
            }
        } catch (InterruptedException e) {
            logger.error("AT Manager thread interrupted");
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(ATResponse.error("Interrupted thread"));
        }
    }

    private void commandTimeout() {
        atParser.resetMode();
        commandFuture.complete(ATResponse.error("AT command timeout"));
        semaphore.release();
    }

}
