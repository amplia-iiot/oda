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

- [x] Infraestructura JUnit 5 + vintage en el pom padre (build verde: commons 121 tests vía vintage).
- [x] Piloto: `oda-operations/set` migrado (3 tests bajo jupiter, verde).
- [ ] Resto de módulos (grind sistemático, por reactor).
- [ ] Retirar vintage-engine + `junit:junit` cuando no quede JUnit 4.
- [ ] (Opcional, aparte) Retirar Whitebox.
