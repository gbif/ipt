
# How to Contribute

## Introduction
The core development of the IPT is directed by GBIF, but the coding is a community effort and everyone is welcome to join. Java or HTML/jQuery developers are very welcome to contribute code patches.

## Source Code
Please be aware the IPT uses the following core frameworks:
  * [Struts2](http://struts.apache.org/2.x/index.html)
  * [Google Guice](http://code.google.com/p/google-guice/)
  * [Freemarker](http://freemarker.sourceforge.net/docs/) templating
  * [JQuery](http://jquery.com/) for javascript and ajax

## Getting the source, Maven & Eclipse
We use Maven extensively to manage dependencies and the build process.
To checkout the source code and setup an eclipse project simply do the following (it will create an eclipse project folder _gbif-ipt_ in your current dir):
```
svn checkout http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt
cd gbif-ipt
mvn eclipse:eclipse
mvn
```

The command mvn eclipse:eclipse should have created the project artefacts for eclipse so you can import this folder into your eclipse editor if that is what you are using.

The last command starts up the IPT via the Jetty plugin on port 8080.
You should be able to see the IPT running by opening the address http://localhost:8080 in your browser.