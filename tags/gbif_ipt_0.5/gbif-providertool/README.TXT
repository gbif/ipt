# Configuration
Most user managed configuration is stored in the database through the class ProviderCfg which is embedded in AppConfig.
In order to load from the database though we need the absolute path to the H2 database files first, so the IPT dataDir can be configured in the ipt.properties. 
If not existing the default will be the data dir inside the webapp root folder.

Values are propagated like this

pom.xml 
 -->
application.properties
ipt.properties
+ WebCtxPropertyResolver for ${dataDir}+${webappDir}
 --> 
ApplicationContext.xml
ApplicationContext-resources.xml

The problem with this is that the WebCtxPropertyResolver needs to be created before any other bean, so you cannot inject other beans.
But to resolve the webapp dir (and in turn the default datadir) we need a servlet context. Which is fine in production, but for tests we need a mock.
Which unfortunately doesnt get created and therefore injected before this bean is created. Buh.
 


# H2
The embedded H2 database can be accessed in 3 ways if the IPT is running:
a) JDBC in embedded mode if running in the same JVM as the IPT (i.e. tomcat)
b) JDBC via the included H2 tcp server. The basedir is pointing to the datadir folder, so no databases outside can be accessed through this server
c) HTML console at http://localhost:8080/h2 