####################
# CONFIGURATION
####################
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
 


####################
# H2 DATA CACHE
####################

The embedded H2 database can be accessed in 3 ways if the IPT is running:
a) JDBC in embedded mode if running in the same JVM as the IPT (i.e. tomcat)
b) JDBC via the included H2 tcp server. The basedir is pointing to the datadir folder, so no databases outside can be accessed through this server
c) HTML console at http://localhost:8080/h2 





####################
# VERSION HISTORY
####################
0.5
- uses embedded H2 database
- geoserver plugin updated for geotools 2.5 and H2
- geoserver feature types are updated by the IPT when occurrence resources are changed
- new manager config UI
- extension entities visualised
- metadata browse mockup
- taxon & region trees open corresponding node in taxon & region overview page

0.4
- google maps integrated
- simple EML metadata forms
- eml output

0.3
- occurrence webapp added
- taxon & region trees implemented
- rss feeds

0.2 
- nested set created for taxonomy & region hierarchy
- upload/import working

