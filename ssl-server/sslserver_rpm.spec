Name:		sslserver		
Version:	1.0	
Release:	1
Summary:	SSL Server	

Group:		Applications/Java
Vendor:		
Packager:	
License:	Free
URL:		
#Source0:	

#BuildRequires:	
#Requires:	
BuildRoot: 	${builddir}/%{name}-root
Prefix:		/usr/local/sslserver


BuildArchitectures:	noarch

%description
SSL Server Java App

Requires(pre): /usr/sbin/useradd, /usr/bin/getent
Requires(postun): /usr/sbin/userdel


%pre
/usr/bin/getent group sslserver || /usr/sbin/groupadd -r sslserver
/usr/bin/getent passwd sslserver || /usr/sbin/useradd -r -g sslserver -d /usr/local/sslserver -s /sbin/nologin sslserver

%postun
/usr/sbin/userdel sslserver


#%prep
#%setup -q


%build
pwd
rm -rf %{_builddir}/*
cp -R %{_sourcedir}/* %{_builddir}

%install
pwd
rm -rf %{buildroot}
mkdir -p %{buildroot}/usr/local/sslserver
mkdir -p %{buildroot}/usr/local/sslserver/config
mkdir -p %{buildroot}/etc/systemd/system
mkdir -p %{buildroot}/var/log/sslserver
cd %{_builddir}
cp sslserver.jar 						%{buildroot}/usr/local/sslserver
cp keystore.jks 						%{buildroot}/usr/local/sslserver
cp truststore.jks 						%{buildroot}/usr/local/sslserver
cp TransactionManager.config 			%{buildroot}/usr/local/sslserver/config
cp log4j.properties 					%{buildroot}/usr/local/sslserver/config
cp startServer.sh 						%{buildroot}/usr/local/sslserver
cp stopServer.sh						%{buildroot}/usr/local/sslserver
cp sslserver.service 					%{buildroot}/etc/systemd/system

%files
%defattr(644,sslserver,sslserver,644)
%dir /usr/local/sslserver
%dir /var/log/sslserver
/usr/local/sslserver/sslserver.jar
%config /usr/local/sslserver/keystore.jks
%config /usr/local/sslserver/truststore.jks
%config /usr/local/sslserver/config/server.config
%config /usr/local/sslserver/config/log4j.properties
/etc/systemd/system/sslserver.service
%attr(755,sslserver,sslserver) /usr/local/sslserver/startServer.sh
%attr(755,sslserver,sslserver) /usr/local/sslserver/stopServer.sh

%doc



%changelog
