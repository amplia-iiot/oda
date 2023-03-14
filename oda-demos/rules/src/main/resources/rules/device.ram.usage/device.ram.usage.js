function when(state, datastreamValue) {
    return true;
}


function then(State, DatastreamValue) {

	var deviceId = DatastreamValue.getDeviceId();
    var ruleDatastreamId = DatastreamValue.getDatastreamId();
    var datastreamId = "ram.average";

    var allValues = State.getAllValues(deviceId, ruleDatastreamId);
    if (allValues.length >= 5) {
        var returnValue = average(State, deviceId, ruleDatastreamId, 5);

        setValue(State, deviceId, datastreamId, parseFloat(returnValue));

        sendImmediately(State, deviceId, datastreamId);
    }

    return setValue(State, deviceId, ruleDatastreamId, DatastreamValue.getValue());
}

