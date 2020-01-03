#Functions of Utils.js

Here they are collected all the functions that **utils.js** provide to use in the rules.


##Control

**getDatastreamIdFromValue**  
*Function*: Get the datastreamId (String) from a Value object.  
*Input*: Value object we want to know the datastream id of.  
*Output*: Datastream id of input value.  

**getDeviceIdFromValue**  
*Function*: Get the deviceId (String) from a Value object.  
*Input*: Value object we want to know the device id of.  
*Output*: Device id of input value.

**getValue**  
*Function*: Obtain from the State the Object Value associated with the datastream id that is passed by parameter.  
*Input*: Actual State of data (state) and datastream id of data we want to take (datastreamId).  
*Output*: Object value with the actual value of required datastream id.

**getData**  
*Function*: Obtain from the State the real value associated with the datastream id that is passed by parameter.  
*Input*: Actual State of data (state) and datastream id of data we want to take (datastreamId).  
*Output*: Real value of required datastream id.  

**getDataFromValueObject**  
*Function*: Obtain the real value of passed Value Object.  
*Input*: Object Value we want to get the real value.  
*Output*: Real value of value.  

**setValue**  
*Function*: Set in the State a value (object Value) for the specified datastream id.  
*Input*: Actual state of the stateManager (state), a String that specified the datastream id (datastreamId) and new value for that datastream (value).  
*Output*: Refreshed state with the new value set for the datastream.  

**setValueFromObject**  
*Function*: Set in the State a real value that will have to be created during the function as Value object to be refreshed like in setValue.  
*Input*: Actual state of the stateManager (state), a String that specified the device id (deviceId), a String that specified the datastream id (datastreamId) and new value for that datastream (value).  
*Output*: Refreshed state with the new value set for the datastream.  

##Filter

**filterBetween**  
*Function*: Filter the value between a minimum and a maximum (both included) and return a true if and only if value is between.  
*Input*:  
* value: Object Value that we are filtering.  
* min: Minimum value that we are expecting from value.  
* max: Maximum value that we are expecting from value.

*Output*: True if and only if value is between min and max, false in another case.

**filterLessOrEqualsThan**  
*Function*: Filter the value under a maximum value (including that value) and return true if is less than max.  
*Input*:  
* value: Object Value that we are filtering.  
* max: Maximum value that we are expecting from value.  

*Output*: True if and only if value is less or equals than max, false in another case.  

**filterMoreOrEqualsThan**  
*Function*: Filter the value over a minimum value (including that value) and return true if is less than min.  
*Input*:  
* value: Object Value that we are filtering.  
* min: Minimum value that we are expecting from value.  

*Output*: True if and only if value is less or equals than min, false in another case.  

**filterLessThan**  
*Function*: Filter the value under a maximum value and return true if is less than max.  
*Input*:  
* value: Object Value that we are filtering.  
* max: Maximum value that we are expecting from value.  

*Output*: True if and only if value is less than max, false in another case.  

**filterEqualsThan**  
*Function*: Filter if the value is the specified and return true if is exactly than data.  
*Input*:  
* value: Object Value that we are filtering.  
* data: Value that we are expecting from value.  

*Output*: True if and only if value is the specified, false in another case.  

**filterMoreThan**  
*Function*: Filter the value over a minimum value and return true if is more than min.  
*Input*:  
* value: Object Value that we are filtering.  
* min: Minimum value that we are expecting from value.  

*Output*: True if and only if value is more than min, false in another case.  

**filterExpectedValues**  
*Function*: Filter if the value exists in a list of values and return true if it exists.  
*Input*:  
* value: Object Value that we are filtering.  
* expected: Array of values that we are expecting from value.  

*Output*: True if and only if value is in expected, false in another case.  

##Derived

**sum**  
*Function*: Add a quantity to the actual value. To create a new Object Value, we need the state, that is the Java object that handle that creation.  
*Input*:
* state: State object that we will use to create the new Object Value.
* value: Object Value that we want to add the quantity.
* quantity: Quantity to add to the value.

*Output*: New Object Value with the real value modified.

**sub**  
*Function*: Subtract a quantity to the actual value. To create a new Object Value, we need the state, that is the Java object that handle that creation.  
*Input*:
* state: State object that we will use to create the new Object Value.
* value: Object Value that we want to subtract the quantity.
* quantity: Quantity to subtract to the value.

*Output*: New Object Value with the real value modified.

**mult**  
*Function*: Multiply a quantity by the actual value. To create a new Object Value, we need the state, that is the Java object that handle that creation.  
*Input*:
* state: State object that we will use to create the new Object Value.
* value: Object Value that we want to multiply by the quantity.
* quantity: Quantity to multiply the value.

*Output*: New Object Value with the real value modified.

**div**  
*Function*: Divide a quantity by the actual value. To create a new Object Value, we need the state, that is the Java object that handle that creation.  
*Input*:
* state: State object that we will use to create the new Object Value.
* value: Object Value that we want to divide by the quantity.
* quantity: Quantity to divide the value.

*Output*: New Object Value with the real value modified.

##Utils

**conditionalValue**  
*Function*: Like a Ternary Operator, return a value if the condition is true and another value if the condition is false.  
*Input*:
* condition: bool value that control what value will be returned in a new Object Value.
* deviceId: String with the deviceId of the Object Value.
* datastreamId: String with the datastreamId of the Object Value.
* valueTrue: value what be set in the created Object Value if condition is true.
* valueFalse: value what be set in the created Object Value if condition is false.
* state: 

*Output*: New Object Value with the new value, valueTrue if condition is true and valueFalse if condition is false.  

**exists**  
*Function*: Check if a datastream is registered in the state.  
*Input*:
* state: Current state of the State Manager.
* datastreamIdRequired: Id of the datastream that we are looking for.

*Output*: Return true if the datastream is in the state and false if not.