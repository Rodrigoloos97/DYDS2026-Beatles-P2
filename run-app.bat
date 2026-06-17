@echo off
REM ============================================================
REM Script para ejecutar la app con la API key cargada correctamente
REM ============================================================

setlocal enabledelayedexpansion

REM Obtener la API key del registro de Windows
for /f "tokens=3" %%i in ('reg query "HKEY_CURRENT_USER\Environment" /v REST_COUNTRIES_API_KEY 2^>nul') do set API_KEY=%%i

if "!API_KEY!"=="" (
    echo.
    echo ❌ ERROR: REST_COUNTRIES_API_KEY no encontrada en el registro
    echo.
    echo Por favor, ejecuta primero en PowerShell:
    echo   setx REST_COUNTRIES_API_KEY "tu_api_key_aqui"
    echo.
    pause
    exit /b 1
)

REM Cargar la variable en la sesión actual
set REST_COUNTRIES_API_KEY=!API_KEY!

echo.
echo ✅ API key cargada correctamente
echo 🚀 Iniciando aplicación...
echo.

REM Ejecutar la app con Gradle
call gradlew.bat run --console=plain

pause

