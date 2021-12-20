Name: ${serviceName}
Version: ${_projectVersion}
Release: 1%{?dist}

Summary: ${serviceDescription}
<% if(has(_projectUrl)) {%>
URL: ${_projectUrl}
 <% } %>
 <% if(has(_projectLicense)) {%>
License: ${_projectLicense}
 <% } %>
Source0: ${serviceName}
<% if(has(withJre)) {%>
%global __requires_exclude_from ^.*/%{name}/jre/.*$
%global __provides_exclude_from ^.*/%{name}/jre/.*$
 <% } %>
%description
${serviceDescription}

%install
mkdir -p %{buildroot}/opt
cp -r %{SOURCE0} %{buildroot}/opt

%files
/opt/%{name}

%changelog

%post
sudo chmod 776 /opt/%{name} -R
sudo /opt/%{name}/svc.sh install
sudo systemctl start %{name}

%preun
sudo systemctl stop %{name}
sudo /opt/%{name}/svc.sh uninstall
