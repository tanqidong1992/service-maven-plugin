@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_BASE_DIR=%DIRNAME%

"%APP_BASE_DIR%/jre/bin/java.exe" ^
<% if(has(javaRunOptions)) {%>
${javaRunOptions} ^
<%}%>
<% if(has(jvmMs)) {%>
-Xms${jvmMs} ^
<%}%>
<% if(has(jvmMs)) {%>
-Xmx${jvmMx} ^
<%}%>
-classpath "${classPath}" ${mainClass}