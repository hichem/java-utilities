## OpenLDAP Installation
~~~
sudo yum -y install openldap-servers openldap-clients
sudo systemctl enable slapd.service
sudo systemctl start slapd.service
~~~

## OpenLDAP Configuration

* Generate root password using slappasswd (for example use password: mypassword)

~~~
slappasswd
New password: 
Re-enter new password: 
{SSHA}rrA2NeQ25zUgu+gAnMyjraEy8or9jCW+
~~~

* Create Configuration of OpenLDAP Database. Create a file ldap_db.ldif with the following content:
(for example domain: local, subdomain: example)

~~~
dn: olcDatabase={2}hdb,cn=config
changetype: modify
replace: olcSuffix
olcSuffix: dc=test,dc=example,dc=local
 
dn: olcDatabase={2}hdb,cn=config
changetype: modify
replace: olcRootDN
olcRootDN: cn=ldapadm,dc=test,dc=example,dc=local
 
dn: olcDatabase={2}hdb,cn=config
changetype: modify
replace: olcRootPW
olcRootPW: {SSHA}rrA2NeQ25zUgu+gAnMyjraEy8or9jCW+
~~~

* Send configuration to OpenLDAP by running command

~~~
sudo ldapmodify -Y EXTERNAL  -H ldapi:/// -f ldap_db.ldif
~~~

* Create OpenLDAP monitor configuration file ldap_monitor.ldif with the following content:

~~~
dn: olcDatabase={1}monitor,cn=config
changetype: modify
replace: olcAccess
olcAccess: {0}to * by dn.base="gidNumber=0+uidNumber=0,cn=peercred,cn=external, cn=auth" read by dn.base="cn=lda
padm,dc=test,dc=example,dc=local" read by * none
~~~

* Send monitor configuration to LDAP by running command

~~~
sudo ldapmodify -Y EXTERNAL  -H ldapi:/// -f ldap_monitor.ldif
~~~

* Create OpenLDAP key and certificates to be used with SSL mode. Use openssl to create the certificate and key. Create these files in folder /etc/openldap/certs

~~~
sudo openssl req -new -x509 -nodes -out /etc/openldap/certs/ldapcert.pem -keyout /etc/openldap/certs/ldapkey.pem -days 3650
~~~

* Give OpenLDAP ownership on the key and certificate files

~~~
sudo chown -R ldap:ldap /etc/openldap/certs/*.pem
~~~

* Prepare certificate configuration file ldap_certs.ldif with the following content:

~~~
dn: cn=config
changetype: modify
replace: olcTLSCertificateFile
olcTLSCertificateFile: /etc/openldap/certs/ldapcert.pem
 
dn: cn=config
changetype: modify
replace: olcTLSCertificateKeyFile
olcTLSCertificateKeyFile: /etc/openldap/certs/ldapkey.pem
~~~

* Send certificate configuration to LDAP

~~~
sudo ldapmodify -Y EXTERNAL  -H ldapi:/// -f ldap_certs.ldif
~~~

* Create OpenLDAP database from sample database configuration that comes with ldap installation

~~~
sudo cp /usr/share/openldap-servers/DB_CONFIG.example /var/lib/ldap/DB_CONFIG
sudo chown -R ldap:ldap /var/lib/ldap/
~~~

* Add required schemas to ldap

~~~
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/cosine.ldif
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/nis.ldif 
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/inetorgperson.ldif
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/inetorgperson.ldif
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/java.ldif
~~~

* Create LDAP directory structure file ldap_base.ldif for the domain we created
(the LDAP admin will be named ldapadm)

~~~
dn: dc=test,dc=example,dc=local
dc: test
objectClass: top
objectClass: domain
 
dn: cn=ldapadm,dc=test,dc=example,dc=local
objectClass: organizationalRole
cn: ldapadm
description: LDAP Manager
 
dn: ou=People,dc=test,dc=example,dc=local
objectClass: organizationalUnit
ou: People
 
dn: ou=Group,dc=test,dc=example,dc=local
objectClass: organizationalUnit
ou: Group
~~~

* Send configuration to LDAP

~~~
sudo ldapadd -x -W -D "cn=ldapadm,dc=test,dc=example,dc=local" -f ldap_base.ldif
~~~

* Enable SSL by editing file /etc/sysconfig/slapd. Change SLAPD_URLS value as following:

~~~
SLAPD_URLS="ldapi:/// ldap:/// ldaps:///"
~~~

* Restart LDAP

~~~
sudo systemctl restart slapd
~~~

## Test Read / Write / Delete

### Read
Simple Local Search Query. The query requires providing the ldap admin password

~~~
ldapsearch -D "cn=ldapadm,dc=test,dc=example,dc=local" -W -p 389 -h localhost -b "dc=test,dc=example,dc=local"
~~~

### Simple Write Entry in People Group

Create an LDIF file with following content (name it add_people.ldif for example)
~~~
dn: uid=john.doe,ou=People,dc=test,dc=example,dc=local
changetype: add
objectClass: top
objectClass: person
objectClass: organizationalPerson
objectClass: inetOrgPerson
uid: john.doe
givenName: John
sn: Doe
cn: John Doe
mail: john.doe@example.com
userPassword: password
~~~

Write the new entry to ldap using the following command (provide the admin password)
~~~
ldapmodify -a -D "cn=ldapadm,dc=test,dc=example,dc=local" -W -p 389 -h localhost -f add_people.ldif
~~~

### Delete Entry

~~~
ldapdelete -D "cn=ldapadm,dc=test,dc=example,dc=local" -W -h localhost:389 "cn=toto,dc=test,dc=example,dc=com"
~~~
