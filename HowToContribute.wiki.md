# How to Contribute

## Table of Contents
+ [[Introduction|HowToContribute.wiki#introduction]]
+ [[Source Code Dependencies|HowToContribute.wiki#source-code-dependencies]]
+ [[Getting the source code|HowToContribute.wiki#getting-the-source-code]]
+ [[Opening the source code in Eclipse|HowToContribute.wiki#opening-the-source-code-in-eclipse]]
+ [[Opening the source code in IntelliJ|HowToContribute.wiki#opening-the-source-code-in-intellij]]
+ [[Running the application in Jetty|HowToContribute.wiki#running-the-application-in-jetty]]

## Introduction
The core development of the IPT is directed by GBIF, but the coding is a community effort and everyone is welcome to join. Java or HTML/jQuery developers are very welcome to contribute code patches. Patches can be submitted by creating a [pull request](https://help.github.com/articles/creating-a-pull-request/) using a branch or fork of the repository. 

## Source Code Dependencies
We use Maven extensively to manage dependencies and the build process. Please be aware the IPT uses the following core frameworks:
  * [Struts2](http://struts.apache.org/2.x/index.html)
  * [Google Guice](http://code.google.com/p/google-guice/)
  * [Freemarker](http://freemarker.sourceforge.net/docs/) templating
  * [JQuery](http://jquery.com/) for javascript and ajax

## Getting the source code

Use Git to checkout the latest version of the code:

<code>$ git clone https://github.com/gbif/ipt.git </code><br>
<code>$ cd ipt </code><br>

## Opening the source code in Eclipse

After checking out the source code, you can open the source code in Eclipse by setting up an Eclipse project. To do so, run the following maven commands:

<code>$ mvn eclipse:eclipse </code><br>

The command mvn eclipse:eclipse should have created the project artifacts for eclipse so you can import this folder into your Eclipse editor.

## Opening the source code in IntelliJ

After checking out the source code, you can open the project in IntelliJ by simply opening the IPT directory in Intellij. 

## Running the application in Jetty

<code>$ mvn -Dmaven.test.skip=true </code><br>

This command starts up the IPT via the Jetty plugin on port 8080. You should be able to see the IPT running by opening http://localhost:8080 in your web browser.