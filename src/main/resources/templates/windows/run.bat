@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_BASE_DIR=%DIRNAME%
set CMD_LINE_ARGS=%*

<% if(has(withJre)) {%>
set JAVA="%APP_BASE_DIR%\jre\bin\java.exe"
<%}else{%>
set JAVA="%JAVA_HOME%\bin\java.exe"
if "%JAVA_HOME%" == "" set JAVA="java.exe"
<%}%>

"%JAVA%" ^
<% if(has(jvmFlags)) {%>
${jvmFlags} ^
<%}%>
<% if(has(jvmMs)) {%>
-Xms${jvmMs} ^
<%}%>
<% if(has(jvmMs)) {%>
-Xmx${jvmMx} ^
<%}%>
-classpath "${classPath}" ${mainClass} ^
<% if(has(args)) {%>
${args} ^
<%}%>
%CMD_LINE_ARGS%