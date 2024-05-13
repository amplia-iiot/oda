package es.amplia.oda.subsystem.countermanager.internal;

import java.util.Hashtable;

public class CountReference {
    
    private Hashtable<String, CounterElement> countersInit;
    private Hashtable<String, CounterElement> countersEnd;
    int size;

    public CountReference(Hashtable<String, CounterElement> countersInit, Hashtable<String, CounterElement> countersEnd, int size) {
        super();
        this.countersInit = countersInit;
        this.countersEnd = countersEnd;
        this.size = size;
    }

    public Hashtable<String, CounterElement> getCountersInit() {
        return countersInit;
    }

    public Hashtable<String, CounterElement> getCountersEnd() {
        return countersEnd;
    }

    public int getSize() {
        return size;
    }

}
