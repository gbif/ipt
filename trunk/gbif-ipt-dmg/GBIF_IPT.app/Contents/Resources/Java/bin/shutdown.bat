@echo off
REM -----------------------------------------------------------------------------
REM Start Script for GEOSERVER
REM
REM $Id: shutdown.bat,v 1.6 2004/09/08 17:32:20 cholmesny Exp $
REM -----------------------------------------------------------------------------

if "%JAVA_HOME%" == "" goto noJava1

if not exist "%JAVA_HOME%\bin\java.exe" goto noJava2

if "%GEOSERVER_HOME%" == "" goto noGeo1

if not exist "%GEOSERVER_HOME%\start.jar" goto noGeo2

REM -------------
REM OK, we're ready to try actually runnning it.
REM -------------

java -jar "%GEOSERVER_HOME%\start.jar" --stop

goto end

:noJava1
  echo The JAVA_HOME environment variable is not defined.
  echo This environment variable is needed to run this program.
goto end

:noJava2
  echo The JAVA_HOME environment variable is defined, but 'java.exe'
  echo was not found there.
goto end

:noGeo1
  if exist stop.jar goto doGeo1
  echo The GEOSERVER_HOME environment variable is not defined.
  echo This environment variable is needed to run this program.
goto end

:doGeo1
echo GEOSERVER_HOME environment variable not found.  Using current
echo directory.  Please set GEOSERVER_HOME for future uses.
 java -jar start.jar --stop
 goto end

:noGeo2
  if exist stop.jar goto doGeo2
  echo The GEOSERVER_HOME environment variable is not defined correctly.
  echo This environment variable is needed to run this program.
goto end

:doGeo2
  echo GEOSERVER_HOME environment variable not properly set.  Using parent
  echo directory of this script.  Please set GEOSERVER_HOME correctly for 
  echo future uses.
  java -jar start.jar --stop
goto end

:end
