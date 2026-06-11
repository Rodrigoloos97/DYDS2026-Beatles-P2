# Plan Resumido — Asistente de Viajes

## Objetivo
Construir una app en Compose Desktop para:
1. Explorar destinos (países).
2. Ver detalle del destino + clima.
3. Guardar y gestionar itinerarios.

APIs obligatorias:
- RestCountries (datos del país)
- Open-Meteo (pronóstico)

---

## Alcance mínimo (consigna)
- 3 pantallas con navegación:
  - `Home` (explorador)
  - `Detail` (país + clima)
  - `Trips` (mis viajes)
- Estados UI obligatorios: `Loading`, `Success`, `Error` y `Empty`.
- Persistencia local para funcionar parcialmente offline.
- Arquitectura Clean + MVVM.
- Tests unitarios en ViewModels, UseCases, Repositorios y DataSources.

---

## Arquitectura (Clean + MVVM)

### Capas
- **Presentation**: Composables + ViewModels.
- **Domain**: entidades, contratos de repositorio, casos de uso.
- **Data**: implementaciones de repositorios, remote/local sources, mappers.
- **DI**: inyección manual con factories singleton.

### Regla clave
`Domain` no depende de `Data` ni `Presentation`.

### MVVM
Flujo: `UI -> ViewModel -> UseCase -> Repository -> DataSource -> ViewModel -> UIState`.

---

## Modelo de dominio (mínimo)
- `Country`: nombre, moneda, idioma, zona horaria, lat/long, bandera.
- `WeatherForecast`: fecha, min/max, lluvia, viento, descripción.
- `Trip`: id, país, fechas, notas, timestamp.

---

## Casos de uso
- `GetCountriesUseCase`
- `SearchCountriesUseCase`
- `GetCountryDetailsUseCase`
- `GetWeatherForecastUseCase`
- `GetTripsUseCase`
- `SaveTripUseCase`
- `UpdateTripUseCase`
- `DeleteTripUseCase`

---

## Data layer (resumen)
- Remote:
  - `CountriesRemoteDataSource` (RestCountries)
  - `WeatherRemoteDataSource` (Open-Meteo)
- Local:
  - `TripsLocalDataSource` (JSON para Desktop)
- Repositorios:
  - `CountriesRepositoryImpl`
  - `WeatherRepositoryImpl`
  - `TripsRepositoryImpl`
- Broker:
  - `CountryWeatherBroker` para combinar país + clima.

---

## UI y navegación
Rutas:
- `home`
- `detail/{countryCode}`
- `trips`

ViewModels:
- `HomeViewModel`
- `DetailViewModel`
- `TripsViewModel`

Estado recomendado:
- `sealed class ListState<T> { Loading, Success, Empty, Error }`

---

## Persistencia y offline
- Guardar viajes en archivo local (`trips.json`).
- Cachear países para fallback offline.
- Si falla clima, mostrar mensaje y mantener detalle del país.

---

## Calidad (SOLID + Clean Code)
- Clases y funciones con una sola responsabilidad.
- Nombres expresivos.
- Sin código muerto.
- Sin magic strings/numbers (usar constantes).
- Consistencia de paquetes y nomenclatura.

---

## Tests obligatorios
Cobertura esperada de lógica core: >= 80%.

Testear:
- Todos los ViewModels.
- Todos los UseCases.
- Implementaciones de repositorio.
- DataSources locales.
- Broker de combinación de APIs.

Escenarios mínimos:
- Happy path.
- Error de red.
- Respuesta vacía.
- Error de parseo.
- Offline con cache.

---

## Plan por etapas (compacto)
1. **Domain**: entidades, contratos, use cases.
2. **Data APIs**: clients + remote sources + mappers.
3. **Data local**: JSON persistence + repositorios.
4. **Broker detalle**: combinación país + clima.
5. **Presentation**: ViewModels + 3 pantallas + navegación.
6. **Estados y errores**: loading/error/empty en toda la UI.
7. **Tests**: unit + integración con MockEngine.
8. **QA final**: lint, cobertura, estabilidad.

---

## División de trabajo (3 personas, GitHub)

### Rama base
- `develop` (integración)
- `main` (release)

### Persona 1 — Domain
- Rama: `feature/domain-layer`
- Entrega: entidades, repositorios (interfaces), use cases, tests de use cases.
- Bloquea a los demás: **sí** (es la base).

### Persona 2 — Data
- Rama: `feature/data-layer`
- Entrega: APIs, data sources, mappers, repos impl, broker, tests de data.
- Depende de Persona 1: **sí**.

### Persona 3 — Presentation
- Rama: `feature/presentation-layer`
- Entrega: ViewModels, screens, componentes, navegación, DI, tests de ViewModels.
- Depende de Persona 1 y 2: **sí**.

### Flujo Git recomendado
1. Cada persona trabaja en su branch.
2. PR a `develop` con checklist + tests.
3. Code review cruzado entre compañeros.
4. Merge a `main` solo al final.

---

## Criterios de aceptación finales
- Cumple Clean Architecture + MVVM.
- Cumple 2 APIs públicas integradas.
- Cumple 3 pantallas navegables.
- Maneja estados y errores sin crashear.
- Persiste viajes y funciona parcialmente offline.
- Pasa tests requeridos con cobertura objetivo.

---

**Fin del plan resumido.**

