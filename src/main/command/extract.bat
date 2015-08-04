@SETLOCAL
@echo off
REM extract.bat

REM Resolve the directory of pldoc
set pldir=%~d0%~p0

REM Set bootclasspath.
REM NB: this is needed to make JDK1.4 use our Xerces version instead of internal parser
@REM set bcp=%pldir%xalan\bin\xalan.jar;%pldir%xalan\bin\xml-apis.jar;%pldir%xalan\bin\xercesImpl.jar
REM Set classpath
REM NB: classpath must contain the pldoc directory to be able to locate .xsl files
set cp=%CLASSPATH%;%pldir%\target\${project.build.finalName}-jar-with-dependencies.jar  

rem Include Oracle jars in the classpath 
if "%ORACLE_HOME%" == "" ( echo ERROR: Environment variable ORACLE_HOME not set. 1>&2 && exit /b 1 )
@REM normal oracle home 
set cp=%cp%;%ORACLE_HOME%\jdbc\lib\ojdbc7.jar;%ORACLE_HOME%\jdbc\lib\ojdbc6.jar;%ORACLE_HOME%\jdbc\lib\ojdbc5.jar;%ORACLE_HOME%\jdbc\lib\ojdbc14.jar;%ORACLE_HOME%\jdbc\lib\classes12.jar
@REM Oracle Instanc Client Home
set cp=%cp%;%ORACLE_HOME%\ojdbc7.jar;%ORACLE_HOME%\ojdbc6.jar;%ORACLE_HOME%\ojdbc5.jar;%ORACLE_HOME%\ojdbc14.jar;%ORACLE_HOME%\classes12.jar


java -server -cp "%cp%" ${exec.mainClass} %*

ENDLOCAL
