package es.amplia.oda.hardware.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		I2CReader.doYourThing();
//		I2CReader.doWhateverThatWorks();
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
//donut
	}
}
