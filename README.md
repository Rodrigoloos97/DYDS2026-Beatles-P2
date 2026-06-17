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
- **API key personal de Rest Countries** (requerida para usar listado de países remoto)

## Configuración de API (Rest Countries) - Setup Rápido

La app usa API remota para obtener el listado completo de países (~250 países).  
**Cada usuario necesita su propia API key** (gratuita, límites generosos).

### 1. Generar tu API key (2 minutos)

1. Ve a `https://restcountries.com/api-keys`
2. Crea una cuenta (o ingresa si ya tienes)
3. Copia tu **API key** desde el dashboard
4. Guárdala segura (no la compartas públicamente)

### 2. Configurar en tu máquina

#### **Windows (PowerShell)**
```powershell
setx REST_COUNTRIES_API_KEY "TU_API_KEY_AQUI"
setx REST_COUNTRIES_BASE_URL "https://api.restcountries.com/countries/v5"
```
Luego cierra y abre terminal/IDE para que apliquen.

#### **macOS / Linux (Bash)**
```bash
echo 'export REST_COUNTRIES_API_KEY="TU_API_KEY_AQUI"' >> ~/.bashrc
echo 'export REST_COUNTRIES_BASE_URL="https://api.restcountries.com/countries/v5"' >> ~/.bashrc
source ~/.bashrc
```
O en tu shell favorito (`.zshrc`, etc.).

#### **Para esta sesión (sin persistencia)**
```powershell
# Windows
$env:REST_COUNTRIES_API_KEY = "TU_API_KEY_AQUI"
$env:REST_COUNTRIES_BASE_URL = "https://api.restcountries.com/countries/v5"

# macOS/Linux
export REST_COUNTRIES_API_KEY="TU_API_KEY_AQUI"
export REST_COUNTRIES_BASE_URL="https://api.restcountries.com/countries/v5"
```

### 3. Ejecutar la app

```powershell
Set-Location "C:\usuarios\tuusuario\ruta\al\proyecto"
.\gradlew.bat run --console=plain
```

Si ves "Error: Not found" → la key no está configurada. Repite paso 2.

## Ejecutar tests

```powershell
Set-Location "C:\usuarios\tuusuario\ruta\al\proyecto"
.\gradlew.bat test --console=plain
```

## Estructura del Proyecto

- **Domain Layer**: Entidades (Country, Trip, etc), repositorios (interfaces) y use cases
- **Data Layer**: Clientes HTTP, data sources remotas y locales, repositorios implementados, mappers
- **Presentation Layer**: ViewModels, estados UI, pantallas (navegación en una sola ventana)

## Características

- Listado de ~250 paises con búsqueda remota en vivo
- Vista de detalle de país con pronóstico del clima
- Gestión de viajes (crear, editar, eliminar)
- Persistencia local en JSON
- Validación de fechas (yyyy-MM-dd) en inputs
- Tests de integración con MockEngine
- Paginación remota automática

## Cache de Paises

- En la primera carga, la app consulta Rest Countries y guarda cache local en `app_data/countries_cache.json`.
- A partir de ahi, listado y busqueda de paises se resuelven desde cache (sin nuevas llamadas a la API de paises).
- Otros servicios (por ejemplo clima) siguen consultando remoto normalmente.
- Si quieres refrescar paises desde API, elimina `app_data/countries_cache.json` y vuelve a abrir la app.

## Flujo de Uso

1. **Home**: Busca y visualiza lista de países desde API remota
2. **Detail**: Selecciona un país, ve su clima, guarda un viaje
3. **Trips**: Visualiza, edita y elimina tus viajes guardados

## FAQ

**P: ¿Puedo usar la app sin API key?**  
R: No. La key es obligatoria para el listado remoto. Sin ella verás error explícito.

**P: ¿La key se puede compartir?**  
R: No. Cada cuenta tiene límites mensuales. Si la compartes públicamente, la agota cualquiera.

**P: ¿Qué pasa si no configuro `REST_COUNTRIES_BASE_URL`?**  
R: La app usa por defecto `https://api.restcountries.com/countries/v5`, así que funciona normalmente.

**P: ¿Mi key se guarda en el código?**  
R: No. Se guarda como variable de entorno en tu máquina (seguro, local, no sincroniza al repo).
