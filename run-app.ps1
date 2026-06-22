# ============================================================
# Script para ejecutar la app con la API key cargada correctamente
# ============================================================

# Leer la API key del registro de Windows
$apiKey = [Environment]::GetEnvironmentVariable('REST_COUNTRIES_API_KEY', 'User')

if (-not $apiKey) {
    Write-Host "❌ ERROR: REST_COUNTRIES_API_KEY no encontrada en el registro" -ForegroundColor Red
    Write-Host ""
    Write-Host "Por favor, configura la variable primero:" -ForegroundColor Yellow
    Write-Host 'setx REST_COUNTRIES_API_KEY "tu_api_key_aqui"' -ForegroundColor Cyan
    Write-Host ""
    exit 1
}

# Cargar la variable en la sesión actual
$env:REST_COUNTRIES_API_KEY = $apiKey

Write-Host "✅ API key cargada: $($apiKey.Substring(0, 20))..." -ForegroundColor Green
Write-Host "🚀 Iniciando aplicación..." -ForegroundColor Cyan
Write-Host ""

# Ejecutar la app con Gradle
.\gradlew.bat run --console=plain

