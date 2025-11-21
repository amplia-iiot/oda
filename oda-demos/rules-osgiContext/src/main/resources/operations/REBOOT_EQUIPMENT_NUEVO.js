function execute(deviceId, operationId, params, ctx) {
	//var resp = new Response(operationId, deviceId, null, "ODA_TEST", es.amplia.oda.core.commons.utils.operation.response.OperationResultCode.SUCCESSFULL, "Prueba de Operación de ODA", null);
	ctx.logInfo("Params type received: {}", params.get("type"));
	
	var step1 = {};
	step1.name = "REBOOT_EQUIPMENT";
	step1.result = "SUCCESSFUL";
	step1.description = "Descripción del paso de prueba";
	
	var resp = {};
	resp.id = operationId;
	resp.deviceId = deviceId;
    resp.path = null;
    resp.resultCode = "SUCCESSFUL";
    resp.resultDescription = "Prueba de REBOOT de ODA";
    resp.steps = null;
	
	ctx.sendSteps(deviceId, null /*path*/, operationId, [step1]);
	java.lang.Thread.sleep(5000);
	ctx.sendResponse(resp);
	
    return null;
}
