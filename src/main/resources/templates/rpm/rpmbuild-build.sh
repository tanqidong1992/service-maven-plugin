#!/bin/bash
cp ${serviceName}.spec ~/rpmbuild/SPECS
cp -r ${serviceName} ~/rpmbuild/SOURCES
rpmbuild -ba ~/rpmbuild/SPECS/${serviceName}.spec


