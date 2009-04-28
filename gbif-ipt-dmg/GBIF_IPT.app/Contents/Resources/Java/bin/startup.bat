@echo off
REM -----------------------------------------------------------------------------
REM Start Script for GEOSERVER
REM
REM $Id: startup.bat,v 1.6 2004/08/24 17:37:53 cholmesny Exp $
REM -----------------------------------------------------------------------------

if "%JAVA_HOME%" == "" goto noJava1

if not exist "%JAVA_HOME%\bin\java.exe" goto noJava2

:doGeo
if "%GEOSERVER_HOME%" == "" goto noGeo1

if not exist "%GEOSERVER_HOME%\bin\startup.bat" goto noGeo2

if "%GEOSERVER_DATA_DIR%" == "" goto noDataDir

REM -------------
REM OK, we're ready to try actually runnning it.
REM -------------

goto run

REM Actions having to do with JAVA_HOME being defined
:noJava1
  echo The JAVA_HOME environment variable is not defined.
  echo Attempting to use current installed Java
goto doGeo

:noJava2
  echo The JAVA_HOME environment variable is defined, but 'java.exe'
  echo was not found there.
goto end

REM Actions if GEOSERVER_HOME isn't defined
:noGeo1
  if exist ..\start.jar goto doGeo1
  echo The GEOSERVER_HOME environment variable is not defined.
  echo This environment variable is needed to run this program.
goto end

:doGeo1
  echo GEOSERVER_HOME environment variable not found.  Using current
  echo directory.  Please set GEOSERVER_HOME for future uses.
goto setCurAsHome

:noGeo2
  if exist ..\start.jar goto doGeo2
  echo The GEOSERVER_HOME environment variable is not defined correctly.
  echo This environment variable is needed to run this program.
goto end

:doGeo2
  echo GEOSERVER_HOME environment variable not properly set.  Using parent
  echo directory of this script.  Please set GEOSERVER_HOME correctly for 
  echo future uses.
goto setCurAsHome

:setCurAsHome
  cd ..
  set GEOSERVER_HOME=.
goto run

:run
  if not exist "%GEOSERVER_DATA_DIR%" goto noDataDir
  goto execJava

REM if there's no GEOSERVER_DATA_DIR defined then use GEOSERVER_HOME/data_dir/
:noDataDir
  if exist "%GEOSERVER_HOME%\data_dir" goto setDataDir
  goto execJava

:setDataDir
  set GEOSERVER_DATA_DIR=%GEOSERVER_HOME%\data_dir
  goto execJava

:execJava
  if "%JAVA_HOME%" == "" goto usePathJava
  ::If it's not defined by now, then we are just using 'java', and it will 
  ::fail there if it can't find it.
  set RUN_JAVA=%JAVA_HOME%\bin\java
  goto runJava

:usePathJava
  ::A better way to do this is given at http://www.ericphelps.com/batch/samples/JavaRuntime.cmd.txt
  ::looking up the registry, but I think this should work too... 
  set JAVA_RUN=java
  goto runJava

:runJava
  cd %GEOSERVER_HOME%
  %RUN_JAVA% -DGEOSERVER_DATA_DIR="%GEOSERVER_DATA_DIR%" -jar %GEOSERVER_HOME%\start.jar


:end
 pause
