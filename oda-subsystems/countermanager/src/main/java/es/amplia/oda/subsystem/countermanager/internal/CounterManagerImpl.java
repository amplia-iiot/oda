package es.amplia.oda.subsystem.countermanager.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.amplia.oda.core.commons.countermanager.CounterManager;

public class CounterManagerImpl implements CounterManager {
	
	static private Logger logger = LoggerFactory.getLogger(CounterManager.class);
    
    private CounterEngine counterEngine;
    private Object monitor = new Object();

    public CounterEngine getCounterEngine() {
        return counterEngine;
    }

    public CounterManagerImpl(CounterEngine counterEngine) {
        this.counterEngine = counterEngine;
    }

	@Override
	public void incrementCounter(String key, int amount, int inputs) {
		if (counterEngine.isEngineEnabled()) {
			synchronized(monitor) {
				if (counterEngine.existsCounter(key))
					counterEngine.incrementCounter(key, amount, inputs);
				else
					counterEngine.addCounter(key, amount);
			}
		}
	}

	@Override
    public void incrementCounter(String key, int amount) {
    	incrementCounter(key,amount,1);
    }
    
    @Override
    public void incrementCounter(String key) {
        incrementCounter(key, 1);        
    }    

}
