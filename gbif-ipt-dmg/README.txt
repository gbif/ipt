*** How to build a DMG for a standalone Mac webapp ***

1) build the console.jar
http://code.google.com/p/gbif-providertoolkit/source/browse/trunk/gbif-ipt-console/


2) include all resources in the GBIF_IPT.app (a jetty server)
http://code.google.com/p/gbif-providertoolkit/source/browse/trunk/gbif-ipt-dmg
- replace Contents/Resources/Java/console.jar with specific one from above
- update Contents/Resources/Info.plist
- copy your exploded webapp(s) (not war) into the Contents/Resources/Java/webapps folder 
 
 
3) build the DMG using "DMG Canvas"
http://www.araelium.com/dmgcanvas/
- copy the above app into the dmg
- replace background images incl application name. Photoshop template dmg.psd included 
- build DMG
