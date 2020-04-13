#!/bin/bash
./jre/bin/java \
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