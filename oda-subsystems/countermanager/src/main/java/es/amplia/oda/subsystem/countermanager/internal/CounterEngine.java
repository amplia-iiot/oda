package es.amplia.oda.subsystem.countermanager.internal;

//import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounterEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(CounterEngine.class);

    public static final String THREAD_NAME = "COUNTERS";
    
    private int slotTime;
    private int storeLimit;
    private int initialSize;
    private String enable;
    private boolean isEngineEnabled = false;
    
    private SlotStoreTask slotStoreTask;
    
    private Timer timer;
    private Hashtable<String, CounterElement> counterStorer;
    private Hashtable<String, Integer> categoriesFixed;
    private Hashtable<String, Integer> categories;
    private ArrayList<Pattern> categoryTemplates; 
        
    // Historic management
    private Hashtable<Long, Hashtable<String, CounterElement>> counterHist;
    private Queue<Long> timestamps;
    
    private PrinterOutputManager printerOutputManager;

	
    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;        
    }    
    
    public int getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(int slotTime) {
        this.slotTime = slotTime;
    }

    public int getStoreLimit() {
        return storeLimit;
    }

    public void setStoreLimit(int storeLimit) {
        this.storeLimit = storeLimit;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public PrinterOutputManager getPrinterOutputManager() {
        return printerOutputManager;
    }

    public void setPrinterOutputManager(PrinterOutputManager printerOutputManager) {
        this.printerOutputManager = printerOutputManager;
    }

    public void init() {
        if (enable.equals("true"))
            isEngineEnabled = true;
        else
            isEngineEnabled = false;
        if (isEngineEnabled) {
            logger.info("initiating counter engine ...");
            
            counterStorer = new Hashtable<String, CounterElement>(initialSize);
            counterHist = new Hashtable<Long, Hashtable<String, CounterElement>>(storeLimit);
            timestamps = new ArrayBlockingQueue<Long>(storeLimit);                
            
            timer = new Timer();
            slotStoreTask = new SlotStoreTask();
            timer.scheduleAtFixedRate(slotStoreTask, (slotTime*1000 - System.currentTimeMillis()%(slotTime*1000)), slotTime*1000);
            printerOutputManager.init(slotTime);
            printerOutputManager.loadCountersFile();
            
            logger.info("Counter Engine initiated");
        } else {
            logger.info("Not init. Counter Engine disabled");
        }
    }

	public void terminate() {
        if (isEngineEnabled) {
            logger.info("terminating counter engine ...");
    
            timer.purge();
            timer.cancel();
            
            logger.info("Counter Engine terminated");
        } else {
            logger.info("Not terminated. Counter Engine disabled");
        }
    }    
    
    public boolean existsCounter(String key) {
        if (isEngineEnabled)
            return counterStorer.containsKey(key);
        else
            return false;
    }

	public void addCounter(String key, int initAmount, int inputs) {
		if (isEngineEnabled) {
			if (logger.isDebugEnabled())
				logger.debug("new counter: " + key);
			CounterElement e = new CounterElement();
			e.increment(key, initAmount,inputs);
			counterStorer.put(key, e);
		}
	}

	public void addCounter(String key, int initAmount) {
		addCounter(key,initAmount,1);
    }

	public void incrementCounter(String key, int amount, int inputs) {
		if (isEngineEnabled) {
			if (logger.isDebugEnabled())
				logger.debug("increment counter " + key + " in " + amount);
			CounterElement element = counterStorer.get(key);
			element.increment(key, amount, inputs);
			counterStorer.put(key, element);
		}
	}

    public void incrementCounter(String key, int amount) {
		incrementCounter(key,amount,1);
    }
    

    public void setCategories(Hashtable<String, Integer> categories, ArrayList<Pattern> categoryTemplate) {
        if (isEngineEnabled) {
//            Enumeration<String> catkeys = categories.keys();
//            while (catkeys.hasMoreElements()) {
//                String category = (String)catkeys.nextElement();
//                int val = categories.get(category).intValue(); 
//                if (val <= 0) {
//                    logger.error("timing error in, at least, one categrory: " + category);
//                    return;
//                }
//            }
            this.categoriesFixed = categories;
            this.categories = (Hashtable<String, Integer>)categories.clone();
            this.categoryTemplates = (ArrayList<Pattern>) categoryTemplate.clone();
        }
    }    
    
    private class SlotStoreTask extends TimerTask {
                
        @Override
        public void run() {
            
            Thread.currentThread().setName(THREAD_NAME);
            
            if (logger.isDebugEnabled())
                logger.debug("slot timout. Saving counters ... ");
            Date now = new Date();
            Long nowLong = new Long(now.getTime());
            if (!timestamps.offer(nowLong)) {
                if (logger.isTraceEnabled())
                    logger.trace("queue full");
                Long delete = timestamps.poll();
                if (delete != null) {
                    counterHist.remove(delete);
                    timestamps.offer(nowLong);
                } else {
                    logger.warn("head null!!. Queue growing up");                    
                }
            }

            Hashtable<String, CounterElement> nowCounterStore = cloneCounterStore();
			counterHist.put(nowLong, nowCounterStore);
            if (logger.isDebugEnabled()) 
                logger.debug("slot timout. Counters saved: " + now.getTime() + " | " + timestamps);
            
            // Categories Report
            if ( (categoriesFixed == null) || (categories == null) ) {
                logger.warn("no report");
                return;
            }

            // normalizamos para evitar desbordamientos
            //normalizeCounters();
            
            expandCategoryTemplates(nowCounterStore.keys());
            
            Enumeration<String> catkeys = categories.keys();
            CountReference countReference = printerOutputManager.getCountReference(counterHist, timestamps);
            if (countReference != null) {            
                while (catkeys.hasMoreElements()) {
                    String category = (String)catkeys.nextElement();
                    
                    // En cada timeslot se intenta sacar la informaci'on
                    printerOutputManager.setReportSlotTime(category, countReference);                                
                    
                    // Solo se intenta sacar mostras contadores cuando corresponde el periodo por categor'ia 
//                    int ref = categoriesFixed.get(category).intValue();
//                    int val = categories.get(category).intValue();
//                    if (val == ref) {
//                        if (printerOutputManager.setReport(category, counterHist, timestamps))
//                            val = val-2;
//                    } else if (val == 0) {
//                        val = ref;
//                    } else {
//                        val--;
//                    }                
//                    categories.put(category, new Integer(val));                
                }
            } else {
                if (logger.isDebugEnabled())
                    logger.debug("count reference is null");
            }           
        }

		private void expandCategoryTemplates(Enumeration<String> counterKeys) {
			while (counterKeys.hasMoreElements()) {
				String counterKey = counterKeys.nextElement();
				for (Pattern template : categoryTemplates) {
					Matcher matcher = template.matcher(counterKey);
					if (matcher.matches())
						if (!categories.containsKey(matcher.group(1)))
							categories.put(matcher.group(1), new Integer(0));
				}
			}
		}
    }

    public Hashtable<String, CounterElement> getCounterStorer() {
        return counterStorer;
    }
    
    public boolean isEngineEnabled() {
        return isEngineEnabled;
    }

    public void resetCounters() throws Exception {
        counterStorer.clear();
        counterHist.clear();
        timestamps.clear();
    }

    private Hashtable<String, CounterElement> cloneCounterStore() {
        return cloneCounterStore(counterStorer);
    }    
    
    private Hashtable<String, CounterElement> cloneCounterStore(Hashtable<String, CounterElement> cloneable) {
        Hashtable<String, CounterElement> clone = new Hashtable<String, CounterElement>(cloneable.size());
        Enumeration<String> e = cloneable.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            CounterElement stored = (CounterElement)cloneable.get(key);
            CounterElement cloned = new CounterElement(stored);
            clone.put(key, cloned);
        }
        return clone;
    }
    
    private void normalizeCounters() {
        Long referenteTime = timestamps.peek();
        Hashtable<String, CounterElement> refStore = cloneCounterStore(counterHist.get(referenteTime));
        
        Iterator<Long> it = timestamps.iterator();
        while (it.hasNext()) {
            Long timestamp = it.next();
            Hashtable<String, CounterElement> store = counterHist.get(timestamp);
            Enumeration<String> e = store.keys();
            while(e.hasMoreElements()) {
                String counter = e.nextElement();
                CounterElement elemCur = store.get(counter);
                CounterElement elemRef = refStore.get(counter);
                if (elemRef !=null) {
                    elemCur.setIncs(elemCur.getIncs() - elemRef.getIncs());
                    elemCur.setAcumulated(elemCur.getAcumulated() - elemRef.getAcumulated());
                    elemCur.setSqrtAcumulated(elemCur.getSqrtAcumulated() - elemRef.getSqrtAcumulated());
                }
                
            }
        }
    }    

}
