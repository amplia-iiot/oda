// GETTING VALUE

function getDatastreamValue(state, deviceId, datastreamId) {
    return state.getLastValue(deviceId, datastreamId);
}

function getValue(state, deviceId, datastreamId) {
    return state.getLastValue(deviceId, datastreamId).getValue();
}

// HANDLING DATASTREAM VALUE OBJECT

function getDatastreamIdFromDatastreamValue(datastreamvalue) {
    return datastreamvalue.getDatastreamId();
}

function getDeviceIdFromDatastreamValue(datastreamvalue) {
    return datastreamvalue.getDeviceId();
}

function getValueFromDatastreamValue(datastreamvalue) {
    return datastreamvalue.getValue();
}

// ADDING VALUES

function setValueFromDatastreamValue(state, deviceId, datastreamId, value) {
    state.refreshValue(deviceId, datastreamId, value);
    return state;
}

function setValue(state, deviceId, datastreamId, feed, value) {
    var newValue = state.createValue(deviceId, datastreamId, feed, value);
    state.refreshValue(deviceId, datastreamId, newValue);
    return state;
}

function setValueWithTime(state, deviceId, datastreamId, feed, at, value) {
    var newValue = state.createValue(deviceId, datastreamId, feed, at, value);
    state.refreshValue(deviceId, datastreamId, newValue);
    return state;
}

// FILTERING VALUES

function filterBetween(value, min, max) {
    return value.getValue() >= min && value.getValue() <= max;
}

function filterLessOrEqualsThan(value, max) {
    return value.getValue() <= max;
}

function filterMoreOrEqualsThan(value, min) {
    return value.getValue() >= min;
}

function filterLessThan(value, max) {
     return value.getValue() < max;
}

function filterEqualsThan(value, data) {
    return value.getValue() == data;
}

function filterMoreThan(state, value, min) {
    return value.getValue() > min
}

function filterExpectedValues(state, datastreamValue, expected) {
    return expected.includes(datastreamValue.getValue());
}

// OPERATING VALUES

function sum(state, value, quantity) {
    return state.createValue(value.getDeviceId(), value.getDatastreamId(), value.getFeed(), value.getValue() + quantity);
}

function sub(state, value, quantity) {
    return state.createValue(value.getDeviceId(), value.getDatastreamId(), value.getFeed(), value.getValue() - quantity);
}

function mult(state, value, quantity) {
    return state.createValue(value.getDeviceId(), value.getDatastreamId(), value.getFeed(), value.getValue() * quantity);
}

function div(state, value, quantity) {
    return state.createValue(value.getDeviceId(), value.getDatastreamId(), value.getFeed(), value.getValue() / quantity);
}

// OPERATIONS WITH HISTORIC DATA

function average(state, deviceId, datastreamId, alpha) {
    var allValues = state.getAllValues(deviceId, datastreamId);
    var i;
    var sum = 0;
    for(i = allValues.length-alpha; i < allValues.length; i++) {
        sum += allValues[i].getValue();
    }
    return sum/alpha;
}

function summation(state, deviceId, datastreamId, alpha) {
    var allValues = state.getAllValues(deviceId, datastreamId);
    var i;
    var sum = 0;
    for(i = allValues.length-alpha; i < allValues.length; i++) {
        sum += allValues[i].getValue();
    }
    return sum;
}

function productOfSequence(state, deviceId, datastreamId, alpha) {
    var allValues = state.getAllValues(deviceId, datastreamId);
    var i;
    var mult = 1;
    for(i = allValues.length-alpha; i < allValues.length; i++) {
        mult *= allValues[i].getValue();
    }
    return mult;
}

// CONDITIONAL

function conditionalValue(condition, deviceId, datastreamId, feed, valueTrue, valueFalse, state) {
    if(condition) {
        return state.createValue(deviceId, datastreamId, feed, valueTrue);
    } else {
        return state.createValue(deviceId, datastreamId, feed, valueFalse);
    }
}

function exists(state, deviceId, datastream) {
    return state.exists(deviceId, datastream);
}

// Check if current value is different than the last one stored
function isValueDifferent(State, datastreamId, deviceId, currentValue) {
    if(State.getLastValue(deviceId, datastreamId) != currentValue) {
	    return true;
    } else {
	    return false;
    }
}

// SEND IMMEDIATELY

function sendImmediately(state, deviceId, datastreamId) {
    state.sendImmediately(deviceId, datastreamId);
}

// READ FILE

// reads the content of the file indicated
// the content is returned as a string
function readFile(filePath){
	var pathObj = java.nio.file.Paths.get(filePath);
	var bytesObj = java.nio.file.Files.readAllBytes(pathObj);
	var bytes = Java.from(bytesObj);
	var content = String.fromCharCode.apply(null, bytes);
	return content;
}

// SET VALUE AS ALREADY SENT
function setSent(state, deviceId, datastreamId, datastreamValue) {
	var HashMap = Java.type('java.util.HashMap');
	var valuesAlreadySent = new HashMap();
	valuesAlreadySent.put(datastreamValue.getAt(), true);
	state.setSent(deviceId, datastreamId, valuesAlreadySent);
}
