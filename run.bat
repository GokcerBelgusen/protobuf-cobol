@echo off
rem ---------------------------------------------------------------------------
rem Translates Protocol buffers proto files to COBOL
rem ------------------------------------------------
rem type run -h to get help on available options
rem protoc.exe must be on the path (see http://code.google.com/p/protobuf/)
rem set JAVA_HOME to point to a JDK (not a JRE)
rem ---------------------------------------------------------------------------

if not "%JAVA_HOME%" == "" goto gotJdkHome
echo JAVA_HOME must be set to point to a JDK (not a JRE)
goto exit

:gotJdkHome
set PROTOCOB_CMD_LINE_ARGS=

:setupArgs
if %1a==a goto doneStart
set PROTOCOB_CMD_LINE_ARGS=%PROTOCOB_CMD_LINE_ARGS% %1
shift
goto setupArgs

:doneStart
rem Use the following to set your own JVM arguments
set JVM_ARGS=

rem Update the log4j configuration to set debug mode
set JVM_ARGS=%JVM_ARGS% -Dlog4j.configuration=file:conf/log4j.properties

java %JVM_ARGS% -cp %JAVA_HOME%/lib/tools.jar;protocob.jar com.legstar.protobuf.cobol.ProtoCobolMain %PROTOCOB_CMD_LINE_ARGS%
goto end

:exit
exit /b 1

:end
exit /b 0
