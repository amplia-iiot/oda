package es.amplia.oda.core.commons.countermanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Counters {

	public interface CounterEnum {

		public CounterType getCounterType();
	}
	
	static private final Logger logger = LoggerFactory.getLogger(Counters.class);
    
    private static CounterManager counterManager;
    
    public void setCounterManager(CounterManager _counterManager) {
        Counters.counterManager = _counterManager;
    }
    
    public static void incrCounter(CounterType counter, String track, int number) {
        incrCounter(counter,track,number,1);
    }
    
    public static void incrCounter(CounterType counter, int number) {
        incrCounter(counter, null, number);
    }

	public static void incrCounter(CounterType counter, String track, int number, int inputs) {

		if (counterManager != null)
			counterManager.incrementCounter(counter.getCounterString(track), number,inputs);
		else
			logger.warn("counterManager is null");
	}
}
