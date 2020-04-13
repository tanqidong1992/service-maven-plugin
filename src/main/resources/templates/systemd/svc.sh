#!/bin/bash

if [ ! $(id -u) -eq 0 ]; then
    echo "The script should run as root!"
    exit 1
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
    nohup ${WORK_DIR}/run-foreground.sh &
    if [ $? -eq 0 ]; then
        echo "$!" > ${WORK_DIR}/${serviceName}.pid
        return 0
    else
        echo "Start service fialed!"
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
    transformedAppRoot=$(echo "${BASE_DIR}" | sed "s/\//\\\\\//g")
    echo "transformed app root :${transformedAppRoot}"
    sed "s/{{serviceDescription}}/${serviceDescription}/g;s/{{serviceName}}/${serviceName}/g;s/{{AppRoot}}/${transformedAppRoot}/g" sample.service  > ${serviceName}.service
    cat ${serviceName}.service
    echo
}
function installService(){
    echo "Install service"
    if [ -e "/etc/systemd/system/${serviceName}.service" ]; then
        echo "The service is already installed!"
    else
        generateServiceUnitFile
        mv "${WORK_DIR}/${serviceName}.service" "/etc/systemd/system/${serviceName}.service"
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
    echo "Uninstall serevice!"
    stopService
    sudo systemctl disable ${serviceName}.service
    if [ -e "/etc/systemd/system/${serviceName}.service" ]; then
        rm "/etc/systemd/system/${serviceName}.service"
    else
        echo "Failed to delete service unit file:/etc/systemd/system/${serviceName}.service"
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
      startService
  ;;
  stop)
      stopService
  ;;
  status)
    displayServiceStatus
  ;;
  install)
      installService
  ;;
  uninstall)
      uninstallService
  ;;
  *)
  echo 
  echo "Usage:"
  echo "./svc.sh [install,start,stop,status,uninstall]"
  echo "Commands:"
  echo "  install: Install the service"
  echo "  start: Manually start the service"
  echo "  stop: Manually stop the service"
  echo "  status: Display status of the service"
  echo "  uninstall: Uninstall the service"
  echo 
  ;;
esac
exit 0