# Plan de Implementación de Tareas Faltantes (Actualizado)

Basado en el análisis de estado del proyecto frente al `PLAN_ASISTENTE_VIAJES_V2.md`, el sistema actual cuenta con la lógica de negocio (Domain) y acceso a datos (Data) completamente implementados, así como una UI en Swing completamente funcional.

A continuación se detalla el estado de las tareas de testing y refactorización.

---

## ✅ Fase 1: Tests de la Capa de Presentación (COMPLETADO)
*Se ha implementado la cobertura de tests para los ViewModels, cumpliendo con un requisito crítico del plan.*

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
*Se ha añadido la cobertura de tests para los mappers y la persistencia local, asegurando la correcta manipulación de datos.*

### ✅ 2.1 Tests de Mappers
*   **Estado:** Creados y completados.
*   **Archivos:** `CountryMapperTest.kt` y `WeatherMapperTest.kt`.
*   **Ubicación:** `src/test/kotlin/edu/dyds/trips/data/mapper/`

### ✅ 2.2 Tests de Persistencia Local
*   **Estado:** Creados y completados.
*   **Archivos:** `TripsLocalDataSourceTest.kt` y `TripsRepositoryImplTest.kt`.
*   **Ubicación:** `src/test/kotlin/edu/dyds/trips/data/local/` y `src/test/kotlin/edu/dyds/trips/data/repository/`

---

##  Fase 3: Refactorización (PENDIENTE - Prioridad Baja)

Estas tareas mejoran la estructura y mantenibilidad del proyecto, alineándolo completamente con el plan original.

### 3.1 Extraer `AppConfig.kt`
*   **Ubicación:** `src/main/kotlin/edu/dyds/trips/config/AppConfig.kt`
*   **Acción:** Mover las constantes como `REST_COUNTRIES_BASE_URL` o `OPEN_METEO_BASE_URL` que actualmente están dentro de los clientes Ktor a este archivo centralizado.

### 3.2 Extraer `TripsDependencyInjector.kt`
*   **Ubicación:** `src/main/kotlin/edu/dyds/trips/di/TripsDependencyInjector.kt`
*   **Acción:** Mover toda la lógica de inicialización de repositorios, casos de uso y ViewModels que actualmente reside dentro de la función `main()` en `Main.kt` a este nuevo objeto `object TripsDependencyInjector`, limpiando así el punto de entrada de la aplicación.
