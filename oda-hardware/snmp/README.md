
### SNMP Hardware

This module allows to connect trough snmp to the devices defined in its configuration file.

To connect with devices using SNMP we need this data:

* __deviceId__: Id of the device
* __ipAddress__: Ip address of the device 
* __port__ : Port where device is listening for requests
* __listenPort__ : Port where device will send traps from
* __version__: Version of the SNMP protocol used

Depending on the version of SNMP used, the data needed its different:

* For SNMP version 1 and 2 we need to indicate the name of the community
* For SNMP version 3 we need to indicate the security data:

  * __contextName__: context name
  * __securityName__: username
  * __authPassphrase__: authentication passphrase
  * __privPassphrase__: privacy passphrase
  * __authProtocol__: protocol used for authentication. Possible values are : 
    * __MD5__ 
    * __SHA__
  * __privProtocol__: privacy protocol used. Possible values are : 
    * __DES__
    * __3DES__
    * __AES128__
    * __AES192__
    * __AES256__

#### Dependencies

This module requires the following modules:

* __[ODA Core Commons]({{< ref "<https://amplia-iiot.github.io/oda-docs/infrastructure/core/index.html>" >}})__: Provides the API of the configurable Bundles, Datastreams handling API's and Device API's.

#### Configuration

To configure Hardware SNMP module, a file named _es.amplia.oda.hardware.snmp.cfg_ must be created.

Each line defines a connection with a device.

Format of each line is :
* For SnmpV1 and SnmpV2 :
  * [deviceId] = ip: [ipAddress], port: [port], listenPort: [listenPort], version: [protocolVersion], community: [community]
* For SnmpV3 :
  * [deviceId] = ip: [ipAddress], port: [port], listenPort: [listenPort], version: [protocolVersion], 
  contextName: [contextName], securityName: [securityName], authPassphrase: [authPassphrase],
  privPassphrase: [privPassphrase], authProtocol: [authProtocol], privProtocol: [privProtocol]


_es.amplia.oda.hardware.snmp.cfg_ will have a similar format to:

```properties
# deviceId = ip, port, listenPort, version
# for v1 and v2 we must also indicate community
# for v3 we must also indicate contextName, securityName, authPassphrase, privPassphrase, authProtocol, privProtocol;
testSnmpDeviceV1 = ip:127.0.0.1, port:1160, listenPort:1161, version:1, community:public
testSnmpDeviceV2 = ip:127.0.0.1, port:1160, listenPort:1161, version:2, community:public
testSnmpDeviceV3 = ip:127.0.0.1, port:1160, listenPort:1161, version:3, contextName: public, securityName:simulator, authPassphrase:auctoritas, privPassphrase:privatus, authProtocol: MD5, privProtocol:DES
```
