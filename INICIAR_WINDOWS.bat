@echo off
cd /d "%~dp0"
echo Iniciando SVB-GUA JavaFX mediante Maven...
where mvn >nul 2>nul
if errorlevel 1 (
  echo No se encontro Maven en PATH. Abre el proyecto desde Apache NetBeans 30 y ejecutalo desde alli.
  pause
  exit /b 1
)
mvn clean javafx:run
pause
