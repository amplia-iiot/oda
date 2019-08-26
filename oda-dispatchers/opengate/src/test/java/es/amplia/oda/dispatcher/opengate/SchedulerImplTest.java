package es.amplia.oda.dispatcher.opengate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static es.amplia.oda.dispatcher.opengate.SchedulerImpl.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerImplTest {

    private static final long TEST_DELAY = 30;
    private static final long TEST_PERIOD = 6000;

    @Mock
    private ScheduledExecutorService mockedExecutor;
    private SchedulerImpl testScheduler;

    @Spy
    private ArrayList<ScheduledFuture> spiedTasks;
    @Mock
    private Runnable mockedRunnable;
    @Mock
    private ScheduledFuture mockedScheduledFuture;


    @Before
    public void setUp() {
        testScheduler = new SchedulerImpl(mockedExecutor);

        Whitebox.setInternalState(testScheduler, "tasks", spiedTasks);
    }

    @Test
    public void testSchedule() {
        //noinspection unchecked
        when(mockedExecutor.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockedScheduledFuture);

        testScheduler.schedule(mockedRunnable, TEST_DELAY, TEST_PERIOD);

        verify(mockedExecutor).scheduleAtFixedRate(eq(mockedRunnable), eq(TEST_DELAY), eq(TEST_PERIOD), eq(TIME_UNIT));
        verify(spiedTasks).add(eq(mockedScheduledFuture));
    }

    @Test
    public void testClear() {
        spiedTasks.add(mockedScheduledFuture);

        testScheduler.clear();

        verify(mockedScheduledFuture).cancel(eq(false));
        verify(spiedTasks).clear();
    }

    @Test
    public void testClose() throws InterruptedException {
        testScheduler.close();

        verify(mockedExecutor).shutdown();
        verify(mockedExecutor).awaitTermination(eq(STOP_PENDING_OPERATIONS_TIMEOUT), eq(TIME_UNIT));
    }

    @Test
    public void testCloseWithInterruptedExceptionIsCaught() throws InterruptedException {
        when(mockedExecutor.awaitTermination(anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException());

        testScheduler.close();

        verify(mockedExecutor).shutdown();
        verify(mockedExecutor).awaitTermination(eq(STOP_PENDING_OPERATIONS_TIMEOUT), eq(TIME_UNIT));
    }
}