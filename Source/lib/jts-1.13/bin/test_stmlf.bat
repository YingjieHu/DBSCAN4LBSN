@echo off
rem A batch file to run a XML test files

set CLASSPATH=
for %%i in (..\lib\*.*) do (
 set jarfile=%%i

 rem If we append to a variable inside the for, only the last entry will
 rem be kept. So append to the variable outside the for.
 rem See http://www.experts-exchange.com/Operating_Systems/MSDOS/Q_20561701.html.
 rem [Jon Aquino]

 call :setclass
)  

java com.vividsolutions.jtstest.testrunner.TopologyTestApp -Files ..\testxml\stmlf -geomop com.vividsolutions.jtstest.geomop.OverlayValidatedGeometryOperation
pause

goto :eof

:setclass
set CLASSPATH=%jarfile%;%CLASSPATH%
set jarfile=

:eof