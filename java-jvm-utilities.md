## JVM Server Optimizations
~~~
java -server -Xss128k -Xms8g -Xmx8g -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -jar program.jar
~~~

## Remote Debug Java Program
~~~
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=y -server -Xss128k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -jar program.jar
~~~

## Enable JMX Monitoring
~~~
java 	-Dcom.sun.management.jmxremote.port=5000 \
		-Dcom.sun.management.jmxremote=true \
		-Dcom.sun.management.jmxremote.authenticate=false \
		-Dcom.sun.management.jmxremote.ssl=false
		-server -Xss128k -Xms8g -Xmx8g -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -jar program.jar
~~~