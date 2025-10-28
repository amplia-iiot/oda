package es.amplia.oda.core.commons.system;

import es.amplia.oda.core.commons.utils.operation.response.*;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.util.*;

@Slf4j
public class RebootEquipment {

    public static void rebootODA(BundleContext context){
        log.info("Reboot Equipment - Restarting all bundles in ODA");
        Bundle[] bundlesArray = context.getBundles();
        for (Bundle bdl : bundlesArray) {
            String bdSymbolicName = bdl.getSymbolicName();
            try {
                bdl.stop();
                log.debug("Bundle {} STOPPED", bdSymbolicName);
            } catch (BundleException e) {
                log.error("Error stopping bundle {}", bdSymbolicName, e);
            }
        }
    }
}
