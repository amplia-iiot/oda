# CLAUDE.md

Este fichero orienta a Claude Code (claude.ai/code) al trabajar con el código de este repositorio.

## Qué es este repositorio

**ODA (OpenGate Device Agent)** — el agente IoT modular de Amplía Soluciones, construido sobre **OSGi (Apache Felix)**. Recoge datos de dispositivos de campo, los procesa en el edge e intercambia información con la plataforma IoT OpenGate mediante MQTT/CoAP/HTTP/WebSocket/DNP3/IEC-104. Licencia Apache 2.0.

Reactor padre: `es.amplia.oda:oda-parent`, versión `4.16.1-SNAPSHOT`, `packaging=pom`. Este repo **es el framework** en sí; las integraciones aguas abajo (p. ej. `adif-oda`) consumen sus bundles como dependencias Maven desde el Nexus privado de Amplia.

## Comandos de build y test

Requiere **JDK 8**, Maven (sin wrapper) y — para compilar las dependencias externas nativas — Git, **Mercurial** (`hg`) y **CMake**.

Las dependencias externas (`dnp3`, `diozero`) viven en el **submódulo git `oda-externaldependencies`**, que *no* forma parte del reactor padre y está **vacío en un checkout recién clonado**. Instálalas en `~/.m2` antes de compilar o `mvn package` fallará:

```bash
# clonar con submódulos
git clone --recursive git@github.com:amplia-iiot/oda.git

# instalar dependencias externas (helper idempotente — se salta si ya están en ~/.m2)
./install-oda-external-dependencies.sh
# o manualmente:
cd oda-externaldependencies && mvn clean install && cd ..

# compilar + empaquetar todo (ejecuta tests + JaCoCo)
mvn clean package

# solo tests
mvn test

# un módulo / un test concreto
mvn test -pl oda-connectors/mqtt
mvn test -pl oda-connectors/mqtt -Dtest=ActivatorTest
mvn test -pl oda-connectors/mqtt -Dtest=ActivatorTest#nombreDelMetodo

# desplegar a Nexus (flujo de release, maven-release-plugin)
mvn clean deploy
```

El build genera ensamblados demo ejecutables (`.tar.bz2`) en `oda-demos/*/target`. **No hay configuración de CI en el repo** (ni GitHub Actions, ni GitLab CI, ni Jenkinsfile); los releases se hacen con `maven-release-plugin`. Los mensajes de commit usan prefijos JIRA tipo `[ODA-449]`.

## Ejecutar una distribución demo

```bash
tar xjf oda-demos/mqtt/target/oda-mqtt-*.tar.bz2
cd mqtt
bin/run.sh [start|debug|stop|status]   # debug = JDWP en el puerto 8000, suspend=y
```

Consola remota: `ssh oda@localhost -p 50000` (contraseña por defecto `oda`). `run.sh` arranca Java + Apache Felix con `-Djava.security.policy=security/dio.policy` (necesario para el acceso DIO/hardware) y la configuración de Logback.

## Arquitectura

ODA es un conjunto de **bundles OSGi** independientes (`packaging=bundle`) que registran y consumen servicios en el registro de servicios de Felix. El runtime es Felix + ConfigAdmin + EventAdmin + FileInstall + consola Gogo + Logback. Los datos fluyen por estas etapas, cada una un grupo de módulos bajo el reactor padre:

1. **oda-hardware** — abstracción del acceso físico (GPIO, I2C, Modbus, SNMP, OPC-UA, serie/AT, UDP, FTP).
2. **oda-datastreams** — modelan los puntos de dato del dispositivo; exponen `DatastreamsGetter` / `DatastreamsSetter`. (`deviceinfofx30`, `deviceinfoowa450` son específicos de esos equipos.)
3. **oda-statemanagers** — mantienen el estado de los datastreams (`inmemory` usa Apache Derby embebido; `realtime`). Implementan `StateManager`.
4. **oda-subsystems** — `poller` (sondeo periódico), `collector` (recopilación/envío), `sshserver` (consola remota), `countermanager`.
5. **oda-ruleengine** — reglas de edge computing; `api` + `nashorn` (reglas en JavaScript).
6. **oda-dispatchers** — serializan/adaptan los datos al formato de transporte: `opengate` (principal), `scada`. Implementan `Dispatcher` / `ScadaDispatcher`.
7. **oda-connectors** — transporte por protocolo hacia OpenGate: `mqtt`, `coap`, `http`, `websocket`, `dnp3`, `iec104`. Implementan `OpenGateConnector` / `ScadaConnector`.
8. **oda-operations** — atienden órdenes entrantes de OpenGate: `get`, `set`, `refreshinfo`, `update` (firmware), `setclock`, `synchronizeclock`, `rebootequipment`, `localprotocoldiscovery`, `nashorn` (operaciones custom en JS). `api` define las interfaces.
9. **oda-events** — publican eventos de negocio/alarmas (`EventPublisher`, `DatastreamsEvent`).

Grupos de apoyo: **oda-core** (`commons` — la base compartida), **oda-comms** (librerías cliente de bajo nivel MQTT/IEC104/HTTP), **oda-services** (`jsonserializer`, `cborserializer`, `scadatables`, `zipcompress`), **oda-demos** (ensamblados).

### `oda-core/commons` es la base

**Todos los módulos dependen de `es.amplia.oda.core:commons`.** Contiene todas las interfaces de servicio (`interfaces/`: `DatastreamsGetter`, `DatastreamsSetter`, `Dispatcher`, `OpenGateConnector`, `ScadaConnector`, `StateManager`, `Serializer`, `DeviceInfoProvider`, `EventPublisher`, `OperationSender`, `Interceptor`, …) y la capa de utilidades OSGi bajo `commons/osgi/` y `commons/utils/`. Reutiliza estas abstracciones — no inventes unas paralelas.

## Convenciones de código

- **Java 8**, UTF-8. **Lombok** en todo el proyecto (`@Slf4j`, etc.; ver `lombok.config` en la raíz).
- **Sin Declarative Services / Blueprint.** La inyección de dependencias es **OSGi programático clásico**: cada bundle tiene un `Activator implements BundleActivator`. En `start()` instancia proxies + service listeners y llama a `bundleContext.registerService(...)`; `stop()` desregistra/cierra. Referencia: `oda-connectors/mqtt/.../Activator.java`.
- Reutiliza los helpers de `commons` al extender: `OsgiServiceProxy` (+ subclases tipadas como `DispatcherProxy`, `StateManagerProxy`), `ServiceListenerBundle<T>`, `ServiceRegistrationManager`, `ServiceLocator`, `Scheduler`, `ScriptsLoader`, y `ConfigurableBundleImpl` + `ConfigurationUpdateHandler` para la configuración vía ConfigAdmin.
- **Empaquetado de bundles** (patrón repetido, ver `oda-connectors/mqtt/pom.xml`): `packaging=bundle` con `maven-bundle-plugin`; `Bundle-Activator=${bundle.namespace}.Activator`, `Export-Package=!*` (los bundles no exportan nada por defecto), `Private-Package=${bundle.namespace}.*`, las libs de terceros se embeben con `Embed-Dependency`.
- **Testing**: JUnit 4.12 + Mockito 1.10.19 + **PowerMock 1.7.4** + Hamcrest 1.3, nomenclatura `*Test.java`, cobertura con JaCoCo. Logging: API SLF4J, `slf4j-nop` en tests.

## Configuración de runtime

El comportamiento se configura por bundle mediante ficheros `.cfg` de Felix ConfigAdmin (el nombre del fichero es el PID del bundle) bajo `src/main/resources/odaBundlesConfiguration/` de cada ensamblado. Los más habituales:

- `es.amplia.oda.datastreams.deviceinfo.cfg` — `deviceId`
- `es.amplia.oda.connector.mqtt.cfg` — `host` del broker MQTT
- `es.amplia.oda.subsystem.sshserver.cfg` — consola SSH + credenciales
- `es.amplia.oda.subsystem.poller.cfg` — planificación del sondeo
- `es.amplia.oda.dispatcher.opengate.cfg` — despacho

La configuración del runtime Felix vive en `apacheFelixConfiguration/` (`config.properties`, `logback.xml`) y `gogoConsoleConfiguration/`. Ejemplo de descriptor de ensamblado: `oda-demos/mqtt/src/main/assembly/oda.xml` (salida `tar.bz2`, id `oda-mqtt`).

## Trampas conocidas

- **`mvn package` falla sin las dependencias externas** — instala primero `oda-externaldependencies` (ver arriba). El directorio del submódulo está vacío hasta inicializarlo.
- El build necesita el Nexus privado de Amplia (`repository.amplia.es:8081`) para resolver snapshots/releases y desplegar.
- Documentación completa: https://amplia-iiot.github.io/oda-docs/
