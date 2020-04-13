serviceName="A"
serviceDescription="A sample service."
AppRoot="/a/b/c"
transformedAppRoot=$(echo "${AppRoot}" | sed "s/\//\\\\\//g")
echo "transformed app root :${transformedAppRoot}"
sed "s/{{serviceDescription}}/${serviceDescription}/g;s/{{serviceName}}/${serviceName}/g;s/{{AppRoot}}/${transformedAppRoot}/g" sample.service  > ${serviceName}.service
cat ${serviceName}.service
echo