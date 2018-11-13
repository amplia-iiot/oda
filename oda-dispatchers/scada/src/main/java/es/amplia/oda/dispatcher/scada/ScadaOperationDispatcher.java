package es.amplia.oda.dispatcher.scada;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.interfaces.ScadaDispatcher;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaInfo;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.DatastreamInfo;

class ScadaOperationDispatcher implements ScadaDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScadaOperationDispatcher.class);

    private final ScadaTableTranslator translator;
    private final OperationGetDeviceParameters getDeviceParameters;
    private final OperationSetDeviceParameters setDeviceParameters;

    ScadaOperationDispatcher(ScadaTableTranslator translator, OperationGetDeviceParameters getDeviceParameters,
                                    OperationSetDeviceParameters setDeviceParameters) {
        this.translator = translator;
        this.getDeviceParameters = getDeviceParameters;
        this.setDeviceParameters = setDeviceParameters;
    }

    @Override
    public <T, S> CompletableFuture<ScadaOperationResult> process(ScadaOperation operation, int index, T value, S type) {
        DatastreamInfo datastreamInfo;
        try {
            datastreamInfo = translator.getDatastreamInfo(new ScadaInfo(index, type, value));
        } catch (DataNotFoundException exception) {
            LOGGER.warn("Can not process SCADA operation: Datastream with SCADA index {} not found.", index);
            return CompletableFuture.completedFuture(ScadaOperationResult.ERROR);
        }

        String deviceId = datastreamInfo.getDeviceId();
        String datastreamId = datastreamInfo.getDatastreamId();

        switch (operation) {
            case SELECT:
            case SELECT_BEFORE_OPERATE:
                return getDeviceParameters.getDeviceParameters(deviceId, Collections.singleton(datastreamId))
                        .thenApply(getResult -> getResult.getValues().stream()
                                .filter(datastreamIdResult -> datastreamIdResult.getDatastreamId().equals(datastreamId))
                                .findFirst()
                                .map(this::toScadaOperationResult)
                                .orElse(ScadaOperationResult.ERROR));
            case DIRECT_OPERATE:
            case DIRECT_OPERATE_NO_ACK:
                OperationSetDeviceParameters.VariableValue variableValue =
                        new OperationSetDeviceParameters.VariableValue(datastreamId, datastreamInfo.getValue());
                return setDeviceParameters.setDeviceParameters(deviceId, Collections.singletonList(variableValue))
                        .thenApply(setResult -> setResult.getVariables().stream()
                                .filter(datastreamIdResult -> datastreamIdResult.getIdentifier().equals(datastreamId))
                                .findFirst()
                                .map(this::toScadaOperationResult)
                                .orElse(ScadaOperationResult.ERROR));
            default:
                return CompletableFuture.completedFuture(ScadaOperationResult.NOT_SUPPORTED);
        }
    }

    private ScadaOperationResult toScadaOperationResult(OperationGetDeviceParameters.GetValue result) {
        switch (result.getStatus()) {
            case OK:
                return ScadaOperationResult.SUCCESS;
            case PROCESSING_ERROR:
            case NOT_FOUND:
            default:
                return ScadaOperationResult.ERROR;
        }
    }

    private ScadaOperationResult toScadaOperationResult(OperationSetDeviceParameters.VariableResult result) {
        return result.getError() == null ? ScadaOperationResult.SUCCESS : ScadaOperationResult.ERROR;
    }
}
