package es.amplia.oda.core.commons.countermanager;

public interface CounterManager {

	public void incrementCounter(String key, int amount, int inputs);
    public void incrementCounter(String key, int amount);
    public void incrementCounter(String key);

}
