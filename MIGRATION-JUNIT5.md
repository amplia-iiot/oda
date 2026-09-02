# Migración JUnit 4 → JUnit 5

Rama: `feature/junit5` (desde `feature/simplification`). Es el ítem "el mayor esfuerzo del plan"
de la Fase 5: migración incremental, módulo a módulo, con el build siempre verde.

## Estrategia: coexistencia con vintage-engine

El pom padre ya declara (heredado por todos los módulos, scope test):

- `org.junit.jupiter:junit-jupiter` — API + engine JUnit 5.
- `org.junit.vintage:junit-vintage-engine` — ejecuta los tests JUnit 4 **aún no migrados**.
- `org.mockito:mockito-junit-jupiter` — `@ExtendWith(MockitoExtension.class)`.

Versión: `junit.jupiter.version=5.10.5` (BOM importado en dependencyManagement). Surefire 3.2.5
corre ambos engines. Mientras vintage esté, JUnit 4 y 5 conviven y el build no se rompe.
**Cuando no quede ningún test JUnit 4, retirar `junit-vintage-engine` y las deps `junit:junit`.**

## Receta por fichero de test

| JUnit 4 | JUnit 5 |
|---|---|
| `import org.junit.Test;` | `import org.junit.jupiter.api.Test;` |
| `import org.junit.Before/After;` | `import org.junit.jupiter.api.BeforeEach/AfterEach;` (y `@Before`→`@BeforeEach`, etc.) |
| `import static org.junit.Assert.*;` | `import static org.junit.jupiter.api.Assertions.*;` |
| `@RunWith(MockitoJUnitRunner.Silent.class)` | `@ExtendWith(MockitoExtension.class)` + `@MockitoSettings(strictness = Strictness.LENIENT)` |
| `@Test(expected = X.class)` | cuerpo con `assertThrows(X.class, () -> ...)` (manual) |
| `@Test(timeout = N)` | `@Timeout(...)` o `assertTimeout(...)` |
| `@Ignore` | `@Disabled` |

**Cuidado — no mecánico:**
- **`assertEquals(mensaje, esperado, actual)`** (3 args, mensaje primero) → en JUnit 5 el mensaje va
  **el último**: `assertEquals(esperado, actual, mensaje)`. Los `assertEquals(esperado, actual)` de 2
  args NO cambian. Revisar caso a caso los que tengan un `String` como primer argumento.
- **`@Test(expected=)`** → hay que envolver la llamada que lanza en `assertThrows` (no es sustitución directa).
- **`Whitebox` (`powermock-reflect`)** se queda: funciona bajo JUnit 5 (es solo reflexión). Su retirada
  es un refactor aparte (830 usos en 127 ficheros).

## Inventario (rama feature/junit5, tras los borrados de Fase 1-2-4)

- ~296 ficheros de test; 295 con `import org.junit.Test`.
- 255 `@RunWith(MockitoJUnitRunner.Silent.class)` (uniforme).
- ~220 `@Test(expected=...)` → `assertThrows` (lo más laborioso).
- ~78 posibles `assertEquals` con mensaje (revisar).
- 11 `@Test(timeout=)`, 1 `@Ignore`, 0 `@Rule`.

## Progreso

- [x] Infraestructura JUnit 5 + vintage en el pom padre.
- [x] Piloto: `oda-operations/set` migrado (3 tests bajo jupiter, verde en aislado).
- [x] Bloqueo JaCoCo-offline + Mockito inline resuelto (ver sección abajo). Reactor completo verde.
- [x] **TODOS los módulos migrados** (~293 ficheros, 41 módulos). Reactor completo verde bajo Jupiter.
- [x] Retirados `junit-vintage-engine` y `junit:junit`; ya no queda ningún test JUnit 4. hamcrest-core pasa a dep de test global (antes llegaba transitivo vía junit).
- [ ] (Opcional, aparte) Retirar Whitebox (830 usos vía `powermock-reflect`; refactor independiente).

## Migración masiva — cómo se hizo (sep 2026)

Ejecutada con 2 herramientas + validación `mvn clean test -pl <módulo> -o` por módulo:
1. **Mecánico** (imports y anotaciones uniformes): `import org.junit.*`→jupiter; `@Before/@After/@BeforeClass/@AfterClass`→`@BeforeEach/@AfterEach/@BeforeAll/@AfterAll`; `@Ignore`→`@Disabled`; `org.junit.Assert`→`Assertions`; `assertThat`→`org.hamcrest.MatcherAssert.assertThat`; `@RunWith(MockitoJUnitRunner.Silent.class)`→`@ExtendWith(MockitoExtension.class)`+`@MockitoSettings(strictness = Strictness.LENIENT)`.
2. **assertThrows**: `@Test(expected = X.class)`→`@Test` + envolver la sentencia que lanza en `assertThrows(X.class, () -> ...)`.
3. **Manual** (lo caza el compilador o el test): asserts con mensaje reordenados (`assertEquals(msg, exp, act)`→`assertEquals(exp, act, msg)`, `assertTrue(msg, cond)`→`assertTrue(cond, msg)`); `@Test(timeout=N)`→`@Timeout(value=N, unit=TimeUnit.MILLISECONDS)`.

**Gotchas encontrados** (por si hay que repetir en adif-oda):
- `@Test(expected=)` cuya última sentencia es `fail(...)`/está en un `finally`/`try-with-resources`: hay que envolver la llamada REAL que lanza, no la última línea textual.
- `@Test (expected = ...)` con espacio antes del paréntesis: no lo detecta un match ingenuo → jupiter `@Test` no admite `expected`, lo caza el compilador.
- `assertTrue/assertFalse(Whitebox.getInternalState(...))`: el retorno genérico resuelve al overload `BooleanSupplier` en vez de `boolean` → `ClassCastException` en runtime; arreglar con cast `(boolean)`.

## ✅ BLOQUEO RESUELTO — JaCoCo offline + Mockito inline (falta de `Offline` en el classpath de test)

**Síntoma:** con la infra de esta rama, `oda-datastreams/modbusslave` fallaba en test (14 errores)
aun con sus tests en JUnit 4 vía vintage:

```
MockitoException: Could not create type
  Caused by: NoClassDefFoundError: Could not initialize class ...ModbusSlaveCounters
    Caused by: NoClassDefFoundError: org/jacoco/agent/rt/internal_xxxx/Offline
```

(`coap` y `nashorn`, citados antes, en realidad ya pasaban: ambos **declaran**
`org.jacoco.agent:runtime` en test. El único que fallaba era modbusslave, que no la declaraba.)

**Causa raíz (verificada reproduciendo el fallo):**
- `ModbusSlaveCounters extends Counters`, y `Counters` vive en **commons**.
- En un build de reactor que para en la fase `test` (`mvn test`, `mvn test -pl X -am`), el goal
  `restore-instrumented-classes` —ligado a `prepare-package`— **no llega a ejecutarse**, así que
  las clases de commons quedan **instrumentadas offline** en `target/classes` y los módulos
  downstream las consumen instrumentadas.
- Una clase instrumentada tiene un `<clinit>` que llama a `$jacocoInit()` → `org.jacoco.agent.rt.*.Offline`.
  El **inline mock maker de Mockito 5** (por defecto) fuerza la inicialización de la clase al mockearla,
  disparando ese `<clinit>`.
- Los módulos con instrumentación offline propia ya llevaban `org.jacoco.agent:runtime` (que aporta
  `Offline`); **modbusslave no** → `NoClassDefFoundError`. No es un choque nuevo con junit-platform:
  es un **hueco de classpath** latente que la infra JUnit 5 (cambio de provider de surefire) hizo aflorar
  de forma consistente.

**Arreglo aplicado:** se generaliza `org.jacoco.agent:runtime` (scope test, classifier `runtime`,
versión gestionada `${jacoco.version}` 0.8.12) como **dependencia de test en el pom padre**, heredada
por todos los módulos. Así `Offline` está siempre en el classpath de test, con instrumentación offline
propia o heredada del reactor. Los módulos que ya la declaraban quedan con una declaración redundante
inocua (dependencyManagement dedupe). Cambio de una sola línea efectiva, scope test → no afecta a los
bundles empaquetados ni a los reportes de cobertura.

**Validado:** `mvn clean test` del reactor completo → **BUILD SUCCESS**, 52 módulos, **1.658 tests,
0 failures, 0 errors** (2 skipped preexistentes), cero `Offline`/`NoClassDefFoundError`.

Con el bloqueo resuelto, la **migración masiva** (~294 ficheros: `@RunWith`→`@ExtendWith`,
`@Test(expected=)`→`assertThrows`, revisión de `assertEquals` con mensaje) puede reaplicarse por
módulos con el build siempre verde. El script mecánico y el filtro están probados (ver historial de la sesión).
