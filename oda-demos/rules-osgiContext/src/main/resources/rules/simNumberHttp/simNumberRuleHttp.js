function when(state, datastreamValue, ctx) {
    return true;
}

function then(state, datastreamValue, ctx) {
    state.logInfo("Activated Rule simNumberRule with OSGI for HTTP");

    // retrieve HttpClientFactory bundle
    var factoryBundle = ctx.getBundle("es.amplia.oda.core.commons.http.HttpClientFactory");
    if(factoryBundle == null) {
        state.logInfo("No HTTP Client Factory bundle retrieved from OSGI");
        return state;
    }
	state.logInfo("HTTP Client Factory bundle retrieved from OSGI");
    
    // Create client for doing HTTP GET
    var http = factoryBundle.createHttpClient();
	state.logInfo("HTTP Client created");
	
	var resp = http.post("http://127.0.0.1:54321/operation/start", "{}".getBytes(), "application/json", []);
	state.logInfo("POST status code: " + resp.statusCode + ", with body: " + resp.response);
	var opLocation = resp.headers["Location"];
	
	var opResult = false;
	var attemps = 3;
	
	while (attemps > 0) {
		java.lang.Thread.sleep(5000);
		resp = http.get("http://127.0.0.1:54321/operation/status/" + opLocation, []);
		state.logInfo("GET status code: " + resp.statusCode + ", with body: " + resp.response);
		var opStatus = JSON.parse(resp.response)["status"];
		state.logInfo("Operation: " + opLocation + " with status: " + opStatus);
		if (opStatus === "success") {
			opResult = true;
			break;
		}
		attemps--;
	}
	
	setValue(state, "jj_device", "operation.result", null, opResult);
	//sendImmediately(state, "jj_device", "operation.result");

	return state;
}

