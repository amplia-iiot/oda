package es.amplia.oda.operation.nashorn;

import es.amplia.oda.operation.api.engine.OperationNotFound;
import es.amplia.oda.operation.api.engine.OperationScriptTranslator;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class OperationEngineNashornTest {

    @Test
    void unknownOperationOnUnconfiguredEngineIsNotFound() {
        OperationEngineNashorn engine = new OperationEngineNashorn(mock(OperationScriptTranslator.class));

        assertThrows(OperationNotFound.class,
                () -> engine.engine("UNKNOWN", "device", "op-1", Collections.emptyMap(), null));
    }

    @Test
    void stopOnUnconfiguredEngineDoesNotFail() {
        new OperationEngineNashorn(mock(OperationScriptTranslator.class)).stop();
    }
}
