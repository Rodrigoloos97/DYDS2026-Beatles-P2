# DYDS2026-Beatles-P2

Asistente de viajes basado en `PLAN_ASISTENTE_VIAJES_V2.md`.

Estado actual implementado:
- Domain layer (entidades, repositorios, use cases)
- Data layer de paises y clima (clientes HTTP, data sources, mappers, repositorios)
- Presentacion desktop funcional (una sola ventana con rutas):
  - `home`: listado + busqueda de paises
  - `detail`: datos del pais + pronostico + guardar viaje
  - `trips`: listado de viajes guardados + editar + eliminar
- Tests de APIs remotas con `MockEngine`:
  - `RestCountriesClientTest`
  - `OpenMeteoClientTest`

## Requisitos

- JDK 21

## Ejecutar app desktop (UI)

```powershell
Set-Location "C:\Users\agost\IdeaProjects\DYDS2026-Beatles-P2"
.\gradlew.bat run --console=plain
```

## Ejecutar tests

```powershell
Set-Location "C:\Users\agost\IdeaProjects\DYDS2026-Beatles-P2"
.\gradlew.bat test --console=plain
```

## Estructura del Proyecto

- **Domain Layer**: Entidades (Country, Trip, etc), repositorios (interfaces) y use cases
- **Data Layer**: Clientes HTTP, data sources remotas y locales, repositorios implementados, mappers
- **Presentation Layer**: ViewModels, estados UI, pantallas (navegación en una sola ventana)

## Características

- Listado de paises con búsqueda
- Vista de detalle de país con pronóstico del clima
- Gestión de viajes (crear, editar, eliminar)
- Persistencia local en JSON
- Validación de fechas (yyyy-MM-dd) en inputs
- Tests de integración con MockEngine

## Flujo de Uso

1. **Home**: Busca y visualiza lista de países
2. **Detail**: Selecciona un país, ve su clima y dataos, guarda un viaje
3. **Trips**: Visualiza, edita y elimina tus viajes guardados
