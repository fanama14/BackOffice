@echo off
setlocal

set TOMCAT_HOME=C:\xampp\tomcat
set APP_NAME=BackOffice

echo ========================================
echo  DEPLOIEMENT - BackOffice
echo ========================================
echo.

echo [1/5] Verification du framework...
cd /d "%~dp0"
if not exist "lib\framework-java-1.0.0.jar" (
    echo ERREUR: framework-java-1.0.0.jar non trouve dans lib/
    pause
    exit /b 1
)
REM Installation du framework dans le depot Maven local si necessaire
call mvn install:install-file -Dfile="lib/framework-java-1.0.0.jar" -DgroupId=mg.framework -DartifactId=framework-java -Dversion=1.0.0 -Dpackaging=jar -q
echo Framework OK.
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
