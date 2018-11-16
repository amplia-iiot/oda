package es.amplia.oda.connector.dnp3;

import com.automatak.dnp3.LogEntry;
import com.automatak.dnp3.LogLevels;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DNP3LogHandlerTest {

    private static final String TEST_ID = "TestId";
    private static final String TEST_LOCATION = "TestLocation";
    private static final String TEST_MESSAGE = "This is a test message";

    @Mock
    private Logger mockedLogger;
    @InjectMocks
    private DNP3LogHandler testLogHandler;


    @Test
    public void logError() {
        LogEntry logEntry = new LogEntry(LogLevels.ERROR, TEST_ID, TEST_LOCATION, TEST_MESSAGE);

        testLogHandler.log(logEntry);

        verify(mockedLogger).error(eq(TEST_MESSAGE));
    }

    @Test
    public void logWarning() {
        LogEntry logEntry = new LogEntry(LogLevels.WARNING, TEST_ID, TEST_LOCATION, TEST_MESSAGE);

        testLogHandler.log(logEntry);

        verify(mockedLogger).warn(eq(TEST_MESSAGE));
    }

    @Test
    public void logInfo() {
        LogEntry logEntry = new LogEntry(LogLevels.INFO, TEST_ID, TEST_LOCATION, TEST_MESSAGE);

        testLogHandler.log(logEntry);

        verify(mockedLogger).info(eq(TEST_MESSAGE));
    }

    @Test
    public void logDebug() {
        LogEntry logEntry = new LogEntry(LogLevels.DEBUG, TEST_ID, TEST_LOCATION, TEST_MESSAGE);

        testLogHandler.log(logEntry);

        verify(mockedLogger).debug(eq(TEST_MESSAGE));
    }
}