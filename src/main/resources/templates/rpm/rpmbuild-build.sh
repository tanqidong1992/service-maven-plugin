#!/bin/bash

if [ ! -d ~/rpmbuild ]; then
  rpmdev-setuptree
  if [ ! $? -eq 0 ] ; then
      echo "Please install rpmdevtools!" 1>&2
  fi
fi

cp ${serviceName}.spec ~/rpmbuild/SPECS
cp -r ${serviceName} ~/rpmbuild/SOURCES
rpmbuild -ba ~/rpmbuild/SPECS/${serviceName}.spec


