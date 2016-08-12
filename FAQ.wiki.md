# FAQ

## Table of Contents
+ [[Introduction|FAQ.wiki#introduction]]
+ [[Installation|FAQ.wiki#installation]]
  + [[1. What is the best way to move my IPT to another server without losing any data?|FAQ.wiki#1-what-is-the-best-way-to-move-my-ipt-to-another-server-without-losing-any-data]]
  + [[2. My GBIF IPT instance is slow. What can I do to improve performance?|FAQ.wiki#2-my-gbif-ipt-instance-is-slow-what-can-i-do-to-improve-performance]]
  + [[3. After installing the IPT, I receive a message "FAIL - Application at context path /ipt could not be started" and the application does not launch. What should I do?|FAQ.wiki#3-after-installing-the-ipt-i-receive-a-message-fail---application-at-context-path-ipt-could-not-be-started-and-the-application-does-not-launch-what-should-i-do]]
  + [[4. I get the following error: "The data directory '/directory' is not writable. What should I do?|FAQ.wiki#4-i-get-the-following-error-the-data-directory-directory-is-not-writable-what-should-i-do]]
  + [[5. How can I change the IPT's default language?|FAQ.wiki#5-how-can-i-change-the-ipts-default-language]]
+ [[Usage|FAQ.wiki#usage]]
  + [[1. Why do published files contain broken lines?|FAQ.wiki#1-why-do-published-files-contain-broken-lines]]
  + [[2. Why do published files not include all records?|FAQ.wiki#2-why-do-published-files-not-include-all-records]]
  + [[3. What does the error "No space left on device" mean and how do I fix it?|FAQ.wiki#3-what-does-the-error-no-space-left-on-device-mean-and-how-do-i-fix-it]]

## Introduction
In this page you will find answers to the most frequently asked questions about the GBIF IPT. Please check the contents of this page before contacting the GBIF Helpdesk.

## Installation

### 1. What is the best way to move my IPT to another server without losing any data?

There is one important directory that contains all the IPT's configuration and resources: the IPT's data directory. If you ensure this directory is regularly backed-up somewhere safe, you'll never lose any data.

To move the IPT to a different server, just make sure that you copy the entire contents of the IPT's data directory over to the new server. It is important to preserve the same folder/file permissions. Then, all you have to do is follow the [Starting Over](https://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes?tm=6#Starting_Over) instructions. As it says: "If the user enters the same absolute path to the data directory as before, the previous configuration will be completely restored".

### 2. My GBIF IPT instance is slow. What can I do to improve performance?

Apache Tomcat comes with very little allocated memory. In order to increase performance, this amount should be increased depending on the amount of physical memory on the server. If the server has at least 2GB of RAM, the available memory should be increased to 1GB. For more information on how to do it, see the following links:
  * http://wiki.apache.org/tomcat/FAQ/Memory

The following is known to run:
`export CATALINA_OPTS="-Xms512M -Xmx1024M -XX:PermSize=512M -XX:MaxPermSize=1024M"`

### 3. After installing the IPT, I receive a message "FAIL - Application at context path /ipt could not be started" and the application does not launch. What should I do?

Some Linux distributions, like [CentOS](http://www.centos.org/) or [Red Hat Linux](http://www.redhat.com/) include Tomcat incorrectly shipped with two libraries, xml-apis.jar and/or xalan.jar, in the Tomcat directories (../tomcat5/shared/, ../tomcat5/common/).

These libraries are not needed for the GBIF IPT operation so make a backup copy of them and then remove them. If that does not solve your problem, put the files back into place from the backup copy.

_If your server admin won't allow these libraries to be removed_, another known solution reported in Issue 851 (on Google Code) is to include the xalan.jar in the IPT's /lib directory located at $tomcat/webapps/ipt/WEB-INF. Just copy the jar file to that folder, restart Tomcat, and the error should disappear.

### 4. I get the following error: "The data directory '/directory' is not writable. What should I do?

Assuming you are running Tomcat for example, you need to ensure the user running Tomcat has permissions on the directory. To find out the user running Tomcat in UNIX, open a shell and enter the following command:

```
$ ps waux | grep tomcat
```

If the user is "tomcatuser", and this user belongs to group "tomcatgroup" then change the ownership of the IPT data directory (and it's child folders and files) by entering the following command:

```
$ chown -R tomcatuser:tomcatgroup directory/
```

To ensure only this user has write permission, enter the following command:

```
$ chmod -R 755 directory/
```

### 5. How can I change the IPT's default language?

The IPT's default language is English, and there is no way to change the default language via the user interface.

It can be changed easily by manual configuration though. To change the default language from English to Portuguese for example, first locate the struts.properties file (if the IPT is deployed in Tomcat for example, it would be located in /tomcat6/webapps/ipt/WEB-INF/classes). Then update the struts.locale property to:

struts.locale=pt

Restart Tomcat, and the IPT will startup in Portuguese.

Please note the two-letter language code must match the code used by the IPT. Other languages currently supported include Japanese (ja), French (fr), Spanish (es), and Traditional Chinese (zh).

Please note that every time you upgrade to a new version of the IPT, you will have to apply the same change.

## Usage

### 1. Why do published files contain broken lines?
The IPT does not support source files that have multiline fields (fields that include a newline character (\n) or carriage return (\r)) even if you have specified a field quote (a single character that encloses every field/column in a row) in the source's configuration.

Unless these line breaking characters are removed, the IPT will publish files with broken lines (the columns will appear mixed up).

To solve this, you can remove these line breaking characters from the source file, replace the source file with the new one, and republish the resource. Remember that when uploading a source file, you can tell the IPT to replace the file with a new one as long as they both have the same name. That way the mappings don't have to be redone.

### 2. Why do published files not include all records?
Check the publication log for exceptions such as:

```
java.sql.SQLException: Cannot convert value '0000-00-00 00:00:00' from column 65 to TIMESTAMP
```

that means you have invalid date value in your data source, which, in this case, is "0000-00-00 00:00:00."

To solve this, you can update the value with "Null" value, and update the resource.
Usually, you can rely on the log message to identify the column of interest, like in the example above, it says "column 65," which is the 65th column in the data source.

The "0000-00-00 00:00:00" value in your SQL table could be resulted when importing, while having defined the column with "Not Null" and default value as "0000-00-00 00:00:00."

### 3. What does the error "No space left on device" mean and how do I fix it?
If you found an exception such as:

```
Caused by: java.io.IOException: No space left on device
```

in your publication log file, it means there is no space left in the disk partition that contains your IPT data directory. 

To solve this, you can:
- Allocate more space to this partition. 
- Move your IPT data directory to another partition where there is more space available. Note, this requires you to [[reinstall|IPT2ManualNotes.wiki#starting-over]] your IPT. 
- Free up disk space (e.g. deleting temporary files, remove unused applications, etc) 

### 4. How do I change the publishing organisation of my resource? The dropdown on the Basic Metadata page is disabled.
Please be aware the publishing organisation cannot be changed after the resource has been either registered with GBIF or assigned a DOI.

In order to change the publishing organisation, you need to republish the resource and then reset the desired publishing organisation. To simplify the process, you can recreate the dataset from its zipped IPT resource folder. Instructions on how to do that can be found [here](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#upload-a-zipped-ipt-resource-configuration-folder). 

Do not "delete" the old resource via the IPT user interface, as this will delete the registered resource in GBIF.

Instead, you should migrate the resource from the old publishing organisation to the new publishing organisation by following [these instructions](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#migrate-a-resource). Please pay careful attention to step #5, where you will have to ask the GBIF Helpdesk to update the GBIF Registry. 

Lastly, you can delete the old resource by removing its IPT resource folder inside the IPT Data Directory. Restart Tomcat for the deletion to take effect.





in your publication log file, it means there is no space left in the disk partition that contains your IPT data directory. 

To solve this, you can:
- Allocate more space to this partition. 
- Move your IPT data directory to another partition where there is more space available. Note, this requires you to [[reinstall|IPT2ManualNotes.wiki#starting-over]] your IPT. 
- Free up disk space (e.g. deleting temporary files, remove unused applications, etc) 