# Plan Mejorado — Asistente de Viajes
## Revisión 2.0: Lecciones aprendidas del Proyecto Películas + Optimizaciones

**Última actualización:** Junio 2026  
**Responsables:** Equipo Beatles  
**Estado:** Fase de Diseño Arquitectónico

---

## Índice
1. Validación contra Consigna
2. Cambios principales respecto al plan original
3. Objetivo y alcance
4. Requerimientos funcionales
5. Arquitectura detallada
6. Modelo de datos
7. Integraciones externas  
8. Casos de uso (dominio)
9. Capa de datos
10. Capa de presentación (UI/MVVM)
11. Persistencia local
12. Manejo de estados y errores
13. DI e inyección de dependencias
14. Seguridad y configuración
15. Estrategia de pruebas
16. Checklist de calidad (SOLID + Clean Code)
17. Plan de implementación por etapas
18. Criterios de aceptación
19. División de Trabajo por Equipo (GitHub)

---

## 1. Validación contra Consigna

### ✅ ¿CUMPLE CON MVVM?

**SÍ, 100% MVVM + Clean Architecture**

| Componente MVVM | Implementación en el plan | ✅ |
|-----------------|---------------------------|-----|
| **Model** | Entidades de dominio (`Country`, `Trip`, `WeatherForecast`) + Use Cases | ✅ |
| **View** | Composables (`HomeScreen`, `DetailScreen`, `TripsScreen`) | ✅ |
| **ViewModel** | `HomeViewModel`, `DetailViewModel`, `TripsViewModel` | ✅ |
| **State Management** | `StateFlow<UIState>` observados en Composables | ✅ |
| **Separation of Concerns** | UI no conoce Data, ViewModels no conocen Compose | ✅ |

**Cómo funciona MVVM en el plan:**
```
User interacts → Composable calls ViewModel.function() 
                    ↓
                ViewModel launches coroutine
                    ↓
                ViewModel calls UseCase
                    ↓
                UseCase llama Repository
                    ↓
                Repository obtiene datos (remote/local)
                    ↓
                Result vuelve a ViewModel
                    ↓
                ViewModel actualiza _uiState (StateFlow)
                    ↓
                Composable re-renderiza automáticamente
```

---

### ✅ ¿CUMPLE CON TODA LA CONSIGNA?

| Requisito | Plan | ✅ |
|-----------|------|-----|
| **Arquitectura Clean Architecture + MVVM** | Sección 5: Arquitectura detallada + Sección 10: Presentación MVVM | ✅ |
| **Dos capas internas sin dependencias externas** | Domain (Sección 8) NO depende de Data/Presentation | ✅ |
| **Presentación → Composables + ViewModels** | Sección 10: ViewModels con `@Composable` factories | ✅ |
| **Inyección de dependencias (Hilt o manual)** | Sección 13: Manual con Object singleton (Desktop-friendly) | ✅ |
| **Clean Code + SOLID (S/O/L/I/D)** | Sección 16: Checklist de 15 items concretos | ✅ |
| **Tests unitarios (ViewModels, Use Cases, Repos)** | Sección 15: Cobertura ≥80%, tests de todas capas | ✅ |
| **Manejo de estados Loading/Success/Error** | Sección 10: Sealed classes `ListState<T>` + ejemplos | ✅ |
| **Persistencia local para offline** | Sección 11: `TripsJsonPersistence` + caché de países | ✅ |
| **Al menos dos APIs públicas gratuitas** | Sección 7: RestCountries + Open-Meteo | ✅ |
| **Mínimo dos pantallas con navegación** | Sección 10: Home → Detail → Trips + Navigation.kt | ✅ |
| **Información del destino (moneda, idioma, zona horaria)** | DTOs: `RemoteCountryDTO` con currencies, languages, timezones | ✅ |
| **Pronóstico extendido (7-14 días)** | Sección 7: Open-Meteo + `WeatherForecast` con lista diaria | ✅ |
| **CRUD itinerarios personales** | Sección 8: `SaveTripUseCase`, `UpdateTripUseCase`, `DeleteTripUseCase` | ✅ |
| **Nombres expresivos sin comentarios** | Todas las clases: `GetCountryDetailsUseCase`, `CountryWeatherBroker`, etc. | ✅ |
| **Funciones cortas (≤20 líneas)** | Sección 16.6: Clean Code guideline | ✅ |
| **Sin código muerto** | Sección 16.6: Revisar unused imports/classes en testing | ✅ |
| **Sin magic strings/numbers** | Sección 14: Constants.kt centralizado | ✅ |
| **Manejo de errores sin crashes** | Sección 12: NetworkErrorHandler + Result types + tests | ✅ |

---

### ¿POR QUÉ CUMPLE?

**1. MVVM bien definido:**
- Cada ViewModel es **responsable de un aspecto de UI** (Home lista, Detail muestra info, Trips CRUD).
- Los ViewModels exponen **StateFlow<State>** que los Composables observan reactivamente.
- La lógica de negocio está en **Use Cases**, no en ViewModels.
- Los ViewModels **dependen de abstracciones (UseCase interfaces)**, no de implementaciones.

**2. Clean Architecture respetada:**
- **Domain:** entidades + interfaces de repositorios + use cases (SIN dependencias externas).
- **Data:** implementa los repositorios usando APIs externas + persistencia local.
- **Presentation:** Composables + ViewModels (solo dependen de Domain).
- **Regla de oro:** las capas internas NUNCA importan las externas. ✅

**3. SOLID aplicado:**
- **S**ingle: `CountriesRemoteDataSource` solo fetch, `TripsJsonPersistence` solo archivos.
- **O**pen/Closed: agregar nueva API de clima NO modifica `DetailViewModel`.
- **L**iskov: `FakeCountriesRepository` reemplaza `CountriesRepositoryImpl` en tests.
- **I**nterface Segregation: `CountriesRepository` ≠ `WeatherRepository` (no monolítica).
- **D**ependency Inversion: inyección en constructores, no lookups.

**4. Tests exhaustivos:**
- **Todos los ViewModels testados** (con `runTest` + fakes).
- **Todos los Use Cases testados** (con fakes).
- **Mappers testados** (DTO → Domain).
- **Repositorios testados** (con fakes de DataSources).
- **Brokers testados** (combinación de datos).
- Cobertura target ≥80% en lógica de negocio.

**5. Estados de UI robustos:**
- `ListState<T>` con cuatro estados bien definidos: `Loading`, `Success`, `Empty`, `Error`.
- Imposible tener estados inválidos (ej: `Loading` + `Error` simultáneamente).
- UI **siempre tiene algo que mostrar** (nunca pantalla en blanco).
- Errores de red → mensajes claros al usuario, sin crashes.

**6. Offline-first:**
- Países cacheados en JSON local.
- Si no hay internet, muestra viajes + países cacheados.
- Clima NO se cachea (siempre fresco) pero si offline muestra "sin conexión".

---

### Resumen en una frase:
**El plan es una implementación completa de Clean Architecture + MVVM que respeta SOLID, incluye tests obligatorios y maneja todos los casos de error sin crashes.** ✅

---

## 1. Cambios principales respecto al plan original

### 1.1 ¿Por qué estos cambios?

| Cambio | Razón |
|--------|-------|
| **Broker Pattern para integración de múltiples fuentes** | En Películas aprendimos que combinar datos de 2+ APIs es crítico. El `DetailedMovieBroker` es esencial para normalizar datos. |
| **Separación clara de Remote/Local DataSources** | Evita logic dispersa. Cada responsabilidad específica (obtener remoto vs guardar local) en su propia clase. |
| **Entidades de dominio vs DTOs** | Las entidades de dominio NO conocen sobre serialización. DTOs (mappers) manejan JSON. |
| **Sealed Classes para estados de UI** | Evita estados inválidos (ej: `Loading` + `Error` simultáneamente). |
| **Tests ANTES de UI** | Testear repositorios, use cases y ViewModels asegura que la UI sea solo renderización. |
| **Constantes centralizadas** | `Constants` + `AppConfig` eliminan magic strings/numbers. |
| **Offline-first approach** | Cargar datos locales primero, luego actualizar desde remoto (no bloquear UI). |

### 1.2 Términos clave adoptados
- **Broker**: clase que normaliza datos de múltiples fuentes (TMDB + OMDB → Movie normalizado; RestCountries + Open-Meteo → CountryDetail).
- **Remote DataSource**: responsable únicamente de obtener datos de APIs externas.
- **Local DataSource**: responsable únicamente de persistencia local.
- **Repository**: orquesta entre DataSources y expone interfaz limpia al dominio.

---

## 2. Objetivo y alcance

Construir desde cero una aplicación desktop multiplatforma (Compose Multiplatform) que **planifique viajes**:
- Explorar destinos (países) con información detallada (moneda, idioma, zona horaria).
- Consultar clima extendido (7-14 días) del destino elegido.
- Guardar itinerarios personales con fechas y notas.
- **Funcionar parcialmente sin conexión**: mostrar datos cached.

### 2.1 Diferencias clave vs. Proyecto Películas
- **Dominio**: países + clima + viajes (vs. películas).
- **APIs**: RestCountries + Open-Meteo (vs. TMDB + OMDB).
- **Persistencia**: viajes guardados en local (vs. favoritos de películas).
- **Caché estratégico**: países change rarement; cache agresivo (vs. películas populares siempre frescas).

---

## 3. Requerimientos funcionales

### 3.1 Pantallas

| Pantalla | Responsabilidad | Estados requeridos |
|----------|-----------------|-------------------|
| **Explorador (Home)** | Listar países + búsqueda/filtro | Loading, Success, Error, Empty |
| **Detalle País** | País info + clima + botón "Guardar viaje" | Loading, Success, Error |
| **Mis Viajes** | CRUD de itinerarios guardados | Loading, Success, Empty |

### 3.2 Funcionalidades

- [ ] Listar todos los países (desde RestCountries).
- [ ] Buscar país por nombre.
- [ ] Ver detalle: nombre oficial, capital, moneda(s), idioma(s), zona horaria, bandera.
- [ ] Pronosticar clima por país (Open-Meteo con lat/long de capital).
- [ ] Guardar viaje: país + fecha inicio + fecha fin + notas.
- [ ] Editar viaje guardado.
- [ ] Eliminar viaje.
- [ ] Funcionar offline (mostrar viajes guardados + países en caché).

### 3.3 Navegación
```
Home (explorador) 
  ↓ (click en país) 
Detail (país + clima) 
  ↓ (click "Guardar viaje") 
Modal Crear/Editar Viaje 
  ↓ (guardar) 
Home (actualiza lista) 
  ↓ (tab "Mis viajes") 
Trips Screen
```

---

## 4. Arquitectura detallada

### 4.1 Capas (Clean Architecture)

```
┌─────────────────────────────────────┐
│    PRESENTATION (UI/MVVM)           │ ← Compose Composables + ViewModels
│  (desktopMain/ + commonMain/)       │
└──────────────┬──────────────────────┘
               │  (depende de)
               ↓
┌──────────────────────────────────────┐
│      DOMAIN (Entidades + Puertos)    │
│   (sealed classes, interfaces)       │ ← USE CASES + REPOSITORIES (interfaces)
│   (commonMain/)                      │    NO DEPENDENCIES EXTERNAS
└──────────────┬──────────────────────┘
               │  (implementado por)
               ↓
┌──────────────────────────────────────┐
│    DATA (Implementación)             │
│  (Remote + Local + Mappers)          │ ← DTOs + API Clients + DB
│  (desktopMain/)                      │
└──────────────────────────────────────┘
```

### 4.2 Principios SOLID aplicados

| Principio | Aplicación | Ejemplo |
|-----------|-----------|---------|
| **S**ingle Responsibility | Cada clase hace UNA cosa | `CountriesRemoteDataSource` solo fetch, no mapea ni cachea |
| **O**pen/Closed | Extensible sin modificar | Nueva API clima → solo new DataSource + broker actualizado |
| **L**iskov Substitution | Implementaciones intercambiables | `CountriesRepository` works con real o fake DataSource |
| **I**nterface Segregation | Interfaces específicas | `CountriesRepository` ≠ `TripsRepository` |
| **D**ependency Inversion | Inyectar abstracciones | `ViewModel(useCase)` no `ViewModel(repository)` |

### 4.3 Paquetes recomendados (estructura)

```
composeApp/src/
├── commonMain/kotlin/edu/dyds/trips/
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── Country.kt
│   │   │   ├── WeatherForecast.kt
│   │   │   └── Trip.kt
│   │   ├── repository/
│   │   │   ├── CountriesRepository.kt (interfaz)
│   │   │   ├── WeatherRepository.kt (interfaz)
│   │   │   └── TripsRepository.kt (interfaz)
│   │   └── usecase/
│   │       ├── GetCountriesUseCase.kt (interfaz + impl)
│   │       ├── SearchCountriesUseCase.kt
│   │       ├── GetCountryDetailsUseCase.kt
│   │       ├── GetWeatherForecastUseCase.kt
│   │       ├── GetTripsUseCase.kt
│   │       ├── SaveTripUseCase.kt
│   │       ├── DeleteTripUseCase.kt
│   │       └── UpdateTripUseCase.kt
│   └── util/
│       └── Constants.kt (constantes de dominio)
│
├── desktopMain/kotlin/edu/dyds/trips/
│   ├── presentation/
│   │   ├── App.kt (Entry point Compose)
│   │   ├── Navigation.kt (rutas + args)
│   │   ├── home/
│   │   │   ├── HomeViewModel.kt
│   │   │   └── HomeScreen.kt
│   │   ├── detail/
│   │   │   ├── DetailViewModel.kt
│   │   │   └── DetailScreen.kt
│   │   ├── trips/
│   │   │   ├── TripsViewModel.kt
│   │   │   ├── TripsScreen.kt
│   │   │   └── TripEditDialog.kt (modal create/edit)
│   │   └── components/
│   │       ├── LoadingBox.kt
│   │       ├── ErrorBox.kt
│   │       ├── CountryCard.kt
│   │       ├── TripCard.kt
│   │       └── WeatherForecastWidget.kt
│   │
│   ├── data/
│   │   ├── remote/
│   │   │   ├── countries/
│   │   │   │   ├── CountriesRemoteDataSource.kt (interfaz)
│   │   │   │   ├── CountriesRemoteDataSourceImpl.kt
│   │   │   │   ├── RestCountriesClient.kt (Ktor)
│   │   │   │   └── RemoteCountryDTO.kt
│   │   │   ├── weather/
│   │   │   │   ├── WeatherRemoteDataSource.kt (interfaz)
│   │   │   │   ├── WeatherRemoteDataSourceImpl.kt
│   │   │   │   ├── OpenMeteoClient.kt (Ktor)
│   │   │   │   └── RemoteWeatherDTO.kt
│   │   │   └── broker/
│   │   │       └── CountryWeatherBroker.kt
│   │   │           (Combina países + clima en detalle)
│   │   │
│   │   ├── local/
│   │   │   ├── TripsLocalDataSource.kt (interfaz)
│   │   │   ├── TripsLocalDataSourceImpl.kt
│   │   │   ├── TripsJsonPersistence.kt (archivo JSON)
│   │   │   └── LocalTripDTO.kt
│   │   │
│   │   ├── mapper/
│   │   │   ├── CountryMapper.kt (DTO → domain entity)
│   │   │   ├── WeatherMapper.kt
│   │   │   └── TripMapper.kt
│   │   │
│   │   └── repository/
│   │       ├── CountriesRepositoryImpl.kt
│   │       │   (Implementa CountriesRepository)
│   │       │   (Orquesta remote + local)
│   │       ├── WeatherRepositoryImpl.kt
│   │       └── TripsRepositoryImpl.kt
│   │
│   ├── di/
│   │   └── TripsDependencyInjector.kt (singleton factories)
│   │
│   ├── config/
│   │   ├── AppConfig.kt (lee env vars / props)
│   │   └── NetworkConfig.kt (Ktor clients)
│   │
│   └── util/
│       ├── Constants.kt (urls base, keys, defaults)
│       └── Logger.kt (si aplica)
│
└── desktopTest/kotlin/edu/dyds/trips/
    ├── domain/usecase/
    │   ├── GetCountriesUseCaseTest.kt
    │   ├── GetWeatherForecastUseCaseTest.kt
    │   ├── SaveTripUseCaseTest.kt
    │   └── ...
    ├── data/
    │   ├── repository/
    │   │   ├── CountriesRepositoryImplTest.kt
    │   │   ├── WeatherRepositoryImplTest.kt
    │   │   └── TripsRepositoryImplTest.kt
    │   ├── remote/
    │   │   ├── CountriesRemoteDataSourceTest.kt
    │   │   └── WeatherRemoteDataSourceTest.kt
    │   ├── local/
    │   │   └── TripsLocalDataSourceTest.kt
    │   └── mapper/
    │       ├── CountryMapperTest.kt
    │       └── WeatherMapperTest.kt
    ├── presentation/
    │   ├── HomeViewModelTest.kt
    │   ├── DetailViewModelTest.kt
    │   └── TripsViewModelTest.kt
    └── util/
        ├── FakeCountriesRemoteSource.kt (mocks para tests)
        ├── FakeWeatherRemoteSource.kt
        └── FakeTripsLocalSource.kt
```

---

## 5. Modelo de datos

### 5.1 Entidades de dominio (NO serializadas)

**Country.kt**
```kotlin
data class Country(
    val code: String,                      // ej: "AR", "US"
    val name: String,
    val officialName: String,
    val region: String,                    // ej: "Americas"
    val subregion: String?,
    val capital: String?,
    val currencies: Map<String, Currency>, // { "ARS" → Currency(...) }
    val languages: Map<String, String>,    // { "es" → "Spanish" }
    val timezones: List<String>,           // [ "UTC-03:00", ... ]
    val latitude: Double,
    val longitude: Double,
    val flagUrl: String,                   // URL de bandera
    val population: Int
)

data class Currency(
    val code: String,
    val name: String,
    val symbol: String
)
```

**WeatherForecast.kt**
```kotlin
data class WeatherForecast(
    val date: String,              // "YYYY-MM-DD"
    val tempMinCelsius: Double,
    val tempMaxCelsius: Double,
    val precipitationMm: Double,
    val windSpeedKmh: Double,
    val weatherCode: Int,          // WMO código (0=cielo claro, etc)
    val description: String        // "Sunny", "Rainy", etc (derivado de weatherCode)
)
```

**Trip.kt**
```kotlin
data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val countryCode: String,
    val countryName: String,       // denormalizado para rapidez en UI
    val startDate: String,         // "YYYY-MM-DD"
    val endDate: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

### 5.2 DTOs (para serialización JSON)

**RemoteCountryDTO.kt** (RestCountries)
```kotlin
@Serializable
data class RemoteCountryDTO(
    val cca2: String,
    val name: RemoteCountryNameDTO,
    val region: String,
    val subregion: String?,
    val capital: List<String>?,
    val currencies: Map<String, RemoteCurrencyDTO>?,
    val languages: Map<String, String>?,
    val timezones: List<String>,
    val latlng: List<Double>,
    val flags: RemoteFlagsDTO,
    val population: Int
) {
    @Serializable
    data class RemoteCountryNameDTO(
        val common: String,
        val official: String
    )
    
    @Serializable
    data class RemoteCurrencyDTO(
        val name: String,
        val symbol: String
    )
    
    @Serializable
    data class RemoteFlagsDTO(
        val png: String,
        val svg: String
    )
}
```

**RemoteWeatherDTO.kt** (Open-Meteo)
```kotlin
@Serializable
data class RemoteWeatherDTO(
    val daily: DailyDTO
) {
    @Serializable
    data class DailyDTO(
        val time: List<String>,
        val temperature_2m_max: List<Double>,
        val temperature_2m_min: List<Double>,
        val precipitation_sum: List<Double>,
        val windspeed_10m_max: List<Double>,
        val weather_code: List<Int>
    )
}
```

**LocalTripDTO.kt** (persistencia JSON)
```kotlin
@Serializable
data class LocalTripDTO(
    val id: String,
    val countryCode: String,
    val countryName: String,
    val startDate: String,
    val endDate: String,
    val notes: String,
    val createdAt: Long
)
```

---

## 6. Integraciones externas

### 6.1 API 1: RestCountries

| Propiedad | Valor |
|-----------|-------|
| **Endpoint** | `https://restcountries.com/v3.1/all` (listar) o `/name/{name}` (buscar) |
| **Método** | GET |
| **Auth** | Ninguna (pública) |
| **Rate Limit** | No oficial, pero ~450 req/min recomendado |
| **Response** | JSON array de países |
| **Qué usamos** | Nombre oficial, moneda, idioma, zona horaria, lat/long, bandera, población |

**Estrategia de caché:**
- Descargar TODO una sola vez (lista completa).
- Guardar en `trips.json` dentro array `cachedCountries`.
- Actualizar si usuario clica "refresh" o estamos online y >7 días desde último fetch.

### 6.2 API 2: Open-Meteo

| Propiedad | Valor |
|-----------|-------|
| **Endpoint** | `https://api.open-meteo.com/v1/forecast` |
| **Params** | `latitude`, `longitude`, `daily` (campos a traer), `timezone` |
| **Auth** | Ninguna (pública) |
| **Rate Limit** | 10k req/día free (suficiente) |
| **Response** | JSON con forecast diario |
| **Qué usamos** | Temperatura min/max, precipitación, velocidad viento, código meteorológico (WMO) |

**Ejemplo de request:**
```
GET https://api.open-meteo.com/v1/forecast?latitude=-34.6037&longitude=-58.3816&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,windspeed_10m_max,weather_code&timezone=America/Argentina/Buenos_Aires
```

**Estrategia:**
- Clima NO se cachea (siempre fresco).
- Si offline, mostrar estado "Sin conexión - datos no disponibles".
- Mostrar 7 días adelante por defecto.

---

## 7. Casos de uso (dominio)

### Interfaces (en commonMain/domain/usecase)

```kotlin
// GetCountriesUseCase.kt
interface GetCountriesUseCase {
    suspend operator fun invoke(): Result<List<Country>>
}

// SearchCountriesUseCase.kt
interface SearchCountriesUseCase {
    suspend operator fun invoke(query: String): Result<List<Country>>
}

// GetCountryDetailsUseCase.kt
interface GetCountryDetailsUseCase {
    suspend operator fun invoke(countryCode: String): Result<CountryDetail>
}
// CountryDetail = Country + clima actual + últimas previsiones

// GetWeatherForecastUseCase.kt
interface GetWeatherForecastUseCase {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Result<List<WeatherForecast>>
}

// GetTripsUseCase.kt
interface GetTripsUseCase {
    suspend operator fun invoke(): Result<List<Trip>>
}

// SaveTripUseCase.kt
interface SaveTripUseCase {
    suspend operator fun invoke(trip: Trip): Result<Unit>
}

// UpdateTripUseCase.kt
interface UpdateTripUseCase {
    suspend operator fun invoke(trip: Trip): Result<Unit>
}

// DeleteTripUseCase.kt
interface DeleteTripUseCase {
    suspend operator fun invoke(tripId: String): Result<Unit>
}
```

### Implementaciones

Muy cortas. Ej:

```kotlin
class GetCountriesUseCaseImpl(
    private val countriesRepository: CountriesRepository
) : GetCountriesUseCase {
    override suspend fun invoke(): Result<List<Country>> =
        countriesRepository.getCountries()
}
```

**Patrón**: orquestar + validar lógica, no hacer work pesado aquí.

---

## 8. Capa de datos

### 8.1 Repositorios (interfaces en domain)

**CountriesRepository** (domain/repository)
```kotlin
interface CountriesRepository {
    suspend fun getCountries(): Result<List<Country>>
    suspend fun searchCountries(query: String): Result<List<Country>>
    suspend fun getCountryByCode(code: String): Result<Country>
}
```

**WeatherRepository** (domain/repository)
```kotlin
interface WeatherRepository {
    suspend fun getWeatherForecast(
        latitude: Double,
        longitude: Double
    ): Result<List<WeatherForecast>>
}
```

**TripsRepository** (domain/repository)
```kotlin
interface TripsRepository {
    suspend fun getTrips(): Result<List<Trip>>
    suspend fun getTripById(id: String): Result<Trip?>
    suspend fun saveTrip(trip: Trip): Result<Unit>
    suspend fun updateTrip(trip: Trip): Result<Unit>
    suspend fun deleteTrip(id: String): Result<Unit>
}
```

### 8.2 Data Sources

**CountriesRemoteDataSource** (desktopMain/data/remote/countries)
```kotlin
interface CountriesRemoteDataSource {
    suspend fun fetchCountries(): Result<List<RemoteCountryDTO>>
    suspend fun searchCountries(query: String): Result<List<RemoteCountryDTO>>
}

class CountriesRemoteDataSourceImpl(
    private val client: RestCountriesClient
) : CountriesRemoteDataSource {
    override suspend fun fetchCountries() =
        runCatching { client.getCountries() }
            .mapCatching { it.toDomain() }
    // ... etc
}
```

**WeatherRemoteDataSource** (desktopMain/data/remote/weather)
```kotlin
interface WeatherRemoteDataSource {
    suspend fun fetchForecast(
        latitude: Double,
        longitude: Double,
        timezone: String
    ): Result<RemoteWeatherDTO>
}

class WeatherRemoteDataSourceImpl(
    private val client: OpenMeteoClient
) : WeatherRemoteDataSource {
    override suspend fun fetchForecast(...) =
        runCatching { client.getForecast(...) }
}
```

**TripsLocalDataSource** (desktopMain/data/local)
```kotlin
interface TripsLocalDataSource {
    suspend fun getTrips(): Result<List<LocalTripDTO>>
    suspend fun saveTrip(trip: LocalTripDTO): Result<Unit>
    suspend fun updateTrip(trip: LocalTripDTO): Result<Unit>
    suspend fun deleteTrip(id: String): Result<Unit>
}

class TripsLocalDataSourceImpl(
    private val persistence: TripsJsonPersistence
) : TripsLocalDataSource {
    override suspend fun getTrips() =
        runCatching { persistence.loadTrips() }
    // ...
}
```

### 8.3 Broker: Combinar múltiples fuentes

**CountryWeatherBroker** (desktopMain/data/broker)

```kotlin
class CountryWeatherBroker(
    private val countriesRepository: CountriesRepository,
    private val weatherRepository: WeatherRepository
) {
    suspend fun getCountryDetail(
        countryCode: String
    ): Result<CountryDetail> = runCatching {
        val country = countriesRepository
            .getCountryByCode(countryCode)
            .getOrThrow()
            
        val forecast = weatherRepository
            .getWeatherForecast(country.latitude, country.longitude)
            .getOrNull() // fallback a lista vacía si error
        
        CountryDetail(
            country = country,
            weatherForecast = forecast ?: emptyList()
        )
    }
}
```

**Ventaja**: separa lógica de combinación. Si luego queremos agregar más fuentes (ej: noticias), ajustamos broker sin tocar ViewModels.

### 8.4 Repositorios (implementación)

**CountriesRepositoryImpl**

```kotlin
class CountriesRepositoryImpl(
    private val remoteDataSource: CountriesRemoteDataSource,
    private val localCache: TripsLocalDataSource // para cachear países
) : CountriesRepository {
    
    override suspend fun getCountries(): Result<List<Country>> = runCatching {
        // Intentar remoto primero
        remoteDataSource.fetchCountries().getOrNull()
            ?.map { it.toDomain() }
            ?.also { saveToCache(it) }
            // Si falla remoto, cargar cache
            ?: loadFromCache()
    }

    override suspend fun searchCountries(query: String) =
        runCatching {
            getCountries().getOrThrow()
                .filter { it.name.contains(query, ignoreCase = true) }
        }

    // helpers...
}
```

### 8.5 Mappers

**CountryMapper**

```kotlin
fun RemoteCountryDTO.toDomain() = Country(
    code = cca2,
    name = name.common,
    officialName = name.official,
    region = region,
    subregion = subregion,
    capital = capital?.firstOrNull(),
    currencies = currencies?.mapValues { (_, remote) ->
        Currency(
            name = remote.name,
            symbol = remote.symbol,
            code = /* extraer del key */ ""
        )
    } ?: emptyMap(),
    languages = languages ?: emptyMap(),
    timezones = timezones,
    latitude = latlng[0],
    longitude = latlng[1],
    flagUrl = flags.png,
    population = population
)

fun Country.toLocal() = LocalCountryDTO(
    code = code,
    name = name,
    // ... serializar para JSON
)
```

---

## 9. Capa de presentación (UI/MVVM)

### 9.1 Estados de UI (Sealed Classes)

```kotlin
// UIState.kt
sealed class UIState<out T> {
    object Loading : UIState<Nothing>()
    data class Success<T>(val data: T) : UIState<T>()
    data class Error(val exception: Exception) : UIState<Nothing>()
}

// Alternativa con estado vacío
sealed class ListState<out T> {
    object Loading : ListState<Nothing>()
    data class Success<T>(val items: List<T>) : ListState<T>()
    object Empty : ListState<Nothing>()
    data class Error(val message: String) : ListState<Nothing>()
}
```

### 9.2 HomeViewModel

```kotlin
class HomeViewModel(
    private val getCountriesUseCase: GetCountriesUseCase,
    private val searchCountriesUseCase: SearchCountriesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ListState<Country>>(Loading)
    val uiState: StateFlow<ListState<Country>> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        loadCountries()
    }
    
    fun loadCountries() {
        viewModelScope.launch {
            _uiState.value = Loading
            _uiState.value = when (val result = getCountriesUseCase()) {
                is Result.Success -> {
                    val countries = result.value
                    if (countries.isEmpty()) Empty
                    else Success(countries)
                }
                is Result.Failure -> Error(result.exception.message ?: "Unknown error")
            }
        }
    }
    
    fun onSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                loadCountries()
            } else {
                _uiState.value = Loading
                _uiState.value = when (val result = searchCountriesUseCase(query)) {
                    is Result.Success -> {
                        val countries = result.value
                        if (countries.isEmpty()) Empty
                        else Success(countries)
                    }
                    is Result.Failure -> Error(result.exception.message ?: "")
                }
            }
        }
    }
}
```

### 9.3 DetailViewModel

```kotlin
data class DetailUiState(
    val country: Country? = null,
    val weather: List<WeatherForecast>? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class DetailViewModel(
    private val getCountryDetailsUseCase: GetCountryDetailsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
    
    fun loadCountryDetail(countryCode: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState(loading = true)
            when (val result = getCountryDetailsUseCase(countryCode)) {
                is Result.Success -> {
                    _uiState.value = DetailUiState(
                        country = result.value.country,
                        weather = result.value.weather,
                        loading = false
                    )
                }
                is Result.Failure -> {
                    _uiState.value = DetailUiState(
                        error = result.exception.message,
                        loading = false
                    )
                }
            }
        }
    }
}
```

### 9.4 TripsViewModel

```kotlin
class TripsViewModel(
    private val getTripsUseCase: GetTripsUseCase,
    private val saveTripUseCase: SaveTripUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val deleteTripUseCase: DeleteTripUseCase
) : ViewModel() {
    
    private val _trips = MutableStateFlow<ListState<Trip>>(Loading)
    val trips: StateFlow<ListState<Trip>> = _trips.asStateFlow()
    
    init {
        loadTrips()
    }
    
    fun loadTrips() {
        viewModelScope.launch {
            _trips.value = Loading
            when (val result = getTripsUseCase()) {
                is Result.Success -> {
                    _trips.value = if (result.value.isEmpty()) {
                        Empty
                    } else {
                        Success(result.value)
                    }
                }
                is Result.Failure -> _trips.value = Error(...)
            }
        }
    }
    
    fun saveTrip(trip: Trip) = viewModelScope.launch {
        when (val result = saveTripUseCase(trip)) {
            is Result.Success -> loadTrips()
            is Result.Failure -> _trips.value = Error(...)
        }
    }
    
    fun deleteTrip(tripId: String) = viewModelScope.launch {
        when (val result = deleteTripUseCase(tripId)) {
            is Result.Success -> loadTrips()
            is Result.Failure -> _trips.value = Error(...)
        }
    }
}
```

### 9.5 Screens (Composables)

**HomeScreen.kt** (estructura)

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCountrySelected: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        TextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQuery,
            placeholder = { Text("Buscar país...") }
        )
        
        // Content based on state
        when (uiState) {
            is Loading -> LoadingBox()
            is Empty -> EmptyBox("No hay países")
            is Error -> ErrorBox((uiState as Error).message)
            is Success -> {
                val countries = (uiState as Success<List<Country>>).data
                LazyColumn {
                    items(countries) { country ->
                        CountryCard(
                            country = country,
                            onClick = { onCountrySelected(country.code) }
                        )
                    }
                }
            }
        }
    }
}
```

**DetailScreen.kt** (estructura)

```kotlin
@Composable
fun DetailScreen(
    countryCode: String,
    viewModel: DetailViewModel,
    onSaveTrip: (Trip) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val showTripDialog = remember { mutableStateOf(false) }
    
    LaunchedEffect(countryCode) {
        viewModel.loadCountryDetail(countryCode)
    }
    
    when {
        uiState.loading -> LoadingBox()
        uiState.error != null -> ErrorBox(uiState.error!!)
        uiState.country != null -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // Country info
                CountryDetailView(uiState.country!!)
                
                // Weather forecast
                if (uiState.weather != null) {
                    WeatherForecastWidget(forecasts = uiState.weather!!)
                }
                
                // Save trip button
                Button(onClick = { showTripDialog.value = true }) {
                    Text("Guardar en mis viajes")
                }
                
                // Dialog to create trip
                if (showTripDialog.value) {
                    TripEditDialog(
                        countryName = uiState.country!!.name,
                        onSave = onSaveTrip,
                        onDismiss = { showTripDialog.value = false }
                    )
                }
            }
        }
    }
}
```

**TripsScreen.kt** (estructura)

```kotlin
@Composable
fun TripsScreen(
    viewModel: TripsViewModel,
    onEditTrip: (Trip) -> Unit
) {
    val trips by viewModel.trips.collectAsState()
    val showCreateDialog = remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { showCreateDialog.value = true }) {
            Text("Crear nuevo viaje")
        }
        
        when (trips) {
            is Loading -> LoadingBox()
            is Empty -> EmptyBox("No hay viajes guardados")
            is Error -> ErrorBox((trips as Error).message)
            is Success -> {
                val tripList = (trips as Success<List<Trip>>).data
                LazyColumn {
                    items(tripList) { trip ->
                        TripCard(
                            trip = trip,
                            onEdit = { onEditTrip(trip) },
                            onDelete = { viewModel.deleteTrip(trip.id) }
                        )
                    }
                }
            }
        }
        
        if (showCreateDialog.value) {
            TripEditDialog(
                onSave = { trip -> viewModel.saveTrip(trip) },
                onDismiss = { showCreateDialog.value = false }
            )
        }
    }
}
```

---

## 10. Persistencia local

### 10.1 Estrategia Desktop (MVP)

**Opción simple**: JSON file en filesystem

```kotlin
// TripsJsonPersistence.kt
class TripsJsonPersistence {
    private val tripsFile = File(
        System.getProperty("java.io.tmpdir"),
        "trips.json"
    )
    
    suspend fun loadTrips(): List<LocalTripDTO> = withContext(Dispatchers.IO) {
        if (!tripsFile.exists()) return@withContext emptyList()
        
        try {
            Json.decodeFromString(tripsFile.readText())
        } catch (e: Exception) {
            emptyList() // corrupted file fallback
        }
    }
    
    suspend fun saveTrip(trip: LocalTripDTO) = withContext(Dispatchers.IO) {
        val trips = loadTrips().toMutableList()
        trips.removeIf { it.id == trip.id } // evitar duplicados
        trips.add(trip)
        tripsFile.writeText(Json.encodeToString(trips))
    }
    
    suspend fun deleteTrip(id: String) = withContext(Dispatchers.IO) {
        val trips = loadTrips().filter { it.id != id }
        tripsFile.writeText(Json.encodeToString(trips))
    }
}
```

### 10.2 Cache de países (opcional pero recomendado)

Agregar tabla `cachedCountries` al mismo JSON:

```kotlin
@Serializable
data class AppDataFile(
    val trips: List<LocalTripDTO> = emptyList(),
    val cachedCountries: List<RemoteCountryDTO> = emptyList(),
    val lastCountriesFetchTime: Long = 0L // timestamp
)
```

Estrategia:
- Si online Y país cache >7 días → refetch.
- Si offline → usar cache viejo.
- Si sin cache Y offline → mostrar "offline - sin datos de países".

---

## 11. Manejo de estados y errores

### 11.1 Result Type (Kotlin estándar)

```kotlin
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
}

// Extension functions
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> value
    is Result.Failure -> null
}

fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> value
    is Result.Failure -> throw exception
}
```

### 11.2 Network Error Handling

```kotlin
object NetworkErrorHandler {
    fun handle(throwable: Throwable): String = when (throwable) {
        is SocketTimeoutException -> "Timeout - intenta nuevamente"
        is ConnectException -> "No hay conexión - verifica tu red"
        is SerializationException -> "Datos inválidos desde servidor"
        else -> throwable.message ?: "Error desconocido"
    }
}
```

### 11.3 Reintentos (simple)

```kotlin
suspend fun <T> retryOnNetwork(
    times: Int = 3,
    delayMillis: Long = 1000,
    block: suspend () -> Result<T>
): Result<T> {
    var lastException: Exception? = null
    repeat(times) {
        val result = block()
        if (result is Result.Success) return result
        if (result is Result.Failure) {
            lastException = result.exception
            if (it < times - 1) delay(delayMillis)
        }
    }
    return Result.Failure(lastException ?: Exception("Max retries reached"))
}
```

---

## 12. DI e inyección de dependencias

### 12.1 Manual con Object Singleton

```kotlin
// TripsDependencyInjector.kt (desktopMain)

object TripsDependencyInjector {
    
    // HTTP Clients
    private val countriesHttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
        }
    }
    
    private val weatherHttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    
    // API Clients
    private val restCountriesClient = RestCountriesClient(countriesHttpClient)
    private val openMeteoClient = OpenMeteoClient(weatherHttpClient)
    
    // Data Sources
    private val countriesRemoteDataSource: CountriesRemoteDataSource =
        CountriesRemoteDataSourceImpl(restCountriesClient)
    
    private val weatherRemoteDataSource: WeatherRemoteDataSource =
        WeatherRemoteDataSourceImpl(openMeteoClient)
    
    private val tripsLocalDataSource: TripsLocalDataSource =
        TripsLocalDataSourceImpl(TripsJsonPersistence())
    
    // Repositories
    private val countriesRepository: CountriesRepository =
        CountriesRepositoryImpl(countriesRemoteDataSource, tripsLocalDataSource)
    
    private val weatherRepository: WeatherRepository =
        WeatherRepositoryImpl(weatherRemoteDataSource)
    
    private val tripsRepository: TripsRepository =
        TripsRepositoryImpl(tripsLocalDataSource)
    
    // Broker
    private val countryWeatherBroker = CountryWeatherBroker(
        countriesRepository,
        weatherRepository
    )
    
    // Use Cases
    private val getCountriesUseCase: GetCountriesUseCase =
        GetCountriesUseCaseImpl(countriesRepository)
    
    private val searchCountriesUseCase: SearchCountriesUseCase =
        SearchCountriesUseCaseImpl(countriesRepository)
    
    private val getCountryDetailsUseCase: GetCountryDetailsUseCase =
        GetCountryDetailsUseCaseImpl(countryWeatherBroker)
    
    private val getTripsUseCase: GetTripsUseCase =
        GetTripsUseCaseImpl(tripsRepository)
    
    private val saveTripUseCase: SaveTripUseCase =
        SaveTripUseCaseImpl(tripsRepository)
    
    private val updateTripUseCase: UpdateTripUseCase =
        UpdateTripUseCaseImpl(tripsRepository)
    
    private val deleteTripUseCase: DeleteTripUseCase =
        DeleteTripUseCaseImpl(tripsRepository)
    
    // ViewModel factories
    @Composable
    fun getHomeViewModel(): HomeViewModel {
        return viewModel {
            HomeViewModel(getCountriesUseCase, searchCountriesUseCase)
        }
    }
    
    @Composable
    fun getDetailViewModel(): DetailViewModel {
        return viewModel {
            DetailViewModel(getCountryDetailsUseCase)
        }
    }
    
    @Composable
    fun getTripsViewModel(): TripsViewModel {
        return viewModel {
            TripsViewModel(
                getTripsUseCase,
                saveTripUseCase,
                updateTripUseCase,
                deleteTripUseCase
            )
        }
    }
}
```

### 12.2 Uso en App

```kotlin
@Composable
fun App() {
    val homeViewModel = TripsDependencyInjector.getHomeViewModel()
    val detailViewModel = TripsDependencyInjector.getDetailViewModel()
    val tripsViewModel = TripsDependencyInjector.getTripsViewModel()
    
    // Navigation + screens...
}
```

---

## 13. Seguridad y configuración

### 13.1 AppConfig

```kotlin
// AppConfig.kt (no versionado)

object AppConfig {
    const val REST_COUNTRIES_BASE_URL = "https://restcountries.com/v3.1"
    const val OPEN_METEO_BASE_URL = "https://api.open-meteo.com/v1"
    
    const val HTTP_TIMEOUT_MS = 10000L
    const val WEATHER_FORECAST_DAYS = 7
    const val COUNTRIES_CACHE_VALIDITY_DAYS = 7
}
```

### 13.2 Evitar hardcoding

- ❌ NO: `"https://api.example.com".length`
- ✅ SÍ: `Constants.API_URL`

```kotlin
// Constants.kt

object Constants {
    object API {
        const val COUNTRIES_BASE = "https://restcountries.com/v3.1"
        const val WEATHER_BASE = "https://api.open-meteo.com/v1"
        const val TIMEOUT_MS = 10000L
    }
    
    object Cache {
        const val VALIDITY_DAYS = 7
        const val MAX_COUNTRIES = 250
    }
    
    object Ui {
        const val SEARCH_DEBOUNCE_MS = 300L
        const val MAX_FORECAST_DAYS = 14
    }
}
```

---

## 14. Estrategia de pruebas

### 14.1 Cobertura obligatoria

| Clase | Happy Path | Edge Cases |
|-------|-----------|-----------|
| HomeViewModel | Cargar países | Empty list, error network |
| DetailViewModel | Mostrar país + clima | Clima unavailable, error |
| TripsViewModel | CRUD completo | Conflict, corruption |
| CountriesRepositoryImpl | Get + cache | Network down, corrupted cache |
| TripsRepositoryImpl | CRUD local | File doesn't exist, permiso negado |
| Mappers | DTO → Domain | Null fields, malformed JSON |
| Brokers | Combinar datos | Falla weather pero país exists |

### 14.2 Ejemplo: HomeViewModelTest

```kotlin
@Test
fun `loadCountries should emit Success with countries`() = runTest {
    // Arrange
    val fakeCountries = listOf(
        Country(code = "AR", name = "Argentina", ...),
        Country(code = "BR", name = "Brazil", ...)
    )
    val fakeUseCase = FakeGetCountriesUseCase(Result.Success(fakeCountries))
    val fakeSearchUseCase = FakeSearchCountriesUseCase(emptyList())
    
    val viewModel = HomeViewModel(fakeUseCase, fakeSearchUseCase)
    
    // Act & Assert
    val uiState = viewModel.uiState.first()
    assertTrue(uiState is Success)
    assertEquals((uiState as Success<List<Country>>).data, fakeCountries)
}

@Test
fun `loadCountries should emit Error on exception`() = runTest {
    // Arrange
    val exception = Exception("Network error")
    val fakeUseCase = FakeGetCountriesUseCase(
        Result.Failure(exception)
    )
    
    val viewModel = HomeViewModel(fakeUseCase, FakeSearchCountriesUseCase(emptyList()))
    
    // Act & Assert
    val uiState = viewModel.uiState.first()
    assertTrue(uiState is Error)
}
```

### 14.3 Fake/Mock implementations

```kotlin
// FakeCountriesRemoteDataSource.kt (test folder)

class FakeCountriesRemoteDataSource(
    private val response: Result<List<RemoteCountryDTO>>
) : CountriesRemoteDataSource {
    override suspend fun fetchCountries() = response
    override suspend fun searchCountries(query: String) = response
}
```

### 14.4 Integration tests (MockEngine)

```kotlin
@Test
fun `RestCountriesClient should parse countries correctly`() = runTest {
    // Arrange
    val mockEngine = MockEngine { scope ->
        respond(
            content = ByteReadChannel(MOCK_COUNTRIES_JSON),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }
    val httpClient = HttpClient(mockEngine)
    val client = RestCountriesClient(httpClient)
    
    // Act
    val result = client.getCountries()
    
    // Assert
    assertTrue(result.isNotEmpty())
    assertEquals(result[0].cca2, "AR")
}
```

---

## 15. Checklist de calidad (SOLID + Clean Code)

### 15.1 Single Responsibility

- [ ] `HomeViewModel` solo maneja UI state de lista países.
- [ ] `CountriesRemoteDataSource` solo hace HTTP calls.
- [ ] `TripsLocalDataSource` solo maneja persistencia local.
- [ ] Broker solo combina datos de múltiples fuentes.

### 15.2 Open/Closed

- [ ] Agregar nueva API clima NO requiere cambiar Domain/Presentation.
- [ ] Implementaría: `NewWeatherRemoteDataSource` + actualizar Broker.

### 15.3 Liskov Substitution

- [ ] Cualquier `CountriesRepository` impl cumple contrato remoto.
- [ ] `FakeCountriesRepository` reemplaza `CountriesRepositoryImpl` en tests.

### 15.4 Interface Segregation

- [ ] ViewModels no conocen `Repository` genérico, solo sus UseCases.
- [ ] `CountriesRepository` ≠ `WeatherRepository` (interfaces separadas).

### 15.5 Dependency Inversion

- [ ] ViewModels dependen de UseCase interfaces, no implementaciones.
- [ ] Inyección en constructor, no Service Locator.

### 15.6 Clean Code

- [ ] Nombres expresivos: `GetCountryDetailsUseCase` no `FetchCountryInfo`.
- [ ] Funciones ≤20 líneas (excepto tests).
- [ ] Sin código muerto: delete unused imports/classes.
- [ ] Sin magic numbers: TODO como `7 días` → `Constants.CACHE_VALIDITY_DAYS`.
- [ ] Consistencia Kotlin: `viewModel::method`, `camelCase`, package naming.

---

## 16. Plan de implementación por etapas

### Etapa 1: Fundaciones (2-3 días)
- ✅ Crear estructura de paquetes.
- ✅ Definir entidades de dominio (`Country`, `WeatherForecast`, `Trip`).
- ✅ Definir interfaces de repositorios.
- ✅ Definir interfaces de use cases.
- ✅ Configurar DI básico.

### Etapa 2: API Países (2 días)
- ✅ Implementar `RestCountriesClient` (Ktor).
- ✅ Implementar `CountriesRemoteDataSource`.
- ✅ Implementar `CountryMapper` (DTO → Domain).
- ✅ Implementar `CountriesRepositoryImpl`.
- ✅ Tests: Remote source, mapper, repository.
- ✅ Tests de integración con MockEngine.

### Etapa 3: API Clima (2 días)
- ✅ Implementar `OpenMeteoClient`.
- ✅ Implementar `WeatherRemoteDataSource`.
- ✅ Implementar `WeatherMapper`.
- ✅ Implementar `WeatherRepositoryImpl`.
- ✅ Tests: análogos a Etapa 2.

### Etapa 4: Broker + Detalle (2 días)
- ✅ Implementar `CountryWeatherBroker`.
- ✅ Use case: `GetCountryDetailsUseCase`.
- ✅ ViewModel: `DetailViewModel`.
- ✅ Tests: broker combo, ViewModel.

### Etapa 5: Home Screen (1 día)
- ✅ Use cases: `GetCountriesUseCase`, `SearchCountriesUseCase`.
- ✅ ViewModel: `HomeViewModel`.
- ✅ Screens: `HomeScreen`, `CountryCard`.
- ✅ Tests: ViewModel con fakes.

### Etapa 6: Persistencia Viajes (2 días)
- ✅ Implementar `TripsJsonPersistence`.
- ✅ Implementar `TripsLocalDataSource`.
- ✅ Implementar `TripsRepositoryImpl`.
- ✅ Use cases: CRUD.
- ✅ ViewModel: `TripsViewModel`.
- ✅ Tests: local source, repository.

### Etapa 7: Trips Screen (1 día)
- ✅ Screens: `TripsScreen`, `TripCard`, `TripEditDialog`.
- ✅ Integración con ViewModel.

### Etapa 8: Navegación (1 día)
- ✅ `Navigation.kt` con rutas nombradas.
- ✅ Integrar todas las screens.
- ✅ Pasar args (ej: `countryCode` en detalle).

### Etapa 9: Pulido & Error Handling (1-2 días)
- ✅ Manejo de errores en todas las screens.
- ✅ Loading states visuales.
- ✅ Offline mode (cache fallback).
- ✅ UX: mensajes claros, retry buttons.

### Etapa 10: QA & Optimización (1 día)
- ✅ Cobertura de tests ≥80% en lógica core.
- ✅ Review de Clean Code + SOLID.
- ✅ Performance: evitar recomposiciones innecesarias.
- ✅ Documentación de decisiones arquitectónicas.

---

## 17. Criterios de aceptación

### Funcionalidad

- [ ] Home: listar todos países, búsqueda funciona, sin bloqueos.
- [ ] Detalle: mostrar país + clima actual + pronóstico 7 días.
- [ ] Viajes: crear, editar, eliminar, listar guardados.
- [ ] Offline: sin internet, mostrar datos cacheados (países + viajes).
- [ ] Errores: network down, JSON inválido → UI muestra mensajes claros.

### Código

- [ ] Tests unitarios: todos los ViewModels, UseCases, Repositories, Mappers.
- [ ] Cobertura ≥80% en lógica de negocio.
- [ ] Cero código muerto.
- [ ] Cero magic strings/numbers.
- [ ] Consistencia de nomenclatura + estructura.
- [ ] Commits claros con mensajes descriptivos.

### Arquitectura

- [ ] Domain NO depende de Data ni Presentation.
- [ ] Cada layer responsabilidad clara.
- [ ] DI centralizado en `TripsDependencyInjector`.
- [ ] Fácil agregar nueva API (extensibilidad).

### Performance

- [ ] App inicia en <2 segundos.
- [ ] Listar países: <500ms con cache.
- [ ] Búsqueda: <200ms (local filtering).
- [ ] Clima: <1s (remote).

---

## Resumen de cambios principales respecto al plan original

| Aspecto | Original | Revisado 2.0 | **Razón** |
|---------|----------|--------------|----------|
| **Patrón Broker** | Mencionado vagamente | Detalladísimo (como `DetailedMovieSource` fue crítico) | Aprendizaje directo del Proyecto Películas |
| **Separación DataSources** | Básica | Clara (Remote vs Local vs Broker) | Evita acoplamiento y bugs |
| **Estados de UI** | Simple `Loading/Success/Error` | Sealed classes + `ListState` con `Empty` | Tipo safety |
| **Mappers** | Mencionados | Extensivo con ejemplos | DTOs ≠ Dominio es fundamental |
| **DI** | "Hilt o manual" | Manual con Object singleton (como Películas) | Desktop no soporta Hilt bien |
| **Caché de países** | Opcional | Integrado en estrategia | Offline-first + performance |
| **Retry logic** | No mencionado | Incluido con `retryOnNetwork` | Network es impredecible |
| **Tests** | Enlistados | **Mock engines + integración ejemplificados** | Reproducible |
| **Checklist SOLID** | Vago | **15 checks concretos** | Evaluación objetiva |
| **Documentación de estructura** | Basic | **Árbol de carpetas completo** | Onboarding claro |

---

## 19. División de Trabajo por Equipo (Especificación Detallada para IA y Programadores)

### Estructura del Repositorio

```
DYDS26-Beatles/
├── main (rama principal - protegida - solo merges)
├── develop (rama de integración - punto de sincronización)
├── feature/domain-layer (PERSONA 1 - Feature branch)
├── feature/data-layer (PERSONA 2 - Feature branch)
└── feature/presentation-layer (PERSONA 3 - Feature branch)
```

---

## 🎯 PERSONA 1: DOMAIN LAYER

### 1.1 Metadata
- **Rama:** `feature/domain-layer`
- **Base:** `develop`
- **Prioridad:** 1️⃣ (PRIMERA - Bloqueador para P2 y P3)
- **Duración estimada:** 2-3 días
- **Estado:** Inicia cuando: Repositorio clonado
- **Estado:** Termina cuando: PR mergeada a `develop`

### 1.2 Archivos a crear (RUTA COMPLETA - 16 ARCHIVOS TOTALES)

#### 1.2.1 Entidades de Dominio (CARPETA: `composeApp/src/commonMain/kotlin/edu/dyds/trips/domain/entity/`)

**ARCHIVO 1: `Currency.kt`**
- **Tipo:** Data class
- **Responsabilidad:** Representar moneda (código, nombre, símbolo)
- **Dependencias:** Ninguna
- **Estructura mínima:** 3 campos: `code: String`, `name: String`, `symbol: String`
- **Debe ser:** @Serializable ❌ (NO, es dominio, no DTO)
- **Validación:** Compilar sin warnings

**ARCHIVO 2: `Country.kt`**
- **Tipo:** Data class
- **Responsabilidad:** Entidad dominio para país
- **Campos requeridos (19 totales):**
  - `code: String` (ej: "AR", código ISO 2 letras)
  - `name: String` (ej: "Argentina")
  - `officialName: String` (ej: "Argentine Republic")
  - `region: String` (ej: "Americas")
  - `subregion: String?` (nullable, ej: "South America")
  - `capital: String?` (nullable, ej: "Buenos Aires")
  - `currencies: Map<String, Currency>` (colección de monedas por código)
  - `languages: Map<String, String>` (ej: { "es" → "Spanish" })
  - `timezones: List<String>` (ej: ["UTC-03:00", ...])
  - `latitude: Double` (coordenada)
  - `longitude: Double` (coordenada)
  - `flagUrl: String` (URL a imagen PNG/SVG de bandera)
  - `population: Int` (habitantes)
  - + 6 campos opcionales para expansion futura
- **Debe ser:** Serializable ❌ (NO, es dominio)
- **Equals/HashCode:** Auto-generated por data class ✅
- **Validación:** Build sin warnings, contenga data class por defecto

**ARCHIVO 3: `WeatherForecast.kt`**
- **Tipo:** Data class
- **Responsabilidad:** Representar pronóstico de clima para 1 día
- **Campos requeridos (7 totales):**
  - `date: String` (formato: "YYYY-MM-DD", ej: "2026-06-15")
  - `tempMinCelsius: Double` (temperatura mínima)
  - `tempMaxCelsius: Double` (temperatura máxima)
  - `precipitationMm: Double` (mm de lluvia)
  - `windSpeedKmh: Double` (velocidad viento)
  - `weatherCode: Int` (código WMO oficial, ej: 0=claro, 1=nublado, etc)
  - `description: String` (texto legible: "Sunny", "Rainy", etc - derivado del weatherCode)
- **Debe ser:** Serializable ❌ (NO, es dominio)
- **Validación:** Build sin warnings

**ARCHIVO 4: `Trip.kt`**
- **Tipo:** Data class
- **Responsabilidad:** Representar viaje guardado por usuario
- **Campos requeridos (8 totales) - CON VALORES POR DEFECTO:**
  - `id: String = UUID.randomUUID().toString()` (identificador único)
  - `countryCode: String` (ej: "AR", referencia al país)
  - `countryName: String` (ej: "Argentina", denormalizado para rapidez en UI)
  - `startDate: String` (formato: "YYYY-MM-DD")
  - `endDate: String` (formato: "YYYY-MM-DD")
  - `notes: String = ""` (anotaciones opcionales del usuario)
  - `createdAt: Long = System.currentTimeMillis()` (timestamp creación)
  - + 1 campo opcional `updatedAt: Long?` para tracking
- **Debe validar:** startDate ≤ endDate (en lógica, no en data class)
- **Debe ser:** Serializable ❌ (NO, es dominio)
- **Validación:** Build sin warnings

**ARCHIVO 5: `Result.kt`**
- **Tipo:** Sealed class (NO data class)
- **Responsabilidad:** Envolver resultado de operación (éxito o error)
- **Variantes exactas (2):**
  ```kotlin
  sealed class Result<out T> {
      data class Success<T>(val value: T) : Result<T>()
      data class Failure(val exception: Exception) : Result<Nothing>()
  }
  ```
- **Extension functions requeridas (3):**
  - `fun <T> Result<T>.getOrNull(): T?` (retorna value o null)
  - `fun <T> Result<T>.getOrThrow(): T` (retorna value o lanza exception)
  - `fun <T> Result<T>.getOrElse(default: T): T` (retorna value o default)
- **Must NOT have:** Map, fold, flatMap (KISS)
- **Validación:** Que sea usado en todos los use cases

---

#### 1.2.2 Interfaces de Repositorios (CARPETA: `composeApp/src/commonMain/kotlin/edu/dyds/trips/domain/repository/`)

**ARCHIVO 6: `CountriesRepository.kt`**
- **Tipo:** Interface (NO class)
- **Responsabilidad:** Contrato para operaciones de países
- **Métodos exactos (3):**
  ```kotlin
  suspend fun getCountries(): Result<List<Country>>
  suspend fun searchCountries(query: String): Result<List<Country>>
  suspend fun getCountryByCode(code: String): Result<Country>
  ```
- **Todos con:** suspend (coroutines)
- **Todos retornan:** Result<T> wrapper
- **Validación:** NO implementación, solo signatures

**ARCHIVO 7: `WeatherRepository.kt`**
- **Tipo:** Interface (NO class)
- **Responsabilidad:** Contrato para operaciones de clima
- **Métodos exactos (1):**
  ```kotlin
  suspend fun getWeatherForecast(
      latitude: Double,
      longitude: Double
  ): Result<List<WeatherForecast>>
  ```
- **Retorna:** Lista de 7-14 días de pronósticos
- **Validación:** NO implementación

**ARCHIVO 8: `TripsRepository.kt`**
- **Tipo:** Interface (NO class)
- **Responsabilidad:** Contrato CRUD para viajes del usuario
- **Métodos exactos (5):**
  ```kotlin
  suspend fun getTrips(): Result<List<Trip>>
  suspend fun getTripById(id: String): Result<Trip?>
  suspend fun saveTrip(trip: Trip): Result<Unit>
  suspend fun updateTrip(trip: Trip): Result<Unit>
  suspend fun deleteTrip(id: String): Result<Unit>
  ```
- **Validación:** NO implementación, solo signatures

---

#### 1.2.3 Use Cases - Interfaces (CARPETA: `composeApp/src/commonMain/kotlin/edu/dyds/trips/domain/usecase/`)

**Patrón general para TODOS los use cases:**
```kotlin
interface UseCase {
    suspend operator fun invoke(params): Result<Output>
}
```

**ARCHIVO 9: `GetCountriesUseCase.kt`**
- **Tipo:** Interface + Implementación
- **Interface requerida:**
  ```kotlin
  interface GetCountriesUseCase {
      suspend operator fun invoke(): Result<List<Country>>
  }
  ```
- **Implementación:**
  ```kotlin
  class GetCountriesUseCaseImpl(
      private val repository: CountriesRepository
  ) : GetCountriesUseCase {
      override suspend fun invoke() = repository.getCountries()
  }
  ```
- **Líneas de código:** MÁXIMO 3 (solo delegar)

**ARCHIVO 10: `SearchCountriesUseCase.kt`**
- **Type:** Interface + Impl
- **Interface:**
  ```kotlin
  interface SearchCountriesUseCase {
      suspend operator fun invoke(query: String): Result<List<Country>>
  }
  ```
- **Impl:** Delegar a `repository.searchCountries(query)`
- **Líneas:** MÁXIMO 3

**ARCHIVO 11: `GetCountryDetailsUseCase.kt`**
- **Type:** Interface + Impl
- **Interface:**
  ```kotlin
  interface GetCountryDetailsUseCase {
      suspend operator fun invoke(countryCode: String): Result<CountryDetail>
  }
  ```
- **NOTA:** `CountryDetail` es data class con `country: Country` + `weatherForecast: List<WeatherForecast>` (definir en entidades también)
- **Impl:** Delegar a repository (broker lo hará después en data layer)
- **Líneas:** MÁXIMO 3

**ARCHIVO 12: `GetWeatherForecastUseCase.kt`**
- **Type:** Interface + Impl
- **Interface:**
  ```kotlin
  interface GetWeatherForecastUseCase {
      suspend operator fun invoke(
          latitude: Double,
          longitude: Double
      ): Result<List<WeatherForecast>>
  }
  ```
- **Impl:** Delegar a repository
- **Líneas:** MÁXIMO 3

**ARCHIVO 13: `GetTripsUseCase.kt`**
- **Type:** Interface + Impl
- **Interface:**
  ```kotlin
  interface GetTripsUseCase {
      suspend operator fun invoke(): Result<List<Trip>>
  }
  ```
- **Impl:** Delegar a repository
- **Líneas:** MÁXIMO 3

**ARCHIVO 14: `SaveTripUseCase.kt`**
- **Type:** Interface + Impl
- **Interface:**
  ```kotlin
  interface SaveTripUseCase {
      suspend operator fun invoke(trip: Trip): Result<Unit>
  }
  ```
- **Impl:** Delegar + VALIDAR que trip.startDate ≤ trip.endDate
- **Líneas:** MÁXIMO 5

**ARCHIVO 15: `UpdateTripUseCase.kt`**
- **Type:** Interface + Impl
- **Interface:**
  ```kotlin
  interface UpdateTripUseCase {
      suspend operator fun invoke(trip: Trip): Result<Unit>
  }
  ```
- **Impl:** Delegar + VALIDAR dates
- **Líneas:** MÁXIMO 5

**ARCHIVO 16: `DeleteTripUseCase.kt`**
- **Type:** Interface + Impl
- **Interface:**
  ```kotlin
  interface DeleteTripUseCase {
      suspend operator fun invoke(tripId: String): Result<Unit>
  }
  ```
- **Impl:** Delegar a repository
- **Líneas:** MÁXIMO 3

---

#### 1.2.4 Utilidades de Dominio (CARPETA: `composeApp/src/commonMain/kotlin/edu/dyds/trips/domain/util/`)

**ARCHIVO 17: `Constants.kt`**
- **Tipo:** Object singleton
- **Responsabilidad:** Constantes de DOMINIO (no de config externa)
- **Valores requeridos (mínimo 8):**
  ```kotlin
  object Constants {
      const val WEATHER_FORECAST_DAYS = 7
      const val CACHE_VALIDITY_DAYS = 7
      const val MAX_SEARCH_RESULTS = 250
      const val MIN_TRIP_DURATION_MS = 86400000L // 1 día
      const val DATE_FORMAT = "yyyy-MM-dd"
      const val ERROR_MESSAGE_TIMEOUT = "Timeout - intenta nuevamente"
      const val ERROR_MESSAGE_NO_CONNECTION = "No hay conexión"
      const val ERROR_MESSAGE_INVALID_DATA = "Datos inválidos"
  }
  ```
- **Validación:** Ningún magic string/number en el código

---

### 1.3 Estructura de Carpetas (debe coincidir 100%)

```
composeApp/src/commonMain/kotlin/edu/dyds/trips/domain/
├── entity/
│   ├── Currency.kt
│   ├── Country.kt
│   ├── WeatherForecast.kt
│   ├── Trip.kt
│   ├── CountryDetail.kt (opcional, puede estar aquí)
│   └── Result.kt
├── repository/
│   ├── CountriesRepository.kt
│   ├── WeatherRepository.kt
│   └── TripsRepository.kt
├── usecase/
│   ├── GetCountriesUseCase.kt
│   ├── SearchCountriesUseCase.kt
│   ├── GetCountryDetailsUseCase.kt
│   ├── GetWeatherForecastUseCase.kt
│   ├── GetTripsUseCase.kt
│   ├── SaveTripUseCase.kt
│   ├── UpdateTripUseCase.kt
│   └── DeleteTripUseCase.kt
└── util/
    └── Constants.kt
```

**Total: 1 carpeta raíz + 4 subcarpetas + 17 archivos**

---

### 1.4 Tests Requeridos (CARPETA: `composeApp/src/desktopTest/kotlin/edu/dyds/trips/domain/`)

**Estructura de carpetas:**
```
desktopTest/kotlin/edu/dyds/trips/domain/
└── usecase/
    ├── GetCountriesUseCaseTest.kt
    ├── SearchCountriesUseCaseTest.kt
    ├── GetCountryDetailsUseCaseTest.kt
    ├── GetWeatherForecastUseCaseTest.kt
    ├── GetTripsUseCaseTest.kt
    ├── SaveTripUseCaseTest.kt
    ├── UpdateTripUseCaseTest.kt
    └── DeleteTripUseCaseTest.kt
```

**Patrón de test (ejemplo - GetCountriesUseCase):**

```kotlin
class GetCountriesUseCaseTest {
    private lateinit var fakeRepository: FakeCountriesRepository
    private lateinit var useCase: GetCountriesUseCase
    
    @Before
    fun setup() {
        fakeRepository = FakeCountriesRepository()
        useCase = GetCountriesUseCaseImpl(fakeRepository)
    }
    
    @Test
    fun `invoke should return Success with countries list`() = runTest {
        // Arrange
        val countries = listOf(
            Country(code = "AR", name = "Argentina", ...),
            Country(code = "BR", name = "Brazil", ...)
        )
        fakeRepository.setResult(Result.Success(countries))
        
        // Act
        val result = useCase()
        
        // Assert
        assertTrue(result is Result.Success)
        assertEquals((result as Result.Success).value, countries)
    }
    
    @Test
    fun `invoke should return Failure on exception`() = runTest {
        // Arrange
        val exception = Exception("Network error")
        fakeRepository.setResult(Result.Failure(exception))
        
        // Act
        val result = useCase()
        
        // Assert
        assertTrue(result is Result.Failure)
        assertEquals((result as Result.Failure).exception.message, "Network error")
    }
}
```

**Requisitos mínimos por test:**
- ❌ Mock con Mockk ❌ (NO - crear Fakes simples)
- ✅ Happy path: verify comportamiento correcto
- ✅ Error path: verify manejo de excepciones
- ✅ Usar `runTest` de `kotlinx-coroutines-test`
- ✅ Setup/Teardown en @Before/@After
- ✅ Nombres descriptivos: `fun_should_when()`

**Cobertura requerida:**
- ✅ Líneas: ≥80%
- ✅ Branches: ≥75%
- ✅ TODOS los use cases con 2+ tests cada uno

---

### 1.5 Fake Implementations para Tests (CARPETA: `composeApp/src/desktopTest/kotlin/edu/dyds/trips/domain/util/`)

**ARCHIVO: `FakeCountriesRepository.kt`**
```kotlin
class FakeCountriesRepository : CountriesRepository {
    private var result: Result<List<Country>> = Result.Success(emptyList())
    
    fun setResult(result: Result<List<Country>>) {
        this.result = result
    }
    
    override suspend fun getCountries() = result
    override suspend fun searchCountries(query: String) = result
    override suspend fun getCountryByCode(code: String) = 
        result.mapCatching { it.find { c -> c.code == code } ?: throw Exception("Not found") }
}
```

**ARCHIVO: `FakeWeatherRepository.kt`** - Similar pattern
**ARCHIVO: `FakeTripsRepository.kt`** - Similar pattern

---

### 1.6 Validaciones (PERSONA 1 debe checkear ANTES de commit)

**Build:**
- [ ] Ejecutar: `./gradlew compileKotlinCommonMain` → SIN ERRORES
- [ ] Ejecutar: `./gradlew compileTestKotlinDesktop` → SIN ERRORES
- [ ] Ejecutar: `./gradlew desktopTest` → TODOS los tests PASSED
- [ ] Ejecutar: `./gradlew detekt` (lint) → 0 warnings

**Código:**
- [ ] Cero imports no usados (IDE cleanup imports)
- [ ] Cero variables sin usar
- [ ] Cero comentarios de debug
- [ ] Cero TODO comentarios
- [ ] Nombres: CamelCase para classes, SCREAMING_SNAKE_CASE para constants
- [ ] Todas las clases data son inmutables (val fields)
- [ ] Todas las interfaces no implementan métodos

**Tests:**
- [ ] Cobertura: `./gradlew testCoverageReport` → ≥80%
- [ ] Todos los tests pasan: ✅

---

### 1.7 Comandos Git exactos (PERSONA 1)

```bash
# PASO 1: Clonar y setup (1 vez)
git clone https://github.com/Beatles/DYDS26-Beatles.git
cd DYDS26-Beatles
git checkout develop  # Asegurar que develop existe

# PASO 2: Crear rama feature
git checkout -b feature/domain-layer

# PASO 3: Crear carpetas (cuando sea)
mkdir -p composeApp/src/commonMain/kotlin/edu/dyds/trips/domain/{entity,repository,usecase,util}
mkdir -p composeApp/src/desktopTest/kotlin/edu/dyds/trips/domain/{usecase,util}

# PASO 4: Crear archivos... (usar IDE)

# PASO 5: Commits incrementales
git add composeApp/src/commonMain/kotlin/edu/dyds/trips/domain/entity/
git commit -m "feat(domain): implement entities (Country, Trip, WeatherForecast, Currency)"

git add composeApp/src/commonMain/kotlin/edu/dyds/trips/domain/repository/
git commit -m "feat(domain): implement repository interfaces"

git add composeApp/src/commonMain/kotlin/edu/dyds/trips/domain/usecase/
git commit -m "feat(domain): implement use case interfaces and implementations"

git add composeApp/src/commonMain/kotlin/edu/dyds/trips/domain/util/
git commit -m "feat(domain): add domain Constants and utilities"

git add composeApp/src/desktopTest/kotlin/edu/dyds/trips/domain/
git commit -m "test(domain): add unit tests for use cases with ≥80% coverage"

# PASO 6: Push a remote
git push origin feature/domain-layer

# PASO 7: Crear PR en GitHub
# URL: https://github.com/Beatles/DYDS26-Beatles/pull/new/feature/domain-layer
# Title: "feat(domain): implement domain layer with entities, repositories and usecases"
# Body: 
# - ✅ 17 archivos creados (entidades, interfaces, use cases, tests)
# - ✅ Result<T> sealed class implementado
# - ✅ 8 interfaces de use cases + implementaciones
# - ✅ 3 interfaces de repositorios definidas
# - ✅ Tests: 16+ test cases, cobertura ≥80%
# - ✅ Build: ./gradlew clean build → SUCCESS
# - ✅ Lint: ./gradlew detekt → 0 warnings

# PASO 8: Esperar review + merge (compañeros)
```

---

### 1.8 Criterios de Aceptación (DEFINICIÓN DE "LISTO")

✅ **PERSONA 1 está LISTO cuando:**

1. **Archivos creados:** Exactamente 17 archivos en rutas especificadas
2. **No hay dependencias externas:** Domain SOLO imports de domain y stdlib
3. **Interfaces vs Implementaciones:** 
   - ✅ 3 interfaces de repositorio (puros contratos)
   - ✅ 8 interfaces de use case (puros contratos)
   - ✅ 8 implementaciones de use case (delegadores)
4. **Result<T> funcional:** 
   - ✅ Success<T> y Failure variantes
   - ✅ Extension functions: getOrNull, getOrThrow, getOrElse
5. **Compilación:** 
   - `./gradlew clean build` → ✅ BUILD SUCCESSFUL
6. **Tests:** 
   - `./gradlew desktopTest` → ✅ ALL TESTS PASSED
   - Cobertura: ≥80% (verificar en reporte)
7. **Lint:** 
   - `./gradlew detekt` → ✅ 0 warnings
8. **Git:** 
   - PR a `develop` con descripción completa
   - Commits con mensajes descriptivos
9. **Documentación interna:** 
   - Nada de comentarios en código
   - Nombres auto-documentan

---

### 1.9 Punto de Sincronización

**PERSONA 1 TERMINA → PERSONAS 2 y 3 PUEDEN EMPEZAR**

Acción:
```bash
git checkout develop
git pull origin develop  # Traer cambios de P1
# Ahora P2 y P3 tienen domain/ disponible en develop
```

---

---

## 🔌 PERSONA 2: DATA LAYER

*(Especificación igualmente detallada - versión resumida por espacio)*

### 2.1 Metadata
- **Rama:** `feature/data-layer`
- **Base:** `develop` (ESPERA que P1 esté mergeada ✅)
- **Prioridad:** 2️⃣ 
- **Duración:** 3-4 días
- **Inicia cuando:** P1 mergeada a `develop`
- **Termina cuando:** PR mergeada a `develop`

### 2.2 Archivos a crear - 24 ARCHIVOS TOTALES

**Carpeta base:** `composeApp/src/desktopMain/kotlin/edu/dyds/trips/data/`

```
data/
├── remote/
│   ├── countries/
│   │   ├── CountriesRemoteDataSource.kt (interface)
│   │   ├── CountriesRemoteDataSourceImpl.kt
│   │   ├── RestCountriesClient.kt (Ktor HttpClient wrapper)
│   │   └── RemoteCountryDTO.kt (@Serializable)
│   ├── weather/
│   │   ├── WeatherRemoteDataSource.kt (interface)
│   │   ├── WeatherRemoteDataSourceImpl.kt
│   │   ├── OpenMeteoClient.kt (Ktor HttpClient wrapper)
│   │   └── RemoteWeatherDTO.kt (@Serializable)
│   └── broker/
│       └── CountryWeatherBroker.kt (combina countries + weather)
├── local/
│   ├── TripsLocalDataSource.kt (interface)
│   ├── TripsLocalDataSourceImpl.kt
│   ├── TripsJsonPersistence.kt (maneja archivo JSON)
│   └── LocalTripDTO.kt (@Serializable)
├── mapper/
│   ├── CountryMapper.kt (extension: RemoteCountryDTO → Country)
│   ├── WeatherMapper.kt (extension: RemoteWeatherDTO → WeatherForecast)
│   └── TripMapper.kt (extension: LocalTripDTO ↔ Trip)
├── repository/
│   ├── CountriesRepositoryImpl.kt (implementa CountriesRepository)
│   ├── WeatherRepositoryImpl.kt (implementa WeatherRepository)
│   └── TripsRepositoryImpl.kt (implementa TripsRepository)
└── util/
    └── NetworkErrorHandler.kt (convierte excepciones HTTP a strings)
```

### 2.3 Dependencias (PERSONA 2 requiere)

**De PERSONA 1:**
- ✅ `composeApp/src/commonMain/.../domain/entity/` → todos los archivos
- ✅ `composeApp/src/commonMain/.../domain/repository/` → interfaces

**Imports exactos que usará:**
```kotlin
import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.WeatherForecast
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.domain.repository.CountriesRepository
import edu.dyds.trips.domain.repository.WeatherRepository
import edu.dyds.trips.domain.repository.TripsRepository
```

### 2.4 Tests requeridos - 12 ARCHIVOS DE TEST

**Carpeta:** `composeApp/src/desktopTest/kotlin/edu/dyds/trips/data/`

```
desktopTest/kotlin/edu/dyds/trips/data/
├── remote/
│   ├── countries/
│   │   ├── CountriesRemoteDataSourceTest.kt (MockEngine)
│   │   ├── RestCountriesClientTest.kt (MockEngine)
│   │   └── CountryMapperTest.kt
│   ├── weather/
│   │   ├── WeatherRemoteDataSourceTest.kt (MockEngine)
│   │   ├── OpenMeteoClientTest.kt (MockEngine)
│   │   └── WeatherMapperTest.kt
│   └── broker/
│       └── CountryWeatherBrokerTest.kt
├── local/
│   ├── TripsLocalDataSourceTest.kt
│   └── TripMapperTest.kt
└── repository/
    ├── CountriesRepositoryImplTest.kt
    ├── WeatherRepositoryImplTest.kt
    └── TripsRepositoryImplTest.kt
```

**Cobertura:** ≥80%

### 2.5 Validaciones (PERSONA 2)

```bash
./gradlew desktopTest  # TODOS PASSED
./gradlew testCoverageReport  # ≥80%
./gradlew detekt  # 0 warnings
```

### 2.6 Comandos Git (PERSONA 2)

```bash
# Setup
git checkout develop
git pull origin develop  # Traer cambios de P1

git checkout -b feature/data-layer

# Commits incrementales
git add composeApp/src/desktopMain/kotlin/edu/dyds/trips/data/remote/
git commit -m "feat(data): implement remote data sources (RestCountries, OpenMeteo clients)"

git add composeApp/src/desktopMain/kotlin/edu/dyds/trips/data/local/
git commit -m "feat(data): implement local data source with JSON persistence"

git add composeApp/src/desktopMain/kotlin/edu/dyds/trips/data/mapper/
git commit -m "feat(data): implement mappers (DTO → domain entity)"

git add composeApp/src/desktopMain/kotlin/edu/dyds/trips/data/broker/
git commit -m "feat(data): implement CountryWeatherBroker"

git add composeApp/src/desktopMain/kotlin/edu/dyds/trips/data/repository/
git commit -m "feat(data): implement repository implementations"

git add composeApp/src/desktopTest/kotlin/edu/dyds/trips/data/
git commit -m "test(data): add tests for data sources, mappers and repositories (≥80%)"

git push origin feature/data-layer

# PR a develop
```

### 2.7 Criterios de Aceptación (PERSONA 2)

✅ **LISTO cuando:**
1. 24 archivos data creados + 12 tests
2. NO imports de data en domain ❌
3. Clientes HTTP usan Ktor + `suspend`
4. DTOs con @Serializable, entidades sin ella
5. Mappers como extension functions
6. Broker combina repositorios sin lógica hardcoded
7. Implementaciones usan Result<T> de domain
8. Build + Tests ✅, Cobertura ≥80%

---

---

## 🎨 PERSONA 3: PRESENTATION LAYER

### 3.1 Metadata
- **Rama:** `feature/presentation-layer`
- **Base:** `develop` (ESPERA que P1 + P2 estén mergeadas ✅)
- **Prioridad:** 3️⃣
- **Duración:** 3-4 días
- **Inicia cuando:** P1 + P2 mergeadas a `develop`

### 3.2 Archivos a crear - 18 ARCHIVOS TOTALES + DI

**Carpeta base:** `composeApp/src/desktopMain/kotlin/edu/dyds/trips/`

```
presentation/
├── ui/
│   ├── state/
│   │   ├── ListState.kt (sealed class genérico)
│   │   └── DetailUiState.kt (data class)
│   └── theme/ (opcional - para Material Design)
├── home/
│   ├── HomeViewModel.kt
│   └── HomeScreen.kt (@Composable)
├── detail/
│   ├── DetailViewModel.kt
│   └── DetailScreen.kt (@Composable)
├── trips/
│   ├── TripsViewModel.kt
│   ├── TripsScreen.kt (@Composable)
│   └── TripEditDialog.kt (@Composable modal)
├── components/
│   ├── LoadingBox.kt (@Composable)
│   ├── ErrorBox.kt (@Composable)
│   ├── EmptyBox.kt (@Composable)
│   ├── CountryCard.kt (@Composable)
│   ├── TripCard.kt (@Composable)
│   ├── WeatherForecastWidget.kt (@Composable)
│   └── AppBar.kt (@Composable opcional)
├── Navigation.kt (archivo de rutas)
└── App.kt (@Composable root)

di/
└── TripsDependencyInjector.kt (singleton, factories)

main.kt (fun main())
```

### 3.3 Dependencias (PERSONA 3 requiere)

**De PERSONA 1 + PERSONA 2:**
- ✅ Todo `domain/`
- ✅ Todo `data/` (repositorios impl)

**Imports:**
```kotlin
import edu.dyds.trips.domain.entity.*
import edu.dyds.trips.domain.usecase.*
import edu.dyds.trips.data.repository.*
import edu.dyds.trips.data.di.*  // Si hay DI en data
```

### 3.4 Tests - 6 ARCHIVOS

**Carpeta:** `composeApp/src/desktopTest/kotlin/edu/dyds/trips/presentation/`

```
├── HomeViewModelTest.kt
├── DetailViewModelTest.kt
├── TripsViewModelTest.kt
├── NavigationTest.kt (rutas)
└── util/
    ├── FakeGetCountriesUseCase.kt
    ├── FakeGetCountryDetailsUseCase.kt
    └── FakeGetTripsUseCase.kt
```

**Cobertura:** ViewModels ≥80%

### 3.5 Validaciones (PERSONA 3)

```bash
./gradlew desktopTest  # ViewModels PASSED
./gradlew testCoverageReport  # ≥80%
./gradlew detekt  # 0 warnings
./gradlew :composeApp:desktopRun # Compila y corre
```

### 3.6 Criterios de Aceptación (PERSONA 3)

✅ **LISTO cuando:**
1. 18 archivos presentation + DI creados
2. UI States: ListState<T> sealed + DetailUiState data
3. 3 ViewModels: Home, Detail, Trips (StateFlow managers)
4. 3 Screens: Home, Detail, Trips (@Composable)
5. 6 componentes reutilizables (Card, Loading, Error, etc)
6. Navigation.kt con rutas nombradas: home, detail/{code}, trips
7. App.kt integra navegación
8. main.kt entry point funcional
9. DependencyInjector centralizado (object singleton)
10. Tests de ViewModels ≥80%
11. Build ✅, App corre sin crashes

---

---

## 📅 CRONOGRAMA SECUENCIAL

| Semana | Días | PERSONA 1 | PERSONA 2 | PERSONA 3 |
|--------|------|-----------|-----------|-----------|
| **1** | 1-3  | 🎯 DOMAIN (2-3 días) | - | - |
| **1** | 3-7  | Espera | 🔌 DATA (inicia día 3, OVERLAP) | - |
| **2** | 4-7  | Espera | 🔌 DATA (continúa) | - |
| **2** | 7-10 | Espera | 🔌 DATA termina | 🎨 PRESENTATION (inicia) |
| **2** | 10-12 | Espera | Espera | 🎨 PRESENTATION (termina) |
| **2** | 12-13 | ✅ QA | ✅ QA | ✅ QA |
| **3** | 13 | 🚀 FINAL | 🚀 FINAL | 🚀 FINAL |

**Total:** 2-2.5 semanas (10-13 días útiles)

---

## ✅ CHECKLIST FINAL (ANTES DE MERGEAR A MAIN)

### Todos los archivos presentes
- [ ] PERSONA 1: 17 archivos domain
- [ ] PERSONA 2: 24 archivos data
- [ ] PERSONA 3: 18 archivos presentation + DI + main

### Compilación
- [ ] `./gradlew clean build` → ✅ BUILD SUCCESSFUL
- [ ] SIN warnings del compilador
- [ ] SIN warnings de lint (`./gradlew detekt`)

### Tests
- [ ] `./gradlew desktopTest` → ✅ ALL TESTS PASSED
- [ ] Cobertura general ≥80%
- [ ] Reporte en `composeApp/build/reports/coverage/`

### Código
- [ ] Nombres expresivos (NO comentarios dedicados)
- [ ] Funciones ≤20 líneas
- [ ] Imports limpios (NO unused)
- [ ] Estructura SOLID validada

### Git
- [ ] Todos los commits en `develop`
- [ ] Commits con mensaje claro: `feat(...)`, `test(...)`
- [ ] Sin merge commits innecesarios

### Execution
- [ ] App corre: `./gradlew :composeApp:desktopRun` → ✅
- [ ] Sin crashes al navegar
- [ ] Pantallas responden a interacción

---

**Fin de especificación detallada de División de Trabajo.**




