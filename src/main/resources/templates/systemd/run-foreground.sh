#!/bin/bash

scriptFilePath=$(readlink -f "$0")
APP_BASE_DIR=$(dirname \${scriptFilePath})
CMD_LINE_ARGS=$@

<% if(has(withJre)) {%>
JAVA=\${APP_BASE_DIR}/jre/bin/java
<%}else{%>
if [ -z "\${JAVA_HOME}" ]; then
    JAVA="java"
else
    JAVA="\${JAVA_HOME}/bin/java"
fi
<%}%>

$JAVA \
<% if(has(javaRunOptions)) {%>
${javaRunOptions} \
<%}%>
<% if(has(jvmMs)) {%>
-Xms${jvmMs} \
<%}%>
<% if(has(jvmMx)) {%>
-Xmx${jvmMx} \
<%}%>
-classpath ${classPath} ${mainClass} \${CMD_LINE_ARGS}