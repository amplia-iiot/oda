# Functions of Utils.js

Here they are collected all the functions that **utils.js** provide to use in the rules.


## Control

### Getting values

**getDatastreamValue**  
*Function*: Get the last value from the historic of a datastream in DatastreamValue object format.  
*Input*: Actual State of data (state) to use its operations and the metadata (deviceId and datastreamId) of the 
datastream that function have to get last value.  
*Output*: The last value of the specified datastream in DatastreamValue format. 

**getValue**  
*Function*: Obtain from the State the Object Value associated with the datastream id that is passed by parameter.  
*Input*: Actual State of data (state) to use its operations and the metadata (deviceId and datastreamId) of the 
datastream that function have to get last value.  
*Output*: The last value of the specified datastream in Object format with the class of the value.  

### Handling datastream value object

**getDatastreamIdFromDatastreamValue**  
*Function*: Get the datastreamId (String) from a DatastreamValue object.  
*Input*: DatastreamValue object that the function have to get its datastreamId.  
*Output*: Datastream id of input DatastreamValue.  

**getDeviceIdFromDatastreamValue**  
*Function*: Get the deviceId (String) from a DatastreamValue object.  
*Input*: DatastreamValue object that the function have to get its deviceId.  
*Output*: Device id of input DatastreamValue.

**getValueFromDatastreamValue**  
*Function*: Obtain the real value of passed DatastreamValue Object.  
*Input*: DatastreamValue object that the function have to get its value.  
*Output*: Real value of the DatastreamValue.  

### Adding values

**setValueFromDatastreamValue**  
*Function*: Change the last value of a existing datastream, using a DatastreamValue object as input to change the value. 
*Input*: Actual state of the stateManager (state), a String that specified the device id (deviceId), a String that 
specifies the datastream id (datastreamId) and new DatastreamValue for that datastream (value).  
*Output*: Refreshed state with the new value set as last value of the datastream.  

**setValue**  
*Function*: Change the last value of a existing datastream, using a real value in simply format 
(boolean, string, integer, etc.) as input.
*Input*: Actual state of the stateManager (state), a String that specified the device id (deviceId), a String that 
specifies the datastream id (datastreamId) and new value for that datastream (value).  
*Output*: Refreshed state with the new value set as last value of the datastream.  

___

## Filter

**filterBetween**  
*Function*: Filter the value between a minimum and a maximum (both included) and return a true if and only if value is 
between.  
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

## Derived

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
* state: actual state to use its functions to refresh its data.  

*Output*: New Object Value with the new value, valueTrue if condition is true and valueFalse if condition is false.  

**exists**  
*Function*: Check if a datastream is registered in the state.  
*Input*:
* state: Current state of the State Manager.
* datastreamIdRequired: Id of the datastream that we are looking for.

*Output*: Return true if the datastream is in the state and false if not.

**sendImmediately**  
*Function*: Mark the datastream to send immediately when all rules are resolved.  
*Input*:  The device id (String) and the datastream id (String) of the datastream that function have to mark.  
*Output*:  Nothing, only refresh the state data.