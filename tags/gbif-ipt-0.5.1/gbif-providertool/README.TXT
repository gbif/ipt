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

The embedded H2 database can be accessed in 2 ways if the IPT is running:
a) Manual HTML console at http://localhost:8080/h2 
b) JDBC via the included H2 tcp server. Make sure you are using H2 1.1.x.jar. Use the following Spring bean or connect otherwise:
    <bean id="dataSource" class="org.h2.jdbcx.JdbcDataSource">
        <property name="URL" value="jdbc:h2:${dataDir}/db/ipt;auto_server=true"/>
        <property name="user" value="${jdbc.username}"/>
        <property name="password" value="${jdbc.password}"/>
    </bean>





####################
# VERSION HISTORY
####################
0.6

0.5.1
- uses XStream for EML metadata persistency
- bug fixes in config UI

0.5
- uses embedded H2 database
- geoserver plugin updated for geotools 2.5 and H2
- geoserver feature types are updated by the IPT when occurrence resources are changed
- new manager config UI
- extension entities visualised
- metadata browse mockup
- resource type property added to metadata
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

