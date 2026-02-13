// oper result codes
var OPER_RESULT_SUCCESS = "SUCCESSFUL";
var OPER_RESULT_ERROR_PARAM = "ERROR_IN_PARAM";
var OPER_RESULT_ERROR_PROCESSING = "ERROR_PROCESSING";
var OPER_RESULT_NOT_SUPPORTED = "NOT_SUPPORTED";

// oper steps result codes
var OPER_STEP_SUCCESS = "SUCCESSFUL";
var OPER_STEP_ERROR = "ERROR";
var OPER_STEP_SKIP = "SKIPPED";
var OPER_STEP_NOT_EXE = "NOT_EXECUTED";

/////////////////////////////////////////////////////////////////

function addStep(stepName, stepResult, stepDescription){
    var operStep = {};
    operStep.name = stepName;
    operStep.result = stepResult;
    operStep.description = stepDescription;
    return operStep;
}

function addStepWithResponse(stepName, stepResult, stepDescription, stepResponse){
    var operStep = {};
    operStep.name = stepName;
    operStep.result = stepResult;
    operStep.description = stepDescription;

    // construct response as java list to avoid type conflicts when parsing the result of the operation
    var ArrayList = Java.type("java.util.ArrayList");
    operStep.response = new ArrayList();
    operStep.response.add(stepResponse);
    return operStep;
}

function createOperResponse(operationId, deviceId, operName, resultCode, resultDescription, steps){
    var operResp = {};
    operResp.id = operationId;
    operResp.deviceId = deviceId;
    operResp.path = null;
    operResp.name = operName;
    operResp.resultCode = resultCode;
    operResp.resultDescription = resultDescription;
    operResp.steps = steps;
    return operResp;
}

function sendOperResponse(operationId, deviceId, operName, resultCode, resultDescription, steps, ctx){
    // create response
    var operResp = createOperResponse(operationId, deviceId, operName, resultCode, resultDescription, steps);

    // send response
    ctx.sendResponse(operResp);
}

function createOdaEvent(deviceId, datastreamId, feed, at, value){
	var event = {};
	event.datastreamId = datastreamId;
	event.deviceId = deviceId;
	event.path = null;
	event.feed = feed;
	event.at = at;
	event.value = value;
	return event;
}