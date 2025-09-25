
### SNMP Datastreams

This module allows to read and write data from/to devices using SNMP protocol. It also listens for SNMP traps sent by devices.

To read data from SNMP devices we need to associate to a OID, a deviceId and datastreamId to generate an ODA event with the value stored in that OID. 

#### Dependencies

This module requires the following modules:

* __[ODA Core Commons]({{< ref "<https://amplia-iiot.github.io/oda-docs/infrastructure/core/index.html>" >}})__: Provides the API of the configurable Bundles, Datastreams handling API's and Device API's.
* __[ODA Hardware SNMP]({{< ref "<https://amplia-iiot.github.io/oda-docs/layers/hardware/index.html>" >}})__: Provides the data needed to connect with devices trough SNMP.

#### Configuration

To configure Datastreams SNMP module, a file named _es.amplia.oda.datastreams.snmp.cfg_ must be created.

Each line defines the relation between an OID and a deviceId and datastreamID. It will create a DatastreamGetter for each line.

Format of each line is :

    [OID], [deviceId] = dataType: [dataType], datastream: [datastreamId], feed : [feed]

 * DataType can have the value: 
   * __OID__
   * __INTEGER__
   * __BITSTRING__ 
   * __STRING__ 
   * __GAUGE__
   * __COUNTER32__ 
   * __COUNTER64__ 
   * __TIMETICK__ 
   * __OPAQUE__ 
   * __IP__

_es.amplia.oda.datastreams.snmp.cfg_ will have a similar format to:

```properties
# OID, TIPO, DEVICEID, DATASTREAMID, FEED
1.3.6.1.2.1.1.7.0,testSnmpDeviceV3=dataType : Integer, datastream : snmpInteger, feed : snmp
1.3.6.1.2.1.1.6.0,testSnmpDeviceV3=dataType : String, datastream : snmpString, feed : snmp
1.3.6.1.2.1.1.2.0,testSnmpDeviceV3=dataType : OID, datastream : snmpOID, feed : snmp
1.3.6.1.2.1.1.3.0,testSnmpDeviceV3=dataType : TimeTicks, datastream : snmpTimeTick, feed : snmp
1.3.6.1.2.1.1.5.0,testSnmpDeviceV3=dataType : StringHex, datastream : snmpStringHex, feed : snmp
```
