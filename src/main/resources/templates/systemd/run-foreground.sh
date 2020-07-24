#!/bin/bash

scriptFilePath=$(readlink -f "$0")
APP_BASE_DIR=$(dirname \${scriptFilePath})

\${APP_BASE_DIR}/jre/bin/java \
<% if(has(javaRunOptions)) {%>
${javaRunOptions} \
<%}%>
<% if(has(jvmMs)) {%>
-Xms${jvmMs} \
<%}%>
<% if(has(jvmMx)) {%>
-Xmx${jvmMx} \
<%}%>
-classpath ${classPath} ${mainClass}