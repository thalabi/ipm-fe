set liquibase=java -jar C:\maven-repo\org\liquibase\liquibase-core\3.5.5\liquibase-core-3.5.5.jar
set changeLogFile=C:\git-repo\FlightLogServer\FlightLogServer\src\main\resources\liquibase\changelog-master.xml
set url=jdbc:oracle:thin:@kerneldc.com:1521:meridian
set driver=oracle.jdbc.OracleDriver
set liquibaseClasspath=C:\git-repo\FlightLogServer\FlightLogServer\src\main\resources;C:\maven-repo\com\oracle\jdbc\ojdbc8\12.2.0.1\ojdbc8-12.2.0.1.jar

%liquibase% --changeLogFile=%changeLogFile% --username=flightlogv3 --password=flightlogv3 --url=%url% --driver=%driver% --classpath=%liquibaseClasspath% rollbackCount 5