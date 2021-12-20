@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_BASE_DIR=%DIRNAME%

<% if(has(withJre)) {%>
set JVM="%APP_BASE_DIR%\jre\bin\server\jvm.dll"
<%}else{%>
set JVM="%JAVA_HOME%\bin\server\jvm.dll"
@rem if "%JAVA_HOME%" == "" set JVM="jvm.dll"
<%}%>

prunsrv //IS//${serviceName} ^
 --Description "${serviceDescription}" ^
 --DisplayName="${serviceDisplayName}" ^
 <% if(has(startup)) {%>
 --Startup=${startup} ^
 <% } %>
 --Jvm="%JVM%" ^
 --StartPath="%~dp0\" ^
 --StopPath="%~dp0\" ^
 --StartMode=jvm ^
 --StopMode=jvm ^
 --LogPath="%~dp0\logs" ^
 --LogLevel=Debug ^
 <% if(has(jvmMs)) {%>
  --JvmMs=${jvmMs} ^
 <% } %>
 <% if(has(jvmMx)) {%>
   --JvmMx=${jvmMx} ^
 <% } %>
 --Classpath="${classPath}" ^
 --StartClass=${mainClass} ^
 --StopClass=${mainClass} ^
 --StartMethod=${startMethod} ^
 --StopMethod=${stopMethod} ^

 
 