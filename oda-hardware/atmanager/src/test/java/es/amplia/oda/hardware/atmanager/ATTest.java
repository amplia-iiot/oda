package es.amplia.oda.hardware.atmanager;

import es.amplia.oda.hardware.atmanager.api.ATEvent;
import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.hardware.atmanager.api.ATResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ATTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ATTest.class);

    private ATManager atManager;
    private boolean end = false;


    private ATTest() throws ATManager.AlreadyRegisteredException {
        // First, we need to create an ATManager.
        atManager = new ATManagerImpl(new ATParserImpl(), System.out);

        atManager.registerEvent("+BAZ", p -> LOGGER.info("BAZ event received"));

        atManager.registerCommand("+FOO", p -> {
            if (p.getParameters() == null) return ATResponse.error();
            if (p.getParameters().size() != 1) return ATResponse.error();
            atManager.send(ATEvent.event("+FOO", "hi", "fellow", "programmer"));
            return ATResponse.ok();
        });

        atManager.registerCommand("+END", p -> {
            LOGGER.info("Ending process");
            end = true;
            return ATResponse.ok();
        });
    }

    /*
     * Will create an AT server that reads from standard input and writes in
     * standard output.
     */
    public static void main(String[] args) throws ATManager.AlreadyRegisteredException {
        new ATTest().start();
    }

    private void start() {
        LOGGER.info("Ready. Send \"AT+END\" to end program.");
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));

        while (!end) {
            try {
                String line = buffer.readLine();
                atManager.process(line);
            } catch (IOException e) {
                LOGGER.error("Error processing line: {}", e);
            }
        }
    }

}
