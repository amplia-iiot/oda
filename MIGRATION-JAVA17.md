# Migración a Java 17 — registro

Rama de trabajo: `feature/java17` (creada desde `master`). Plan completo en 8 fases/tareas.

## Fase 0 — Preparación e infraestructura ✅

Fecha: 2026-07-14. Ejecutado en un entorno de desarrollo (WSL2 / Ubuntu 24.04).

### Hecho
- **Rama `feature/java17`** creada en `oda` y en `adif-oda` (desde `master` / `develop`).
- **Submódulo `oda-externaldependencies` inicializado** (`git submodule update --init --recursive`, commit `a8e92b5`). Contiene los módulos de *crosscompilation*: `deviceio-crosscompilation`, `opendnp3-crosscompilation`, `diozero-crosscompilation`.
- **Toolchain confirmado**: JDK 17 (17.0.19, por defecto) y JDK 11 disponibles; Maven 3.8.7.
- **`~/.m2/settings.xml`** creado con perfil `amplia-nexus` (repos releases/snapshots) — el `pom.xml` solo declara `distributionManagement`, no `<repositories>` de resolución, así que sin este settings no se resuelven los artefactos `es.amplia.oda.*` ni las deps externas.

### Hallazgos clave
1. **Las 3 dependencias nativas ya están publicadas en Nexus** (`net.java.openjdk`, `com.automatak.dnp3`, `com.diozero` → HTTP 200). **Conclusión:** no es necesario recompilar nativo localmente; el build las resuelve desde Nexus. La *crosscompilation* (que requiere `cmake`, `hg`, `gcc/g++`, `make` — ausentes en este entorno) solo hace falta al **regenerar** esas libs en un agente aprovisionado, no para el trabajo de migración Java.
2. **Baseline reproduce el bloqueante nº1 (Lombok).** `mvn -pl oda-core/commons -am clean test-compile` bajo JDK 17 falla con:
   ```
   Fatal error compiling: java.lang.ExceptionInInitializerError:
   Unable to make field ... com.sun.tools.javac.processing.JavacProcessingEnvironment.discoveredProcs accessible:
   module jdk.compiler does not "opens com.sun.tools.javac.processing" to unnamed module
   ```
   Es el fallo canónico de Lombok < 1.18.20 en JDK 16+. **Primera acción de la Fase 1: subir Lombok a 1.18.30+.**
3. La resolución desde Nexus funciona (plugins y deps descargados); el entorno local **puede** construir los módulos Java puros una vez resuelto el bloqueante de Lombok.

### Pendiente / requiere agente de build o decisión de infra
- CI con JDK 17 en paralelo al pipeline JDK 8 (no hay CI en el repo; el release usa `maven-release-plugin`).
- Regeneración de nativas (`oda-externaldependencies`) solo si hay que reconstruir las `.so`: requiere `cmake` + `hg` + toolchain de cross-compilación en un agente dedicado (ARM: legato/owa).

### Cómo reproducir la baseline
```bash
git submodule update --init --recursive        # o ./install-oda-external-dependencies.sh en un agente con toolchain
# settings.xml con el perfil amplia-nexus en ~/.m2
mvn -B -pl oda-core/commons -am clean test-compile   # falla por Lombok bajo JDK 17 → valida el bloqueante
```

## Fase 1 — Toolchain y dependencias ✅

Fecha: 2026-07-15. **Resultado: los 83 módulos del reactor compilan y empaquetan en verde bajo JDK 17** (`mvn clean package -DskipTests`), manteniendo `target 1.8`.

### Cambios
- `pom.xml` (padre): Lombok `1.18.2 → 1.18.36`, compiler `3.8.1 → 3.13.0`, resources `3.1.0 → 3.3.1`, bundle-plugin `3.5.0 → 5.1.9`, assembly `3.1.0 → 3.7.1`, install `2.5.2 → 3.1.2`, deploy `2.8.2 → 3.1.2`, JaCoCo `0.8.1 → 0.8.12`, surefire `2.22.0 → 3.2.5`.
- `oda-demos/pom.xml`: Felix `6.0.1 → 7.0.5`, configadmin `1.9.26`, eventadmin `1.6.4`, fileinstall `3.7.4`, gogo runtime/command/jline `1.1.6/1.1.2/1.1.8`, jansi `1.18`, jline `3.21.0`, felix.log `1.2.6`, logback `1.2.13`, felix.logback `1.0.6`.
- `lombok.config`: `lombok.addGeneratedAnnotation = false` — con `true` Lombok emite `@javax.annotation.Generated`, **eliminado del JDK en Java 11 (JEP 320)** y rompía la compilación de todo módulo con Lombok. Bloqueante que no estaba en el inventario original. (`lombok.addLombokGeneratedAnnotation` se mantiene a `true` para el filtrado de JaCoCo.)

### Desviación respecto al plan
La fase preveía validar «verde en JDK 8» antes del salto; este entorno no tiene JDK 8, así que la validación es «compila y empaqueta bajo JDK 17 con target 8». Los tests siguen aplazados a la Fase 3 (PowerMock no arranca en JDK 17, como estaba previsto).

## Fase 2 — Motor Nashorn ✅

Fecha: 2026-07-15. Opción A del plan (nashorn-core standalone), aplicada en los 3 puntos de resolución del motor (los 2 traductores + `scadatables`, confirmando la sospecha del plan).

### Cambios
- `pom.xml` (padre): propiedad `nashorn.version = 15.4` + `org.openjdk.nashorn:nashorn-core` en dependencyManagement.
- `oda-ruleengine/nashorn`, `oda-operations/nashorn`, `oda-services/scadatables`: dependencia `nashorn-core` + `Embed-Dependency: nashorn-core|asm*` (embebe nashorn-core 15.4 y ASM 7.3.1 en cada bundle, patrón estándar del repo).
- Los 3 call sites: `new ScriptEngineManager().getEngineByName("nashorn")` → `new NashornScriptEngineFactory().getScriptEngine()`. Motivo doble: en JDK 15+ devuelve `null`, y dentro de OSGi el ServiceLoader tampoco vería el bundle.
- Los 9 `config.properties` de demos: añadidos `jdk.dynalink{,.beans,.linker,.linker.support,.support}` a `org.osgi.framework.system.packages.extra` — coinciden exactamente con los `Import-Package` que bnd genera para los bundles con nashorn embebido (verificado contra el manifiesto).

### Validación
- Smoke test standalone bajo JDK 17 con nashorn-core 15.4: `load()`, `Java.type`, acceso global `java.*` e `Invocable.invokeFunction` — OK. Cubre los constructos que usan las reglas JS.
- Los 3 bundles empaquetan con los jars embebidos y manifiesto correcto.
- **Arranque real de la demo `rules` sobre Felix 7.0.5 + JDK 17 — OK**: todos los bundles instalan, resuelven y arrancan; el rule engine carga y evalúa `device.ram.usage.js` (initScript → Nashorn embebido) sin excepciones; parada limpia con `run.sh stop`. Sin errores de `IllegalAccess`/`InaccessibleObjectException` (todavía **sin** `--add-opens`).
- Errores en el log, todos propios del entorno demo, no del salto de JDK: broker MQTT ausente en localhost:1883, `Default configuration is not allowed` en operaciones de reloj/update (comportamiento esperado), permisos de `obtainSerialNumber.sh`, y un `NumberFormatException: Cannot parse null string` en la config de `statemanager.inmemory` (propiedad ausente en el .cfg de la demo; comprobar si ya ocurría en JDK 8).

### Estado / pendiente
- Cambios sin commitear en `feature/java17` (pendiente de revisión).
- Fase 3 (tests, ~170 ficheros PowerMock→Mockito 5) — siguiente gran bloque.
- Fase 4 formal: `<release>17</release>` (aún compila con target 1.8), decisión SecurityManager/dio.policy, validación MQTT/CoAP/SSH con servicios reales.
