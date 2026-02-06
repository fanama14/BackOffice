@echo off
setlocal

set TOMCAT_HOME=C:\apache-tomcat-9.0.82\apache-tomcat-9.0.82
set APP_NAME=BackOffice

echo ========================================
echo  DEPLOIEMENT - BackOffice
echo ========================================
echo.

echo [1/5] Compilation du framework-core...
cd /d "%~dp0..\framework-core"
call mvn clean install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Echec de la compilation du framework-core
    pause
    exit /b 1
)
echo.

echo [2/5] Compilation et packaging du BackOffice...
cd /d "%~dp0"
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Echec de la compilation du BackOffice
    pause
    exit /b 1
)
echo.

echo [3/5] Arret de Tomcat...
call "%TOMCAT_HOME%\bin\shutdown.bat" 2>nul
timeout /t 3 /nobreak >nul
echo.

echo [4/5] Deploiement du WAR dans Tomcat...
if exist "%TOMCAT_HOME%\webapps\%APP_NAME%" (
    rmdir /S /Q "%TOMCAT_HOME%\webapps\%APP_NAME%"
)
if exist "%TOMCAT_HOME%\webapps\%APP_NAME%.war" (
    del /F /Q "%TOMCAT_HOME%\webapps\%APP_NAME%.war"
)
copy /Y "%~dp0target\backoffice-1.0-SNAPSHOT.war" "%TOMCAT_HOME%\webapps\%APP_NAME%.war"
echo.




pause
