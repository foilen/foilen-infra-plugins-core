database.driverClassName=org.mariadb.jdbc.Driver
database.url=jdbc:mariadb://127.0.0.1:3306/${dbName}
database.username=${dbUser}
database.password=${dbPass}

vendorAdapter.database=MYSQL

openjpa.streaming=false

datasource.testOnBorrow=true
datasource.validationQueryTimeoutSec=2
datasource.validationQuery=select 1
