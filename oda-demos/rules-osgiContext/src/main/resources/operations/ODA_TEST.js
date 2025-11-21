function execute(deviceId, operationId, params, ctx) {
	//var resp = new Response(operationId, deviceId, null, "ODA_TEST", es.amplia.oda.core.commons.utils.operation.response.OperationResultCode.SUCCESSFULL, "Prueba de Operación de ODA", null);
	ctx.logInfo("Params received: {}", params);
	
	var step1 = {};
	step1.name = "ODA_TEST_STEP";
	step1.result = "SKIPPED";
	step1.description = "Descripción del paso de prueba";
	
	var step2 = {};
	step2.name = "ODA_TEST_STEP_NUEVO";
	step2.result = "SUCCESSFUL";
	step2.description = "Nueva descripción del paso de prueba";
	
	var resp = {};
	resp.id = operationId;
	resp.deviceId = deviceId;
    resp.path = null;
    resp.name = "ODA_TEST";
    resp.resultCode = "SUCCESSFUL";
    resp.resultDescription = "Prueba de Operación de ODA";
    resp.steps = null;
	
	ctx.sendSteps(deviceId, null /*path*/, operationId, [step1]);
	java.lang.Thread.sleep(5000);
	ctx.sendSteps(deviceId, null /*path*/, operationId, [step2]);
	java.lang.Thread.sleep(5000);
	//ctx.sendResponse(resp);
	
    return resp;
}
