function getDatastreamIdFromValue(value) {
    return value.getDatastreamId();
}

function getDeviceIdFromValue(value) {
    return value.getDeviceId();
}

function getValue(state, datastreamId) {
    return state.getLastValue(datastreamId);
}

function getData(state, datastreamId) {
    return state.getLastValue(datastreamId).getValue();
}

function getDataFromValueObject(value) {
    return value.getValue();
}

function setValue(state, datastreamId, value) {
    state.refreshValue(datastreamId, value);
    return state;
}

function setValueFromObject(state, deviceId, datastreamId, value) {
    var newValue = state.createValue(deviceId, datastreamId, value);
    state.refreshValue(datastreamId, newValue);
    return state;
}

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

function filterExpectedValues(state, value, expected) {
    return expected.includes(value.getValue());
}

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

function conditionalValue(condition, deviceId, datastreamId, valueTrue, valueFalse, state) {
    if(condition) {
        return state.createValue(deviceId, datastreamId, valueTrue);
    } else {
        return state.createValue(deviceId, datastreamId, valueFalse);
    }
}

function exists(state, datastreamIdRequired) {
    return state.exists(datastreamIdRequired);
}

function sendImmediately(datastreamId) {
    state.sendImmediately(datastreamId);
}