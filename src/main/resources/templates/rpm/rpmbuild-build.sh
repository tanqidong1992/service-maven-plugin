#!/bin/bash

if [ ! -d ~/rpmbuild ]; then
  rpmdev-setuptree
  if [ ! $? -eq 0 ] ; then
      echo "Please install rpmdevtools!" 1>&2
  fi
fi
if [ -f  ~/rpmbuild/SPECS/${serviceName}.spec ];then
  rm ~/rpmbuild/SPECS/${serviceName}.spec
fi
cp ${serviceName}.spec ~/rpmbuild/SPECS
if [ -d  ~/rpmbuild/SOURCES/${serviceName} ];then
  rm ~/rpmbuild/SOURCES/${serviceName} -r
fi
cp -r ${serviceName} ~/rpmbuild/SOURCES
~/rpmbuild/SOURCES/${serviceName}/svc.sh guf /opt/${serviceName}
rm ~/rpmbuild/SOURCES/${serviceName}/svc.sh
rm ~/rpmbuild/SOURCES/${serviceName}/sample.service
cp svc-rpm.sh ~/rpmbuild/SOURCES/${serviceName}/svc.sh
rpmbuild -ba ~/rpmbuild/SPECS/${serviceName}.spec




