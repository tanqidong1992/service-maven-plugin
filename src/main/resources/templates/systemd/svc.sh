#!/bin/bash

function requireRunAsRoot(){
  if [ ! $(id -u) -eq 0 ]; then
      echo "The script should run as root!"
      exit 1
  fi
}

if [ -e "/etc/redhat-release" ]; then
    installationPath="/usr/lib/systemd/system"
else
    installationPath="/etc/systemd/system"
fi

scriptFilePath=$(readlink -f "$0")
BASE_DIR=$(dirname ${scriptFilePath})
WORK_DIR="${BASE_DIR}"
source ${BASE_DIR}/env.sh
function start(){
    echo "Start service!"
    if [ -f  "${WORK_DIR}/${serviceName}.pid" ]; then
        pid=$(cat "${WORK_DIR}/${serviceName}.pid")
        if [ -e "/proc/${pid}" ]; then
            echo "The service is started!"
            return 0
        fi
    fi
    nohup ${WORK_DIR}/run-foreground.sh >>/dev/null 2>&1 &
    if [ $? -eq 0 ]; then
        echo "$!" > ${WORK_DIR}/${serviceName}.pid
        return 0
    else
        echo "Start service failed!"
        return 1
    fi
}
function stop(){
    echo "Stop service"
    if [ -f  "${WORK_DIR}/${serviceName}.pid" ]; then
        pid=$(cat "${WORK_DIR}/${serviceName}.pid")
        if [ -e "/proc/${pid}" ]; then
            echo "Start to stop the service!"
            kill -9 ${pid}
            return 0
        fi
    fi
    echo "The service is not started!"
}
function generateServiceUnitFile(){

    if [ -z "$1" ]; then
        transformedAppRoot=$(echo "${BASE_DIR}" | sed "s/\//\\\\\//g")
    else   
        transformedAppRoot=$(echo "${1}" | sed "s/\//\\\\\//g") 
    fi

    echo "transformed app root :${transformedAppRoot}"
    sed "s/{{wantedBy}}/${wantedBy}/g;s/{{after}}/${after}/g;s/{{serviceDescription}}/${serviceDescription}/g;s/{{serviceName}}/${serviceName}/g;s/{{AppRoot}}/${transformedAppRoot}/g" ${BASE_DIR}/sample.service  > ${BASE_DIR}/${serviceName}.service
    cat ${BASE_DIR}/${serviceName}.service
    echo
}
function installService(){
    echo "Install service"
    if [ -e "${installationPath}/${serviceName}.service" ]; then
        echo "The service is already installed!"
    else
        generateServiceUnitFile
        mv "${WORK_DIR}/${serviceName}.service" "${installationPath}/${serviceName}.service"
        systemctl daemon-reload
        systemctl enable ${serviceName}.service
    fi
    
}
function startService(){
    echo "Start service"
    sudo systemctl start ${serviceName}.service
}
function stopService(){
    echo "Stop service"
    sudo systemctl stop ${serviceName}.service
}
function uninstallService(){
    echo "Uninstall service!"
    stopService
    sudo systemctl disable ${serviceName}.service
    if [ -e "${installationPath}/${serviceName}.service" ]; then
        rm "${installationPath}/${serviceName}.service"
    else
        echo "Failed to delete service unit file:${installationPath}/${serviceName}.service"
    fi
    systemctl daemon-reload
     
}
function displayServiceStatus(){
    sudo systemctl status ${serviceName}.service
}
op=$1
case "$op" in
  doStart)
      start
  ;;
  doStop)
      stop
  ;;
  start)
      requireRunAsRoot
      startService
  ;;
  stop)
      requireRunAsRoot
      stopService
  ;;
  status)
    requireRunAsRoot
    displayServiceStatus
  ;;
  install)
      requireRunAsRoot
      installService
  ;;
  uninstall)
      requireRunAsRoot
      uninstallService
  ;;
  guf)
      generateServiceUnitFile $2
  ;;
  *)
  echo 
  echo "Usage:"
  echo "./svc.sh [install,start,stop,status,uninstall]"
  echo "Commands:"
  echo "  install: Install ${serviceName} service"
  echo "  start: Manually start ${serviceName} service"
  echo "  stop: Manually stop ${serviceName} service"
  echo "  status: Display status of ${serviceName} service"
  echo "  uninstall: Uninstall ${serviceName} service"
  echo 
  ;;
esac
exit 0
