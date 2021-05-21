

This section provides information about the source database types supported by the IPT

# Supported default databases
The IPT can use database connections to import data from tables or views. Currently the following databases are supported out of the box:
  * Microsoft SQL Server
  * MySQL
  * ODBC (Sun Java6)`**`
  * Oracle
  * PostgreSQL
  * Sybase databases

`**`Starting with Java SE 8, the JDBC-ODBC Bridge will no longer be included with the JDK. It is [recommended](https://blogs.oracle.com/Lance/entry/removal_of_the_jdbc_odbc) that you use a JDBC driver provided by the vendor of your database or a commercial JDBC Driver instead of the JDBC-ODBC Bridge.

# Adding new JDBC drivers
You can add your own jdbc drivers to the IPT if needed. For example if you prefer to use the Microsoft JDBC driver for SQL Server or want to add one for sqlite. The following steps assume you have a working IPT installed with an "exploded" war, i.e. you have a folder `ipt` (or however you named your instance) in your application servers webapps folder. The IPT needs to be stopped before you start adding a driver:

## Add jdbc driver jar to classpath
First get hold of the jar file of the driver you want to add, for example download the SQLite jar here:  
https://bitbucket.org/xerial/sqlite-jdbc/downloads

You need to copy this jar into the classpath of your webapp. The simplest is to copy it to the `ipt/WEB-INF/lib` directory.

## Modify jdbc.properties
In order for the IPT to understand which drivers are available and how to construct the jdbc url for it, we maintain a simple properties file with all the information. Open `ipt/WEB-INF/classes/jdbc.properties` and inspect the existing entries, for example for PostgreSQL:

```
# PostgreSQL driver
pgsql.title=PostgreSQL
pgsql.driver=org.postgresql.Driver
pgsql.url=jdbc:postgresql://{host}/{database}?compatible=7.2
pgsql.limitType=LIMIT
```

There are 4 properties that you need to add for each driver. All 4 have to start with the same prefix that you can freely choose without any further meaning:

  * **title**: The title to be displayed in the IPT for this driver
  * **driver**: The driver java class to be used when connecting
  * **url**: A template to build the url for connecting. There are 2 variables that can be used in the url string which will be replaced by the actual settings configured: **{host}** and **{database}**
  * **limitType**: How to limit the amount of data returned. Possible values are **LIMIT**, **TOP**, **ROWNUM**. This is driver specific. 

In the SQLite example above the driver connects to a file, so no {host} is used in the url template. Please add the following to the jdbc.properties to add the SQLite driver:

```
# SQLite driver
# uses files only, so {host} is ignored
# database example on win =C:/work/mydatabase.db
# database example on unix=/home/leo/work/mydatabase.db
sqlite.title=SQLite
sqlite.driver=org.sqlite.JDBC
sqlite.url=jdbc:sqlite:{database}
sqlite.limitType=LIMIT
```


Now you should be good to restart the IPT and use the new driver for mapping sqlite data sources.