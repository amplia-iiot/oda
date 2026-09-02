package es.amplia.oda.subsystem.countermanager.internal;

import java.text.DecimalFormat;
import java.util.Vector;
import java.util.regex.Pattern;

public class CountersHelper {

    private static final DecimalFormat df = new DecimalFormat("###.##");

    /*public static String getRouteInfoCounterString(RouteInfo routeInfo) {
        StringBuffer res = new StringBuffer();
        res.append(routeInfo.getComponent().getApplicationName()).append("/");
        res.append(routeInfo.getComponent().getComponentName()).append("/");
        res.append(routeInfo.getComponent().getInstanceName()).append("/");
        res.append(routeInfo.getTrack());
        return res.toString();
    }*/

    public static double getMean(CounterElement end, CounterElement init) {
        if (end.getIncs() != init.getIncs())
            return (double)(end.getAcumulated() - init.getAcumulated())/(double)(end.getIncs()-init.getIncs());
        else
            return 0;
    }

    public static double getStdDev(CounterElement end, CounterElement init) {
        if (end.getIncs() != init.getIncs()) {
            double average = CountersHelper.getMean(end, init);
            double var = ((double)(end.getSqrtAcumulated()-init.getSqrtAcumulated())/(double)(end.getIncs()-init.getIncs())) - (average*average);
            return Math.sqrt(var);
        } else {
            return 0;
        }
    }

    public static boolean isSimple(CounterElement ce) {
        if (ce.getAcumulated() == ce.getIncs())
            return true;
        return false;
    }

    public static CounterElement getTotalCounter(Vector<CounterElement> counters ) {
        long inc = 0;
        long acc = 0;
        long sqrt = 0;
        for (CounterElement e : counters) {
            inc += e.getIncs();
            acc += e.getAcumulated();
            sqrt += e.getSqrtAcumulated();
        }
        CounterElement e = new CounterElement(inc, acc, sqrt);
        return e;
    }

    public static boolean keyMatchesCategory(String key, String category) {
    	return key.contains(category);
    }

	public static Pattern generateCategoryPattern(String key) {
		if (!key.contains("*"))
			return null;
		StringBuilder regEx = new StringBuilder("(");
    	regEx.append(key.replace(".", "\\.").replace("/","/?").replace("*", "[^/]*"));
    	regEx.append(").*");
		return Pattern.compile(regEx.toString());
	}

}
