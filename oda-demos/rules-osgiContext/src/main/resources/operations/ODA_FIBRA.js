function execute(deviceId, operationId, params, ctx) {
	ctx.logInfo("Params received: {}", params);
	
	// retrieve HttpClientFactory bundle
    var factoryBundle = ctx.getBundle("es.amplia.oda.core.commons.http.HttpClientFactory");
    if(factoryBundle == null) {
        ctx.logInfo("No HTTP Client Factory bundle retrieved from OSGI");
        return null;
    }
    
    // Create client for doing HTTP GET
    var http = factoryBundle.createHttpClient();
	
	var ip = params.get("address");
	var port = params.get("port");
	
	var http_resp = http.post("http://" + ip + ":" + port + "/operation/start", "{}".getBytes(), "application/json", []);
	ctx.logInfo("POST status code: " + http_resp.statusCode + ", with body: " + http_resp.response);
	var opLocation = http_resp.headers["Location"];
	
	var step1 = {};
	step1.name = "OPERATION_HTTP_STARTED";
	step1.result = "SUCCESSFUL";
	step1.description = "Operation started with id: " + opLocation;
	
	ctx.sendSteps(deviceId, null /*path*/, operationId, [step1]);
	
	var resp = {};
	var step2 = {};
	var opResult = false;
	resp.resultCode = "ERROR_PROCESSING";
	step2.result = "ERROR";
	var attemps = 3;
	var opStatus = "";
	
	while (attemps > 0) {
		java.lang.Thread.sleep(5000);
		http_resp = http.get("http://" + ip + ":" + port + "/operation/status/" + opLocation, []);
		ctx.logInfo("GET status code: " + http_resp.statusCode + ", with body: " + http_resp.response);
		opStatus = JSON.parse(http_resp.response)["status"];
		ctx.logInfo("Operation: " + opLocation + " with status: " + opStatus);
		if (opStatus === "success") {
			opResult = true;
			step2.result = "SUCCESSFUL";
			resp.resultCode = "SUCCESSFUL";
			break;
		}
		attemps--;
	}
	
	// Recolección del datastream
	var event1 = {};
	event1.datastreamId = "operation.result";
	event1.deviceId = deviceId;
	event1.path = null;
	event1.feed = null;
	event1.at = null;
	event1.value = opResult;
	
	ctx.collect([event1]);
	
	step2.name = "OPERATION_HTTP_RESULT";
	step2.description = "Operation result retreived after " + attemps + " attemps: " + opStatus;
	
	resp.id = operationId;
	resp.deviceId = deviceId;
    resp.path = null;
    resp.name = "ODA_FIBRA";
    resp.resultDescription = "Prueba de Operación de ODA";
    resp.steps = [step2];
	
	//ctx.sendResponse(resp);
	
    return resp;
}
