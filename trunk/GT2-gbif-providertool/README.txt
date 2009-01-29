           
           --------------------------------------------------
           
                THE INTEGRATED PUBLISHING TOOLKIT
                        GEOSERVER PLUGIN 
                    is hosted on google code
                please see website for documentation:
             http://code.google.com/p/gbif-providertoolkit/
           
           --------------------------------------------------
            
 
 
  *** DEVELOPER NOTES ***

SOURCE-CODE:
http://code.google.com/p/gbif-providertoolkit/source/browse/#svn/trunk/GT2-gbif-providertool                          


To run geoserver services with IPT data, we developed a geoserver plugin to access the non spatial H2 database. 
It requires GeoTools 2.5.1 which is used by geoserver 1.7.x
 
== build gt2 plugin ==
As it uses maven, you can build the jar like this:
$ mvn install

== Install plugin in existing geoserver ==
add the following 2 jars into your geoserver lib directory which is at geoserver/WEB-INF/lib:
 * h2-1.1.104.jar [http://code.google.com/p/h2database/downloads/detail?name=h2-2008-11-28.zip&can=2 download]
 * gt2-ipt-1.0.jar [http://gbif-providertoolkit.googlecode.com/files/gt2-ipt-1.4.jar download]
