# Install Tomcat on Mac OS X

*Apple no longer sell servers, or server software for Apple computers. This guide is probably only useful for IPT developers using a Mac.*

# Table of Contents

This tutorial explains how to install the Apache Tomcat 6.0.x on Mac OS X 10.5 or 10.6. This document is not tested to work with other versions of Tomcat or Java. For complete details, please consult the specific documentation for those software installations.

This tutorial is for now 'partially' tested with Tomcat 7.

## Prerequisites
Following are the conditions assumed to be true in order to follow this tutorial.

1) The client version of OS X 10.5.8 Leopard or OS X 10.6 Snow Leopard. If you're running Server version of Mac OS X 10.5 or 10.6, Tomcat is pre-installed.

2) The latest security upgrades.

3) JAVA 5 or JAVA 6 Framework installed. See [this FAQ](http://docs.oracle.com/javase/7/docs/webnotes/install/mac/mac-install-faq.html) for Java installation on Mac OS X.

4) Logged in as an administrator.

## Steps
### 1) Download Tomcat 6.x
Download the latest stable Tomcat 6 Binary Distribution Core (tar.gz) release from http://tomcat.apache.org/download-60.cgi. This should put a file of the form apache-tomcat-6.x.x.tar.gz (or apache-tomcat-6.x.x.tar if you download with Safari) into your Downloads folder.

### 2) Install Tomcat 6.x
Open the Terminal application to get a command prompt. The commands that follow assume that the Bourne Again SHell (bash) is in use. You can find out which shell you are using by typing the following and then hitting the ENTER key in the Terminal's command prompt:
```
echo $SHELL
```

All versions of OS X later than 10.3 use bash as the default shell. If the result echo command does not end in "/bash", you can change the default shell by using the System Preferences Accounts pane. If the pane is locked, unlock it. Control Click on your account name and a contextual menu will appear. Click on Advanced Options. You will then be presented with a dialog where you can change the default login shell to whatever you want. Select "bin/bash".

Change into the Library directory:
```
cd /Library
```

Create the Tomcat directory:
```
mkdir Tomcat
```

Set the owner of the Tomcat directory, where username should be the login name under which Tomcat will run:
```
chown username Tomcat
```

Set the group for the Tomcat directory to admin:
```
chgrp admin Tomcat
```

Change into the newly created Tomcat directory:
```
cd Tomcat
```

If the downloaded file was not already unzipped, unpack and unzip it into the Tomcat directory:
```
tar -xvzf ~/Downloads/apache-tomcat-6.x.x.tar.gz
```

Otherwise unpack the tar file:
```
tar -xvf ~/Downloads/apache-tomcat-6.0.x.tar
```

Create a Home symbolic link that points to the Tomcat directory:
```
ln -sfhv apache-tomcat-6.x.x Home
```

### 3) Edit the Tomcat Configuration
You will need to add a name and password to the tomcat-users.xml configuration file to access the Tomcat management and administration programs. Execute the following commands in Terminal:

Change into the Tomcat configuration directory:
```
cd Home/conf
```

Edit the tomcat-users.xml file. This example shows the command to edit using nano:
```
nano tomcat-users.xml
```

In the file, add the two lines below into the file above the line that says `</tomcat-users>` and outside of any comments (delimited with `<!--` and `-->`). Substitute the name you want as the admin's username for "admin" and enter a password for that user to log in to the Tomcat Manager in place of "password".

```
<role rolename="manager"/>
<user username="admin" password="password" roles="standard,manager,admin"/>
```

If you're setting up Tomcat 7, the role is defined differently:

```
<user username="admin" password="password" roles="manager-gui"/>
```

In Tomcat 7, role names are automatically created.

Save the tomcat-users.xml file and exit from the editor.

### 4) Run Tomcat
Execute the following commands in Terminal:
Change into the directory where Tomcat startup scripts are located
```
cd ../bin
```

Remove all of the scripts ending with `.bat`.
```
rm *.bat
```

Execute the Tomcat startup script. Please note at this point you should have JDK or JRE installed as Java is required by Tomcat, which is then required by IPT.
```
./startup.sh
```

Check the Tomcat error log for errors:
```
less ../logs/catalina.out
```

If there are no error messages in the log files, then the installation has been completed successfully and Tomcat is running. There should be an informational message similar to the following near the end of the log file:
```
INFO: Server startup in 2412 ms
```

Under some circumstances the startup scripts do not execute because the execute permission has not been set. If this is the case you can change the execute permission to the scripts by typing the following:
```
cd ../bin
chmod 750 *.sh
```

This signifies read, write, and execute permissions for the owner, read and execute permissions for the group, and no permissions for others.

### 5) Test Tomcat
If Tomcat is running successfully following step 4, above, you should be able to see the Tomcat Welcome page at the following URL:

http://localhost:8080/

### 6) Shut down Tomcat
To shut down Tomcat type the following from the ./bin directory where Tomcat was installed (/Library/Tomcat/Home following the steps on this page):

```
./shutdown.sh
```


### 7) Running Tomcat as a service daemon
Mac OS X introduced **launchd** as the system-wide service management framework when Mac OS X 10.4 Tiger was released. Since then lanuchd succeeded traditional cron job management as the preferred way of daemonise system services on Mac OS X.

With the previous setup, to start up Tomcat while booting:

1. Create a script as `[`Tomcat home`]`/bin/tomcat-launchd.sh to start Tomcat as a non-daemonised process:

```
#!/bin/sh
#
# Wrapper for running Tomcat under launchd
# Required because launchd needs a non-daemonizing process

function shutdown()
{
        $CATALINA_HOME/bin/shutdown.sh
	/bin/rm $CATALINA_PID
}

function wait_for_death()
{
	while /bin/kill -0 $1 2> /dev/null ; do
		sleep 2	
	done
}

export CATALINA_PID=$CATALINA_HOME/logs/tomcat.pid
$CATALINA_HOME/bin/startup.sh
trap shutdown QUIT ABRT KILL ALRM TERM TSTP
sleep 2
wait_for_death `cat $CATALINA_PID`
```

2. Create a launchd plist at `/Library/LaunchDaemons/org.apache.tomcat.plist`

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple Computer//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>Label</key>
	<string>org.apache.tomcat</string>
	<key>Disabled</key>
	<false/>
	<key>OnDemand</key>
	<false/>
	<key>RunAtLoad</key>
	<true/>
	<key>ProgramArguments</key>
	<array>
		<string>/Library/Tomcat/bin/tomcat-launchd.sh</string>
	</array>
	<key>EnvironmentVariables</key>
	<dict>
		<key>CATALINA_HOME</key>
		<string>/Library/Tomcat</string>
		<key>JAVA_OPTS</key>
		<string>-Djava.awt.headless=true</string>
	</dict>
	<key>StandardErrorPath</key>
	<string>/Library/Tomcat/logs/tomcat-launchd.stderr</string>
	<key>StandardOutPath</key>
	<string>/Library/Tomcat/logs/tomcat-launchd.stdout</string>
	<key>UserName</key>
	<string>_appserver</string>
</dict>
</plist>
```

3. Load the launchd process by

```
$ sudo launchctl load /Library/LaunchDaemons/org.apache.tomcat.plist
```

You can replace the **load** subcommand as **unload**, **stop**, **start** to remove Tomcat from startup processes, stop or start it, respectively.

Please also note that, the sample launchd plist assume the service will run as the user **`_`appserver**, which is predefined on Mac OS X for running app services. So you'll have to make sure the Tomcat directories and IPT data directories are owned, writable and executable by `_`appserver. Or, please refer to [this page](http://code.google.com/p/gbif-providertoolkit/wiki/PermissionSettings).

### 8) References
For more thorough descriptions of Tomcat configuration on Mac OS X, also consult these sources:

  * Tomcat documentation: http://tomcat.apache.org/tomcat-6.0-doc/index.html
  * Tomcat wiki: http://wiki.apache.org/tomcat/TomcatOnMacOS
  * launchd intro: http://en.wikipedia.org/wiki/Launchd
  * launchd wrapper: http://confluence.atlassian.com/display/DOC/Start+Confluence+automatically+on+OS+X+using+launchd