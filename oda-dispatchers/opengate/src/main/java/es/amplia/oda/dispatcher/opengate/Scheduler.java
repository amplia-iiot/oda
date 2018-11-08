package es.amplia.oda.dispatcher.opengate;

import java.util.Set;

interface Scheduler {
    void runFor(Set<String> ids);
}
