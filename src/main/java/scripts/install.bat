prunsrv //IS//${serviceName} ^
 --DisplayName="${serviceDisplayName}" ^
 --Startup=auto ^
 --Jvm=".\jre\bin\server\jvm.dll" ^
 --StartPath="%~dp0\" ^
 --StopPath="%~dp0\" ^
 --StartMode=jvm ^
 --StopMode=jvm ^
 --LogPath="%~dp0\logs" ^
 --LogLevel=Debug ^
 --Classpath="${classPath}" ^
 --StartClass=${mainClass} ^
 --StopClass=${mainClass} ^
 --StartMethod=${startMethod} ^
 --StopMethod=${stopMethod} ^
 --JvmMs=128m ^
 --JvmMx=1024m
 
 