# Plan de Implementación de Tareas Faltantes (Actualizado)

Basado en el análisis del estado real del proyecto frente al `PLAN_ASISTENTE_VIAJES_V2.md`, el sistema ya cuenta con:
- lógica de dominio completa,
- capa de datos completa,
- UI funcional en Swing,
- tests de ViewModels, mappers y persistencia,
- e inyección centralizada mediante `TripsDependencyInjector`.

Lo que queda pendiente es solo una refactorización menor de configuración.

---

## ✅ Fase 1: Tests de la Capa de Presentación (COMPLETADO)
*Se implementó la cobertura de tests para los ViewModels, cumpliendo con un requisito crítico del plan.*

### ✅ 1.1 `HomeViewModelTest.kt`
*   **Estado:** Creado y completado.
*   **Ubicación:** `src/test/kotlin/edu/dyds/trips/presentation/home/HomeViewModelTest.kt`

### ✅ 1.2 `DetailViewModelTest.kt`
*   **Estado:** Creado y completado.
*   **Ubicación:** `src/test/kotlin/edu/dyds/trips/presentation/detail/DetailViewModelTest.kt`

### ✅ 1.3 `TripsViewModelTest.kt`
*   **Estado:** Creado y completado.
*   **Ubicación:** `src/test/kotlin/edu/dyds/trips/presentation/trips/TripsViewModelTest.kt`

---

## ✅ Fase 2: Tests de la Capa de Datos (COMPLETADO)
*Se añadió cobertura para mappers, repositorios y persistencia local.*

### ✅ 2.1 Tests de Mappers
*   **Estado:** Creados y completados.
*   **Archivos:** `CountryMapperTest.kt` y `WeatherMapperTest.kt`.
*   **Ubicación:** `src/test/kotlin/edu/dyds/trips/data/mapper/`

### ✅ 2.2 Tests de Persistencia Local
*   **Estado:** Creados y completados.
*   **Archivos:** `TripsLocalDataSourceTest.kt`, `TripsJsonPersistenceTest.kt` y `TripsRepositoryImplTest.kt`.
*   **Ubicación:** `src/test/kotlin/edu/dyds/trips/data/local/` y `src/test/kotlin/edu/dyds/trips/data/repository/`

---

## ✅ Fase 3: Refactorización de DI (COMPLETADO)

### ✅ 3.1 Extraer `TripsDependencyInjector.kt`
*   **Ubicación:** `src/main/kotlin/edu/dyds/trips/di/TripsDependencyInjector.kt`
*   **Estado:** Implementado.
*   **Acción realizada:** Se centralizó el armado de clientes HTTP, data sources, repositorios, use cases y factories de ViewModels; `Main.kt` quedó reducido al arranque de la app.

---

## ⏳ Fase 4: Refactorización de configuración (PENDIENTE - Prioridad Baja)

### ⏳ 4.1 Extraer `AppConfig.kt`
*   **Ubicación sugerida:** `src/main/kotlin/edu/dyds/trips/config/AppConfig.kt`
*   **Acción pendiente:** Mover constantes de configuración como `REST_COUNTRIES_BASE_URL` y `OPEN_METEO_BASE_URL` a un archivo centralizado.
*   **Estado actual:** Las constantes siguen definidas dentro de los clientes Ktor.

---

## ✅ Estado final resumido

### Completado
- Domain layer
- Data layer
- Presentation layer funcional
- Tests de ViewModels
- Tests de mappers
- Tests de persistencia/repositorios
- `TripsDependencyInjector`

### Pendiente
- `AppConfig.kt` separado
