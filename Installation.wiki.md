The IPT consists of 2 webapplications that run separately in a Java 1.5 container.
The default installation of the IPT is using the compiled ipt.war and a geoserver.zip archive available in the download section. Alternatively you can build the IPT from source which is described at the bottom of this page (only recommended for Java developers).

  * [Install Application Server](#Application_Server.md)
  * [Install IPT](#IPT.md)
  * [Install Geoserver](#Geoserver.md)



---

# Application Server
The IPT should run in any compliant java  application server using Java 1.5. We have tested it with Jetty and [Tomcat 5.5](http://tomcat.apache.org/download-55.cgi), which we recommend for the installation. The IPT requires quite some memory, as it also runs a full database. So make sure your application server has enough memory available and potentially increase the memory to 1 gig or more (Tomcat on [OSX](http://explanatorygap.net/2005/03/06/configuring-tomcat-under-os-x-server-for-more-memory/), [general](http://wiki.apache.org/tomcat/FAQ/Memory)).


---

# IPT
In most application servers like Tomcat it is enough to simply _rename the file into ipt.war_ and then _drop the [ipt.war](http://code.google.com/p/gbif-providertoolkit/downloads/list?can=3&q=ipt+war&colspec=Filename+Summary+Uploaded+Size+DownloadCount) file into the webapp directory_ of your application server. You might need to restart the server. Consult your application server manual how to deploy war archives in detail. After the server has restarted you should find a new folder called `ipt` inside your webapps directory, which we will refer to as `%IPT` in the rest of this document.

In case you don't have any application server we recommend Tomcat which you can download from here:
http://www.apache.org/tomcat



### IPT settings
Nearly all configuration is done through the [admin webinterface](UserManual#Settings.md) and stored in IPTs cache database. There is one exception to this - that is where the IPT database is actually located!

The default data directory of the IPT is inside the applications webapp and comes with 2 sample resources preconfigured. If you want to keep your data somewhere different (for easier backup for example), you can change one line in `%IPT/WEB-INF/classes/ipt.properties`:
```
dataDir=/wherever/you/want/data
```

This change requires a restart of the IPT of course (restarting or reloading tomcat)
You should now be able to see the IPT in your browser. For a default tomcat installation this would be:

`http://localhost:8080/ipt/`

### Web-based configuration
As you might have noticed a lot of images are broken. To fix this, you need to configure some basic settings in the web admin interface, especially the base URL of your server. Go to:

`http://localhost:8080/ipt/admin/settings.html`

Login as the admin (default is user=admin, password=admin) and update all settings you can find on this page, described in detail at [UserManual#Settings](UserManual#Settings.md)



---


# Geoserver
To run IPT' geoservices we have developed a small plugin for [Geoserver](http://geoserver.org) that allows it to access the IPT cache. We provide a [geoserver archive](http://code.google.com/p/gbif-providertoolkit/downloads/list?can=2&q=geoserver+zip&colspec=Filename+Summary+Uploaded+Size+DownloadCount) that comes bundled with the plugin, but you should also be able to install the plugin in your own geoserver installation. It was build for [geoserver 1.7.2](http://geoserver.org/display/GEOS/GeoServer+1.7.2) but should also work with later versions. The source code is hosted in the [SVN](http://code.google.com/p/gbif-providertoolkit/source/browse/#svn/trunk/GT2-gbif-providertool) and the plugin is available as a jar in the downloads.

To install the prepared IPT geoserver in your application server, please download the zipped archive and uncompress it straight in Tomcats webapps folder. A Tomcat restart might be needed. You should now be able to access geoserver at:
`http://localhost:8080/geoserver/`

## Install plugin in existing geoserver
This step is only necessary if you havent use the geoserver archive that we provide for the IPT. In case you wanna use your existing geoserver installation,
add the following 2 jars into your geoserver lib directory which is at `geoserver/WEB-INF/lib`:
  * h2-1.1.104.jar [download](http://code.google.com/p/h2database/downloads/detail?name=h2-2008-11-28.zip&can=2)
  * gt2-ipt-1.0beta.jar [download](http://code.google.com/p/gbif-providertoolkit/downloads/list?can=2&q=geoserver+plugin&colspec=Filename+Summary+Uploaded+Size+DownloadCount)

## Configure Geoserver
The only configuration that geoserver needs to be aware of, it where your IPTs data directory is located. By default geoserver tries to find the IPT data directory in Tomcat/webapps/ipt/data. If you have a different directory, please adjust this setting as follows:

Log into geoservers config interface as the admin `http://localhost:8080/geoserver/config/index.do`. The default credentials are user=admin, password=geoserver.
Go to the `Feature Data Set Configuration` (Menu: Config/Data/Datastores), select IPT and edit the `datadir` so it points to your data directory. Then save and apply the configuration (upper corner left).

### Configure GeoWebCache
The IPT makes use of GeoWebCache (GWC), a plugin for geoserver that caches WMS images which increases performance a lot. As with IPT, GWC needs to know the URL for geoserver. By default it assumes you run geoserver under http://localhost:8080/geoserver

If you need to change that, please update geoservers web.xml settings, in particular the following parameter:
```
<context-param>
    <param-name>GEOSERVER_WMS_URL</param-name>
    <param-value>http://EXAMPLE.COM:PORT/PATH/wms?request=GetCapabilities</param-value>
</context-param>
```

General configuration help for the geoserver bundled version can be found here:
http://geoserver.org/display/GEOSDOC/5.+GWC+-+GeoWebCache


Thats it.



---


# Building IPT from source
The build process is using Maven and by default the IPT should run with a single command after you have checked out the sourcecode. Most parameters are kept as variable definitions at the bottom of the Maven pom.xml file which can be found in the root folder of the project.

## Run IPT via Jetty
Maven can run the IPT in a local java webserver called Jetty.
This is useful for testing and getting the software to run quickly, but is not recommended for production.
To start the IPT via the jetty server simply run the following maven command from the projects root folder:

```
mvn -P prod
```

You should then be able to access the IPT at:
http://localhost:8080


## Build WAR and deploy it to Tomcat
The following maven command builds the war archive which then can be found in the target folder:
```
mvn -P prod install
```

Please follow the regular IPT installation guide for further instructions.


### Developer details
#### POM.xml
Values are propagated like this

pom.xml
> -->
application.properties
ipt.properties
+ WebCtxPropertyResolver for ${dataDir}+${webappDir}
> -->
ApplicationContext.xml
ApplicationContext-resources.xml

The problem with this is that the WebCtxPropertyResolver needs to be created before any other bean, so you cannot inject other beans.
But to resolve the webapp dir (and in turn the default datadir) we need a servlet context. Which is fine in production, but for tests we need a mock.
Which unfortunately doesnt get created and therefore injected before this bean is created. Buh.

#### IPT settings and H2
The IPT settings are stored in the database through the class ProviderCfg which is embedded in AppConfig,
a singleton that is injected into most other spring beans.