package es.amplia.oda.core.commons.countermanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounterType {
	
	static private final Logger logger = LoggerFactory.getLogger(CounterType.class);
	static private final String COUNTERS_ERROR = "COUNTERS/ERROR";
	
	private String m_name = "";
    private String m_replace = null;
    
    public CounterType(String _name, String _replace){
        if (_name != null) m_name = _name;
        if ( (_replace != null) && (!_replace.equals("")) ) m_replace = _replace;
    }
    
    public String getCounterString(String replace) {
    	try {
	        String res = new String(m_name);
	        if ( (replace != null) && (m_replace != null) )
	            res = res.replaceAll(m_replace, replace);
	        if (logger.isTraceEnabled())
	            logger.trace("counter string retrieved: " + res);
	        return res;
    	} catch (Throwable e) {
    		logger.error("Throwable exception: ", e);
    		return COUNTERS_ERROR;
    	}
    }
}
