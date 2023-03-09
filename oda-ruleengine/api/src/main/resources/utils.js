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

function setValue(state, deviceId, datastreamId, value) {
    var newValue = state.createValue(deviceId, datastreamId, value);
    state.refreshValue(deviceId, datastreamId, newValue);
    return state;
}

function setValueWithTime(state, deviceId, datastreamId, at, value) {
    var newValue = state.createValue(deviceId, datastreamId, at, value);
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
    return state.createValue(value.getDeviceId(), value.getDatastreamId(), value.getValue() + quantity);
}

function sub(state, value, quantity) {
    return state.createValue(value.getDeviceId(), value.getDatastreamId(), value.getValue() - quantity);
}

function mult(state, value, quantity) {
    return state.createValue(value.getDeviceId(), value.getDatastreamId(), value.getValue() * quantity);
}

function div(state, value, quantity) {
    return state.createValue(value.getDeviceId(), value.getDatastreamId(), value.getValue() / quantity);
}

// OPERATIONS WITH HISTORIC DATA

function average(state, deviceId, datastreamId, alpha) {
    var allValues = state.getAllValues(state.getKey(deviceId, datastreamId));
    var i;
    var sum = 0;
    for(i = allValues.length-alpha; i < allValues.length; i++) {
        sum += allValues[i];
    }
    return sum/alpha;
}

function summation(state, deviceId, datastreamId, alpha) {
    var allValues = state.getAllValues(state.getKey(deviceId, datastreamId));
    var i;
    var sum = 0;
    for(i = allValues.length-alpha; i < allValues.length; i++) {
        sum += allValues[i];
    }
    return sum;
}

function productOfSequence(state, deviceId, datastreamId, alpha) {
    var allValues = state.getAllValues(state.getKey(deviceId, datastreamId));
    var i;
    var mult = 1;
    for(i = allValues.length-alpha; i < allValues.length; i++) {
        mult *= allValues[i];
    }
    return mult;
}

// CONDITIONAL

function conditionalValue(condition, deviceId, datastreamId, valueTrue, valueFalse, state) {
    if(condition) {
        return state.createValue(deviceId, datastreamId, valueTrue);
    } else {
        return state.createValue(deviceId, datastreamId, valueFalse);
    }
}

function exists(state, deviceId, datastream) {
    return state.exists(deviceId, datastream);
}

// SEND IMMEDIATELY

function sendImmediately(state, deviceId, datastreamId) {
    state.sendImmediately(deviceId, datastreamId);
}