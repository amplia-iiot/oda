package es.amplia.oda.subsystem.countermanager.internal;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Vector;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrinterOutputManager {

    private static final Logger loggerCounter = LoggerFactory.getLogger("_COUNTER");
    private static final Logger logger = LoggerFactory.getLogger(PrinterOutputManager.class);

    public static final String COUNTERS_FOLDER_NAME = "conf";
    public static final String COUNTERS_FILE_NAME = "counters.properties";
    
    public static final String COMMON_CONFIG_FOLDER = "common-conf";
    
    public static final String USER_DIR_PROPERTY = "user.dir";
    public static final String COMMON_CONFIG_PROPERTY = "common.config.dir";
    
    private DecimalFormat df;

    private static Properties countersProp;
    
    private Hashtable<String, Integer> categories;
    
    private ArrayList<Pattern> categoryTemplates;
    
    private CounterEngine counterEngine;
    
    private int slotTime;
    
    private String format;
    private int offsetTotal;
    private int offsetRatio;
    private int offsetAcc;    
    private int offsetAvg;
    private int offsetVar;    
    
    private final String NO_APPLY = "N/A";
    
    public void setFormat(String format) {
        this.format = format;
    }

    public void setOffsetTotal(int offsetTotal) {
        this.offsetTotal = offsetTotal;
    }

    public void setOffsetRatio(int offsetRatio) {
        this.offsetRatio = offsetRatio;
    }

    public void setOffsetAcc(int offsetAcc) {
        this.offsetAcc = offsetAcc;
    }    
    
    public void setOffsetAvg(int offsetAvg) {
        this.offsetAvg = offsetAvg;
    }

    public void setOffsetVar(int offsetVar) {
        this.offsetVar = offsetVar;
    }

    public CounterEngine getCounterEngine() {
        return counterEngine;
    }

    public void setCounterEngine(CounterEngine counterEngine) {
        this.counterEngine = counterEngine;
    }

    public void init(int slotTime) {        
        File userDirFile = new File(System.getProperty(USER_DIR_PROPERTY));
        System.setProperty(COMMON_CONFIG_PROPERTY, userDirFile.getParent() + File.separator + COMMON_CONFIG_FOLDER);
        categories = new Hashtable<String, Integer>(1);
        categoryTemplates = new ArrayList<Pattern>();
        this.slotTime = slotTime; 
        df = new DecimalFormat((format==null)?"##0.000000":format);
    }

    public void loadCountersFile() {
        if (logger.isDebugEnabled())
            logger.debug("loading counters file");
        countersProp = getCountersProperties();
        Enumeration<Object> e = countersProp.keys();
        categories.clear();
        categoryTemplates.clear();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            // Por si en el futuro volvemos a poner propiedades a los contadores
            //Integer period = new Integer(Integer.parseInt((String)countersProp.get(key))/slotTime);
            Integer period = new Integer(0);
            Pattern categoryPattern = CountersHelper.generateCategoryPattern(key);
            if (categoryPattern!=null)
            	categoryTemplates.add(categoryPattern);
            else
            	categories.put(key, period);
        }
        if (logger.isTraceEnabled())
            logger.trace("categories loaded: " + categories);

        counterEngine.setCategories(categories, categoryTemplates);
    }
    
    public CountReference getCountReference(Hashtable<Long, Hashtable<String, CounterElement>> counterHist, Queue<Long> timestamps) {
        if (logger.isDebugEnabled())
            logger.debug(" getting count reference");

        Long initTime = null;
        Long endTime = null;
        
        int rest = timestamps.size();
        if (rest < 1) {
            if (logger.isDebugEnabled())
                logger.debug("counter cannot be shown");
            return null;
        } else if (rest == 1) {
            if (logger.isDebugEnabled())
                logger.debug("first sample");
        }

        int i=0;
        Iterator<Long> it = timestamps.iterator();
        while (it.hasNext()) {
            Long value = (Long)it.next();
            if (i == rest-1)
                endTime = value;
            else if (i == 0)
                initTime = value;
            i++;                
        }
        
        Hashtable<String, CounterElement> countersInit = (initTime!=null)?counterHist.get(initTime):new Hashtable<String, CounterElement>();
        Hashtable<String, CounterElement> countersEnd = counterHist.get(endTime);

        return new CountReference(countersInit, countersEnd, rest - 1);
    }
    
    public boolean setReportSlotTime(String key, CountReference countReference) {
        if (logger.isDebugEnabled())
            logger.debug("setting report for category: " + key);


        Vector<CounterElement> initListCount = new Vector<CounterElement>(); 
        Vector<CounterElement> endListCount = new Vector<CounterElement>(); 
        
        // init List
        Enumeration<String> allKeys = countReference.getCountersInit().keys();
        while (allKeys.hasMoreElements()) {
            String onekey = (String)allKeys.nextElement();
            if (CountersHelper.keyMatchesCategory(onekey, key)) {
                CounterElement e = countReference.getCountersInit().get(onekey);
                initListCount.add(e);
            }
        }
        
        // end list
        allKeys = countReference.getCountersEnd().keys();
        while (allKeys.hasMoreElements()) {
            String onekey = (String)allKeys.nextElement();
            if (CountersHelper.keyMatchesCategory(onekey, key)) {
                CounterElement e = countReference.getCountersEnd().get(onekey);
                endListCount.add(e);
            }
        }
        
        if (endListCount.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("counter cannot be shown");
            }
            return false;
        } else {
            CounterElement initTotalCounter = CountersHelper.getTotalCounter(initListCount);
            CounterElement endTotalCounter = CountersHelper.getTotalCounter(endListCount);
            formatResultToShowSlot(key, initTotalCounter, endTotalCounter, countReference.getSize());
            return true;
        }
    }
        
    private void formatResultToShowSlot(String category, CounterElement init, CounterElement end, int size) {

        StringBuffer print = new StringBuffer();
        Long total = new Long(end.getIncs());
        print.append(formatValues(total.toString(),offsetTotal));
        double ratio = (double)(((double)end.getIncs()-(double)init.getIncs())/(slotTime*size));
        print.append(" - rat: ");
        print.append(formatValues(df.format(ratio),offsetRatio));
        print.append(" (ips)");
        String avgStr = null; 
        String stdStr = null; 
        String accStr = null; 
        if (!CountersHelper.isSimple(end)) {
            double avg = (double)CountersHelper.getMean(end, init); 
            avgStr = formatValues(df.format(avg), offsetAvg);
            double sqr = (double)CountersHelper.getStdDev(end, init);
            stdStr = formatValues(df.format(sqr), offsetVar);
            accStr = formatValues(Long.toString(end.getAcumulated()),offsetAcc);
        } else {
            avgStr = formatValues(NO_APPLY, offsetAvg);
            stdStr = formatValues(NO_APPLY, offsetVar);
            accStr = formatValues(NO_APPLY, offsetAcc);
        }
        print.append(" - avg: ");
        print.append(avgStr);
        print.append(" - std: ");
        print.append(stdStr);
        print.append(" - acc: ");
        print.append(accStr);
        print.append(" - ").append(category);
        
        loggerCounter.info(print.toString());
    }
    
    private String formatValues(String val, int fill) {
        if (val.length() <= fill) {
            StringBuffer buff = new StringBuffer();
            for (int i=0;i<fill; i++)
                buff.append(" ");
            buff.append(val);
            return buff.toString().substring(val.length());
        } else {
            return val;
        }
    }
    
    @Deprecated
    public boolean setReport(String key, Hashtable<Long, Hashtable<String, CounterElement>> counterHist, Queue<Long> timestamps) {        
        if (logger.isDebugEnabled()) {
            logger.debug("setting report for category: " + key);
        }
        
        int slots = categories.get(key).intValue();
        if (timestamps.size() >= slots) {
            Iterator<Long> it = timestamps.iterator();
            int rest = timestamps.size() - slots; 
            List<Long> countTotal = new ArrayList<Long>(slots);
            boolean found = false;
            while (it.hasNext()) {
                Long tst = (Long)it.next();
                if (rest > 0) {
                    rest--;
                    continue;
                } else {
                    // sumar cada slot
                    Hashtable<String, CounterElement> counters = counterHist.get(tst);
                    long totalCountPerSlot = 0;
                    Enumeration<String> allKeys = counters.keys();
                    while (allKeys.hasMoreElements()) {
                        String onekey = (String)allKeys.nextElement();
                        if (CountersHelper.keyMatchesCategory(onekey, key)) {
                            CounterElement e = counters.get(onekey);
                            totalCountPerSlot += e.getAcumulated();
                            found = true;
                        }
                    }
                    countTotal.add(new Long(totalCountPerSlot));
                }
            }
            
            if (found) {
                if (logger.isDebugEnabled())
                    logger.debug("result List for " + key + ". " + countTotal);
                formatResultToShow (key, countTotal);
            } else {
                if (logger.isDebugEnabled())
                    logger.debug("category not found: " + key );
            }
            return true;            
        } else {
            if (logger.isDebugEnabled())
                logger.debug("not enough slots yet for " + key + " (" + timestamps.size() + "/" + slots + ")");
            return false;
        }        
    }
    
    @Deprecated
    private void formatResultToShow(String category, List<Long> counters) {
        StringBuffer print = new StringBuffer(category);
        print.append(" => Period: ");
        int period = counters.size() * slotTime;
        print.append(period);
        print.append(" sec, Total: ");
        print.append(counters.get(counters.size()-1).longValue());
        print.append(", Mean: ");        
        Iterator<Long> it = counters.iterator();
        double acc = 0; 
        while (it.hasNext()) {
            acc += it.next().longValue();
        }
        acc = acc/counters.size();
        print.append(df.format(acc));
        print.append(", Ratio: ");
        double l1 = (double)counters.get(counters.size()-1).longValue();
        double l2 = (double)counters.get(0).longValue();   
        double ratio = (l1 - l2)/(double)period;
        print.append(df.format(ratio));
        print.append(" incr/sec");
        
        loggerCounter.info(print.toString());
    }
    
    protected Properties getCountersProperties(){
        Properties properties = new Properties();
        String countersPropPath = File.separator + COUNTERS_FOLDER_NAME + File.separator + COUNTERS_FILE_NAME;
        File file;
        try {
            if ( (file = new File(System.getProperty(USER_DIR_PROPERTY) + countersPropPath)) != null && file.exists() && file.isFile()){
                    properties.load(new FileInputStream(file));
            }else if ( (file = new File(System.getProperty(COMMON_CONFIG_PROPERTY) + countersPropPath)) != null && file.exists() && file.isFile()){
                    properties.load(new FileInputStream(file));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

}
