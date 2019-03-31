## Generate CA Certs

Use the following commands to generate:
 - A 4096 byte length RSA key pair for CA
 - Auto-signed certificate with 10 years validity

~~~
openssl genrsa -des3 -out ca.key 4096
openssl req -new -x509 -days 3650 -key ca.key -out ca.crt 
~~~

## Generate Client Certs

Use the following commands to generate:
 - A 4096 bit long RSA key pair for Client
 - A certificate signing request for client in order to sign its certificate by our CA
 - A Client certificate valid for 10 years signed by our CA

~~~
openssl genrsa -des3 -out client.key 4096
openssl req -new -key client.key -out client.csr
openssl x509 -req -days 365 -in client.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out client.crt 
~~~

Remove the passphrase from the client’s key using the following command:

~~~
copy client.key client.key.old
openssl rsa -in client.key.old -out client.key
~~~

## Generate Server Keys

Use the following commands to generate:
 - A 4096 bit long RSA key pair for Server
 - A certificate signing request for server in order to sign its certificate by our CA
 - A Server certificate valid for 10 years signed by our CA

~~~ 
openssl genrsa -des3 -out server.key 4096
openssl req -new -key server.key -out server.csr
openssl x509 -req -days 365 -in server.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out server.crt
~~~

Remove the passphrase from the server’s key using the following command:

~~~
copy server.key server.key.old
openssl rsa -in server.key.old -out server.key
~~~

## Generate PKCS12 & JKS Containers for Server

Generate the PKCS12 container in PFX format using the server key and certificate:

~~~
openssl pkcs12 -export -in server.crt -inkey server.key -name ServerKey -out server.pfx
~~~

Generate the java key store using Keytool (JDK):

~~~
keytool.exe -importkeystore -srckeystore server.pfx -destkeystore server.jks -srcstoretype pkcs12 -deststoretype JKS -alias ServerKey -destalias ServerKey -deststorepass somepassword -destkeypass somepassword
~~~
