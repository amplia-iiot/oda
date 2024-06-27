package es.amplia.oda.subsystem.countermanager.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounterElement {

	private static final Logger logger = LoggerFactory.getLogger(CounterElement.class);
    
    private long incs = 0;
    private long acumulated = 0;
    private long sqrtAcumulated = 0;
    
    public CounterElement() {        
    }

    public CounterElement(long incs, long acumulated, long sqrtAcumulated) {
        this.incs = incs;
        this.acumulated = acumulated;
        this.sqrtAcumulated = sqrtAcumulated;
    }    
    
    public CounterElement(CounterElement ce) {
        this.incs = ce.getIncs();
        this.acumulated = ce.getAcumulated();
        this.sqrtAcumulated = ce.getSqrtAcumulated();
    }

    public void increment(String key, long value) {
        increment(key,value,1L);
    }

    public void increment(String key, long value, long inputs){
		incs = incs + inputs;
		acumulated+=value;
		sqrtAcumulated+=(value*value);
		if (acumulated < 0) {
			logger.info("Ignored negative acumulated counter. Key=" + key + ", value=" + acumulated);
		}
		if (sqrtAcumulated <0) {
			logger.info("Ignored negative sqrtAcumulated counter. Key=" + key + ", value=" + sqrtAcumulated);
		}
	}
    
    public long getIncs() {
        return incs;
    }

    public long getAcumulated() {
        return acumulated;
    }
    
    public long getSqrtAcumulated() {
        return sqrtAcumulated;
    }

    public void setIncs(long incs) {
        this.incs = incs;
    }

    public void setAcumulated(long acumulated) {
        this.acumulated = acumulated;
    }

    public void setSqrtAcumulated(long sqrtAcumulated) {
        this.sqrtAcumulated = sqrtAcumulated;
    }

}
