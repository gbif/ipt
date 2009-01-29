           
           --------------------------------------------------
           
                THE INTEGRATED PUBLISHING TOOLKIT 
                    is hosted on google code
                please see website for documentation:
             http://code.google.com/p/gbif-providertoolkit/
           
           --------------------------------------------------
            
 
 
 
  *** DEVELOPER NOTES ***
  
If you want to install, configure or just use the IPT, please see the google code wiki for documentation.
The following notes are for developers only. You have been warned!

  

= Build from source =
The build process is using Maven and by default the IPT should run with a single command after you have checked out the sourcecode.
Most parameters are kept as variable definitions at the bottom of the Maven pom.xml file which can be found in the root folder of the project.

== ipt.properties ==
In order to load from the database though we need the absolute path to the H2 database files first, so the IPT dataDir can be configured in the ipt.properties. 
If not existing the default will be the data dir inside the webapp root folder.

http://yourdomain/ipt/h2


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


== Run IPT via Jetty ==
Maven can run the IPT in a local java webserver called Jetty.
This is useful for testing and getting the software to run quickly, but is not recommended for production.
To start the IPT via the jetty server simply run the following maven command from the projects root folder:
mvn -P prod

You should then be able to access the IPT at:
http://localhost:8080


== Build WAR and deploy it to Tomcat ==
The following maven command builds the war archive which then can be found in the target folder:
mvn -P prod install




= IPT settings =
IPT settings are stored in the H2 database (table provider_cfg) and can be configured through the admin webinterface of the IPT.
Settings to be configured include:
- provider metadata
- geoserver location and login
- GoogleMaps key

The IPT settings are stored in the database through the class ProviderCfg which is embedded in AppConfig,
a singleton that is injected into most other spring beans.



= Geoserver =
To run geoserver services with IPT data, we developed a geoserver plugin to access the non spatial H2 database. 
It requires GeoTools 2.5.1 which is used by geoserver 1.7.x
 
The source code is hosted at:
http://code.google.com/p/gbif-providertoolkit/source/browse/#svn/trunk/GT2-gbif-providertool

== build gt2 plugin ==
As it uses maven, you can build the jar like this:
$ mvn install

== Install plugin in existing geoserver ==
add the following 2 jars into your geoserver lib directory which is at geoserver/WEB-INF/lib:
 * h2-1.1.104.jar [http://code.google.com/p/h2database/downloads/detail?name=h2-2008-11-28.zip&can=2 download]
 * gt2-ipt-1.0.jar [http://gbif-providertoolkit.googlecode.com/files/gt2-ipt-1.4.jar download]
