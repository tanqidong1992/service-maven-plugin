"./jre/bin/java.exe" ^
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