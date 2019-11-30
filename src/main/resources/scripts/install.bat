prunsrv //IS//${serviceName} ^
 --Description "${serviceDescription}" ^
 --DisplayName="${serviceDisplayName}" ^
 --Startup=auto ^
 --Jvm=".\jre\bin\server\jvm.dll" ^
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

 
 