# Install Tomcat 6 on Windows 7

_Tomcat 6 and Windows 7 are obsolete, please refer to [[TomcatInstallationWindows8]] instead._

## Table of Contents
+ [[Introduction|TomcatInstallationWindows7.wiki#introduction]]
+ [[Prerequisites|TomcatInstallationWindows7.wiki#prerequisites]]
+ [[Steps|TomcatInstallationWindows7.wiki#steps]]
  + [[1. Download Tomcat 6.x|TomcatInstallationWindows7.wiki#1-download-tomcat-6x]]
  + [[2. Install Tomcat 6.x|TomcatInstallationWindows7.wiki#2-install-tomcat-6x]]
  + [[3. Run Tomcat|TomcatInstallationWindows7.wiki#3-run-tomcat]]
  + [[4. Test Tomcat|TomcatInstallationWindows7.wiki#4-test-tomcat]]
  + [[5. Shut down Tomcat|TomcatInstallationWindows7.wiki#5-shut-down-tomcat]]
  + [[6. References|TomcatInstallationWindows7.wiki#6-references]]

## Introduction
This tutorial explains how to install the Apache Tomcat Java Servlet 6 on Windows 7. This document is not tested to work with other versions of Tomcat. For complete details, consult the specific documentation for those software installations.

## Prerequisites
Please make sure the following conditions (software and version) are met before you continue.

1) The client version of Windows 7. This tutorial is written based on the 32-bit version of Windows 7 Professional.

2) The latest security upgrades.

3) JAVA 6 Framework installed. Please refer to http://java.com/en/download/ to install JRE. This page usually will detect your operating system and gives hints to the correct version to download. Assuming Java is installed in c:\\Program Files\Java\jre6.

4) Logged in as an administrator.

## Steps
### 1. Download Tomcat 6.x
Download the latest stable Tomcat 6 Binary Distribution Core (32-bit/64-bit Windows Service Installer) release from http://tomcat.apache.org/download-60.cgi. This should put a file of the form apache-tomcat-6.x.x.exe(or apache-tomcat-6.x.x.tar if you download with Safari) into your Downloads folder.

### 2. Install Tomcat 6.x
Double click the executable to start installation wizard. **Do** fill information to specify **the connector port**, **Tomcat administrator login credentials** without changing the Roles. If you forget to provide the credential information, you'll have to edit the [tomcat\_home](tomcat_home.md)/conf/tomcat-users.xml file and restart Tomcat to flush the privileges.

### 3. Run Tomcat
Go to c:\\Program Files\Apache Software Foundation\Tomcat 6.0\bin
Double click the **tomcat6** application.

You should see a terminal window showing logs about starting up Tomcat. When you see

```
INFO: Server startup in xxxx ms
```

That means the Tomcat is ready.

You can also run the **tomcat6w** application by right-clicking it and choose "run as administrator". A service setting pane will be brought up, and you can decide you'd like to run Tomcat for once or make it start up automatically every time you boot Windows 7.

### 4. Test Tomcat
If Tomcat is running successfully following step 5, above, you should be able to see the Tomcat Welcome page at the following URL:

http://localhost:8080/

With the admin credentials you entered during the setup wizard, you can access:

http://localhost:8080/manager/html

Where you can upload the ipt.war file to install IPT.

### 5. Shut down Tomcat
Close the terminal windows will shut down Tomcat.

Or, if you brought up Tomcat by using tomcat6w, stop Tomcat by clicking the stop button.

### 6. References
For more thorough descriptions of Tomcat configuration, also consult these sources:

  * Tomcat documentation: http://tomcat.apache.org/tomcat-6.0-doc/index.html