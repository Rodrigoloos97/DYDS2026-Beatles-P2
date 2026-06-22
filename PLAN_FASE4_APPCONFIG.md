# Plan de Implementación — Fase 4: Refactorización de Configuración

**Objetivo:** Centralizar todas las constantes de configuración (URLs de APIs, rutas de archivos) en un archivo único `AppConfig.kt`, respetando MVVM, Clean Code y SOLID.

---

## 📋 Resumen Ejecutivo

| Aspecto | Descripción |
|---|---|
| **Archivo a crear** | `src/main/kotlin/edu/dyds/trips/config/AppConfig.kt` |
| **Archivos a modificar** | `RestCountriesClient.kt`, `OpenMeteoClient.kt`, `TripsDependencyInjector.kt` |
| **Tests a crear** | `src/test/.../config/AppConfigTest.kt` |
| **Principios a respetar** | SRP, DIP, Open/Closed, Interface Segregation |
| **Etapas** | 6 |

---

## 🗂️ Etapa 1: Crear la interfaz de configuración (SRP + ISP)

**Responsabilidad:** Definir el contrato de configuración sin implementación.

**Archivo:** `src/main/kotlin/edu/dyds/trips/config/AppConfig.kt`

**Lo que incluirá:**
- Interfaz `AppConfig` con propiedades de solo lectura (no métodos de mutación)
- Propiedades requeridas:
  - `restCountriesBaseUrl: String`
  - `openMeteoBaseUrl: String`
  - `tripsCacheFilePath: String`
  - `countriesCacheFilePath: String`

**Principios aplicados:**
- **SRP (Single Responsibility):** `AppConfig` solo expone configuración, no la resuelve.
- **ISP (Interface Segregation):** otros componentes dependen de una interfaz mínima, no de la implementación.
- **DIP (Dependency Inversion):** inversión de dependencias → los clientes reciben `AppConfig`, no lo crean.

---

## 🗂️ Etapa 2: Crear la implementación de configuración (OCP + DIP)

**Responsabilidad:** Implementar `AppConfig` leyendo de variables de entorno con valores por defecto.

**Archivo:** Mismo `AppConfig.kt` (objeto `companion` o clase segregada)

**Lo que incluirá:**
```
- Clase `AppConfigImpl` (o data class con valores)
- Lectura desde `System.getenv()` para cada propiedad
- Valores por defecto seguros (URLs de API públicas, rutas relativas de datos)
- Constructor o factory para testabilidad
```

**Principios aplicados:**
- **OCP (Open/Closed):** la interfaz está cerrada a modificación; la implementación es abierta a extensión (agregar más configs sin cambiar el contrato).
- **DIP:** la inyección de `AppConfig` permite testear con valores ficticios sin tocar variables de entorno.

---

## 🗂️ Etapa 3: Refactorizar `RestCountriesClient.kt`

**Responsabilidad:** Usar `AppConfig` en lugar de gestionar su propia URL.

**Cambios:**
- Parámetro constructor: agregar `appConfig: AppConfig`
- Reemplazar la lógica de `System.getenv()` por lectura desde `appConfig.restCountriesBaseUrl`
- Eliminar el parámetro `apiBaseUrl` con default (mantenerlo como "respeto a atrás compatibilidad" si se usa en tests, pero deprecado)
- Mantener `apiKey` como está (es secreto de runtime, no configuración de app)

**Principios aplicados:**
- **SRP:** `RestCountriesClient` ya no gestiona su propia configuración de URL.
- **DIP:** depende de `AppConfig`, no de variables de entorno directamente.

**Nota:** Los tests de `RestCountriesClient` que usan el constructor directo seguirán funcionando (aceptan `apiBaseUrl`), pero preferir inyectar `AppConfig` en nuevos tests.

---

## 🗂️ Etapa 4: Refactorizar `OpenMeteoClient.kt`

**Responsabilidad:** Mover URL hardcodeada a parámetro de configuración inyectado.

**Cambios:**
- Parámetro constructor: agregar `appConfig: AppConfig`
- Reemplazar hardcoded `"https://api.open-meteo.com/v1/forecast"` por `appConfig.openMeteoBaseUrl`
- Asegurar que la URL se lee en tiempo de construcción (no en cada request)

**Principios aplicados:**
- **SRP:** `OpenMeteoClient` solo ejecuta requests, no gestiona configuración.
- **OCP:** cambiar URL es tan simple como cambiar la variable de entorno (build) o la implementación de `AppConfig` (test).

---

## 🗂️ Etapa 5: Actualizar `TripsDependencyInjector.kt`

**Responsabilidad:** Centralizar la creación de `AppConfig` e inyectarla en clientes y data sources.

**Cambios:**
- Crear propiedad `appConfig: AppConfig` (lazy singleton)
- Instanciar `RestCountriesClient(httpClient, appConfig)`
- Instanciar `OpenMeteoClient(httpClient, appConfig)`
- Usar `appConfig.tripsCacheFilePath` en lugar del string hardcodeado `"app_data/trips_data.json"`
- Usar `appConfig.countriesCacheFilePath` si es aplicable a otros componentes

**Principios aplicados:**
- **DIP:** el injector actúa como orquestador; no expone configuración al exterior, solo inyecta.
- **SRP:** `TripsDependencyInjector` sigue siendo solo el inicializador; `AppConfig` es quien gestiona la configuración.

---

## 🧪 Etapa 6: Tests para `AppConfig.kt`

**Responsabilidad:** Verificar que la configuración se lee correctamente desde entorno y con defaults.

**Archivo:** `src/test/kotlin/edu/dyds/trips/config/AppConfigTest.kt`

**Tests a implementar:**
1. `AppConfig con variables de entorno configura correctamente las URLs`
   - Setear vars. entorno
   - Verificar que `appConfig.restCountriesBaseUrl == expectedUrl`
   - Limpiar vars. de entorno

2. `AppConfig con vars. de entorno ausentes usa defaults`
   - Sin vars. de entorno
   - Verificar que las URLs son las públicas conocidas
   - Verificar que las rutas de archivos son relativas válidas

3. `AppConfig se puede construir con valores inyectados para tests`
   - Usar data class o factory que acepte valores personalizados
   - Constructor alternativo para inyections en tests sin tocar vars. de entorno

4. `RestCountriesClient y OpenMeteoClient usan AppConfig correctamente`
   - Mock de `AppConfig` con URLs ficticias
   - Verificar que los clientes las usan (capturar URLs en requests mock)

**Principios aplicados:**
- **SRP en tests:** cada test verifica un aspecto de `AppConfig`.
- **Disponibilidad en tests:** no requerir configurar vars. de entorno en CI/CD (usar inyección).

---

## 📝 Resumen de archivos a crear/modificar

```
CREAR:
├── src/main/kotlin/edu/dyds/trips/config/AppConfig.kt           ← interfaz + impl
└── src/test/kotlin/edu/dyds/trips/config/AppConfigTest.kt       ← 4 tests

MODIFICAR:
├── src/main/kotlin/.../countries/RestCountriesClient.kt
├── src/main/kotlin/.../weather/OpenMeteoClient.kt
└── src/main/kotlin/.../di/TripsDependencyInjector.kt
```

---

## 📊 Matriz de Principios SOLID

| Principio | Etapa | Aplicación |
|---|---|---|
| **S**RP | 1, 2, 3, 4, 5 | Cada clase gestiona solo su responsabilidad; configuración centralizada en `AppConfig` |
| **O**CP | 2, 5 | `AppConfig` interfaz cerrada; implementación abierta a extensión |
| **L**SP | 1, 6 | `AppConfig` cumple su contrato en tests y producción de igual forma |
| **I**SP | 1 | Interfaz `AppConfig` con solo propiedades necesarias; no contamina con métodos no usados |
| **D**IP | 1, 2, 3, 4, 5, 6 | Inyección de `AppConfig`; clientes no usan `System.getenv()` directamente |

---

## ⚠️ Consideraciones importantes

### Backwards Compatibility
- `RestCountriesClient` seguirá aceptando `apiBaseUrl` en constructor (deprecado pero funcional).
- Esto permite que tests antiguos sigan pasando sin cambios inmediatos.

### Testing
- Crear una implementación "fake" o test-friendly de `AppConfig` con valores conocidos.
- No mockear `AppConfig` con Mockito/mockk si es posible; pasar instancia real con valores inyectados.

### Orden de implementación sugerido
1. Etapas 1 y 2 (crear `AppConfig`)
2. Etapa 6 (tests, para validar `AppConfig`)
3. Etapas 3, 4 (refactorizar clientes; sus tests existentes se adaptan)
4. Etapa 5 (actualizar DI; verifica que todo funciona)

---

## 🎯 Resultado esperado

Después de completar las 6 etapas:

✅ **Cohesión:** Todas las URLs y paths centralizados en un único lugar.
✅ **Testabilidad:** Cambiar configuración es inyectar valores, no vars. de entorno.
✅ **Mantenibilidad:** Un ingreso en `AppConfig`, no búsqueda en 5 archivos.
✅ **SOLID:** Cada componente respeta su responsabilidad.
✅ **Clean Code:** Nombres claros, cambios mínimos en clientes existentes.
✅ **MVVM:** Presentación sigue usando ViewModels; configuración es invisible para ella.

