package es.amplia.oda.connector.dnp3;

import com.automatak.dnp3.LogEntry;
import com.automatak.dnp3.LogHandler;
import com.automatak.dnp3.LogLevels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DNP3LogHandler implements LogHandler {

    private final Logger logger;

    DNP3LogHandler() {
        logger = LoggerFactory.getLogger(DNP3LogHandler.class);
    }

    DNP3LogHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(LogEntry entry) {
        switch (entry.level) {
            case LogLevels.ERROR:
                logger.error(entry.message);
                break;
            case LogLevels.WARNING:
                logger.warn(entry.message);
                break;
            case LogLevels.INFO:
                logger.info(entry.message);
                break;
            case LogLevels.DEBUG:
            default:
                logger.debug(entry.message);
        }
    }
}
