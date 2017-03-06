# FAQ

## Table of Contents
+ [[Introduction|FAQ.wiki#introduction]]
+ [[Installation|FAQ.wiki#installation]]
  + [[1. What is the best way to move my IPT to another server without losing any data?|FAQ.wiki#1-what-is-the-best-way-to-move-my-ipt-to-another-server-without-losing-any-data]]
  + [[2. My GBIF IPT instance is slow. What can I do to improve performance?|FAQ.wiki#2-my-gbif-ipt-instance-is-slow-what-can-i-do-to-improve-performance]]
  + [[3. After installing the IPT, I receive a message "FAIL - Application at context path /ipt could not be started" and the application does not launch. What should I do?|FAQ.wiki#3-after-installing-the-ipt-i-receive-a-message-fail---application-at-context-path-ipt-could-not-be-started-and-the-application-does-not-launch-what-should-i-do]]
  + [[4. I get the following error: "The data directory '/directory' is not writable. What should I do?|FAQ.wiki#4-i-get-the-following-error-the-data-directory-directory-is-not-writable-what-should-i-do]]
  + [[5. How can I change the IPT's default language?|FAQ.wiki#5-how-can-i-change-the-ipts-default-language]]
  + [[6. How can I switch the IPT from test mode to production mode?|FAQ.wiki#6-how-can-i-switch-the-ipt-from-test-mode-to-production-mode]]
  + [[7. What outgoing connections does the IPT make?|FAQ.wiki#7-what-outgoing-connections-does-the-ipt-make]]
  + [[8. How can I configure my IPT to assign DOIs to datasets?|FAQ.wiki#8-how-can-i-configure-my-ipt-to-assign-dois-to-datasets]]
+ [[Usage|FAQ.wiki#usage]]
  + [[1. Why do published files contain broken lines?|FAQ.wiki#1-why-do-published-files-contain-broken-lines]]
  + [[2. Why do published files not include all records?|FAQ.wiki#2-why-do-published-files-not-include-all-records]]
  + [[3. What does the error "No space left on device" mean and how do I fix it?|FAQ.wiki#3-what-does-the-error-no-space-left-on-device-mean-and-how-do-i-fix-it]]
  + [[4. How do I change the publishing organisation of my resource? The dropdown on the Basic Metadata page is disabled.|FAQ.wiki#4-how-do-i-change-the-publishing-organisation-of-my-resource-the-dropdown-on-the-basic-metadata-page-is-disabled]]
  + [[5. How do I change the type of an existing resource?|FAQ.wiki#5-how-do-i-change-the-type-of-an-existing-resource]]
  + [[6. What are some tricks to simplify the authoring and maintenance of metadata?|FAQ.wiki#6-what-are-some-tricks-to-simplify-the-authoring-and-maintenance-of-metadata]]
+ [[Indexing by GBIF|FAQ.wiki#indexing-by-gbif]]
  + [[1. How long does it take GBIF to (re)index my dataset following registration?|FAQ.wiki#1-how-long-does-it-take-gbif-to-start-reindexing-my-dataset]]
  + [[2. Why hasn't GBIF (re)indexed my dataset yet?|FAQ.wiki#2-why-hasnt-gbif-reindexed-my-dataset-yet]]
  + [[3. How often does GBIF reindex my dataset?|FAQ.wiki#3-how-often-does-gbif-reindex-my-dataset]]
  + [[4. What type of datasets does GBIF index/support?|FAQ.wiki#4-what-type-of-datasets-does-gbif-indexsupport]]

## Introduction
In this page you will find answers to the most frequently asked questions about the GBIF IPT. Please check the contents of this page before contacting the GBIF Helpdesk.

## Installation

### 1. What is the best way to move my IPT to another server without losing any data?

There is one important directory that contains all the IPT's configuration and resources: the IPT's data directory. If you ensure this directory is regularly backed-up somewhere safe, you'll never lose any data.

To move the IPT to a different server, just make sure that you copy the entire contents of the IPT's data directory over to the new server. It is important to preserve the same folder/file permissions. Then, all you have to do is follow the [Starting Over](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#starting-over) instructions. As it says: "If the user enters the same absolute path to the data directory as before, the previous configuration will be completely restored".

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

Please note the two-letter language code must match the code used by the IPT. Other languages currently supported include Japanese (ja), French (fr), Spanish (es), Traditional Chinese (zh), and Russian (ru).

Please note that every time you upgrade to a new version of the IPT, you will have to apply the same change.

### 6. How can I switch the IPT from test mode to production mode?

Switching the IPT from test mode to production mode cannot be done automatically. This is by design, because each mode connects to a different registry (test mode uses the GBIF Sandbox Registry while production mode uses the GBIF Live Registry). 

Therefore to switch to production mode you need to setup an entirely new IPT instance in production mode. 

To save time recreating resources, you can try transferring your 'test' resources to the production IPT. To do this simply "upload an existing IPT resource using its zipped resource configuration folder." There are complete instructions on how to do this [here](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#upload-a-zipped-ipt-resource-configuration-folder).

### 7. What outgoing connections does the IPT make?

In production mode, the IPT makes outgoing connections to http://gbrds.gbif.org. In case you need to update your firewall rules, note you will also need to cater for a “non-proxyfied” redirection to http://apps2.gbif.org. 

In test mode, the IPT makes outgoing connections to http://gbrdsdev.gbif.org. In case you need to update your firewall rules, note you will also need to cater for a “non-proxyfied” redirection to http://apps2.gbif-uat.org. 

### 8. How can I configure my IPT to assign DOIs to datasets?
 
In order to assign DOIs to datasets, the IPT must be configured with a DataCite or EZID account, which has itself been configured to mint DOIs under the IPT's domain. 

The account holding organization must already be associated to the IPT as an organization. For help associating an organization to your IPT, you can refer to the [Add Organization](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#add-organisation) section of the User Manual. 

All that remains, is to add the DataCite or EZID account information to the organization (e.g. the username, password, prefix, etc) and make sure the "Account activated" checklist has been checked. For help editing the organization and a description of each field in the form, you can refer to the [Edit Organization](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#edit-organisation) section of the User Manual.

If you work for a GBIF ‘country' Node, please contact the GBIF Helpdesk <helpdesk@gbif.org> to inquire about getting a free DataCite account. GBIF has its own account with DataCite and may be able to provide 'country' Nodes with their own free account (determined on a case by case basis). GBIF has done this for Spain and Colombia so far, both of which have a strong track record of keeping their IPTs online near 100% of the time. 

The following well-maintained IPTs have all been configured with a DataCite accounts, can assign DOIs to datasets, and are thus recognized as a repository in BioSharing.org, which is used by journals such as Nature to refer their authors to one or more repositories that are appropriate for the type of data they have.

* [GBIF Spain IPT](http://www.gbif.es/ipt/) - [BioSharing record](https://biosharing.org/biodbcore-000854)
* [GBIF Australia IPT](http://ipt.ala.org.au/) - [BioSharing record](https://biosharing.org/biodbcore-000852)
* [Canadensys IPT](http://data.canadensys.net/ipt/) - [BioSharing record](https://biosharing.org/biodbcore-000855)
* [GBIF Colombia IPT](http://ipt.biodiversidad.co/sib/) - [BioSharing record](https://biosharing.org/biodbcore-000856)  

Authors in Spain are already depositing their standardized data in the GBIF Spain IPT, e.g. the following dataset http://doi.org/10.15470/qomfu6 underlies this Scientific Data Data Descriptor: http://doi.org/10.1038/sdata.2016.85 

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

### 5. How do I change the type of an existing resource? 

The type of a resource is derived from its core mapping:
* If the core mapping is to the [Occurrence extension](http://rs.gbif.org/core/dwc_occurrence_2015-07-02.xml), the type is equal to "occurrence". 
* If the core mapping is to the [Taxon extension](http://rs.gbif.org/core/dwc_taxon_2015-04-24.xml), the type is equal to "checklist". 
* If the core mapping is to the [Event extension](http://rs.gbif.org/core/dwc_event_2016_06_21.xml), the type is equal to "sampling-event". 
* If the core mapping is not equal to one of the IPT's default cores (Occurrence, Taxon, or Event) the type is equal to "other".  

Therefore to change the type of a resource, you need to change its core mapping. To change an occurrence resource to type checklist, for example, simply delete all core mappings to the Occurrence extension, and then recreate new core mappings to the Taxon extension. A new version of the resource should be published in order to finalize the change. If the resource has been registered with GBIF, its type will be automatically updated after it has been re-published.

### 6. What are some tricks to simplify the authoring and maintenance of metadata?
Datasets can change over time. Keeping the dataset metadata up to date can be a burden, as inconsistencies can begin to creep in over time. Below is a list of tricks you can use to simplify the authoring of metadata and make it easier to maintain over time:
* use a contact's ORCID instead of supplying their email and other fields which are likely change if the person changes jobs. For more information about how to supply an ORCID see [here](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#basic-metadata).
* for datasets that continue to change/grow (versus static datasets) avoid specifying exact counts in textual descriptions, e.g. for record or taxa counts.
* use the IPT's citation [Auto-generation feature](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#citations) instead of supplying a manually created data citation. 
* use the IPT's import by [Taxon list feature](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#taxonomic-coverage) instead of supplying each taxa manually one at a time when entering the taxonomic converage. 

## Indexing by GBIF
### 1. How long does it take GBIF to start (re)indexing my dataset?

The answer depends on how long GBIF's indexing queue is, how big your dataset it and whether GBIF's indexing service is turned on.

Normally it will take between 5-60 minutes for GBIF to start indexing your dataset. It can take several hours to finish indexing large datasets once started (e.g. with several million records) so please be patient. If you believe GBIF failed to index your dataset successfully, please submit feedback directly via GBIF.org, or send an email directly to the GBIF Helpdesk <helpdesk@gbif.org> to investigate what happened. If you are interested in finding out why GBIF may not have (re)indexed your dataset, please see #2 below.

### 2. Why hasn't GBIF (re)indexed my dataset yet?

Occasionally, GBIF turns off its indexing service for maintenance. This is the most common reason why datasets aren't indexed as quickly as expected.

If your dataset has been successfully reindexed, but the records weren't actually updated, you may be affected by [this bug](https://github.com/gbif/crawler/issues/1) in the crawling service. 

In either case, if you believe GBIF failed to index your dataset, please submit feedback directly via GBIF.org, or send an email directly to the GBIF Helpdesk <helpdesk@gbif.org> to investigate what happened.

### 3. How often does GBIF reindex my dataset?

GBIF automatically attempts to reindex all registered datasets every 7 days. GBIF will only reindex the dataset if its last published date has changed since the last time it was indexed. 

GBIF automatically attempts to reindex a registered dataset each time its registration is updated. This happens each time the dataset gets republished via the IPT. **Note, however, GBIF doesn't reindex the same dataset more than once every five days. **

### 4. What type of datasets does GBIF index/support?

GBIF supports 4 classes of datasets described [here](https://github.com/gbif/ipt/wiki/howToPublish#introduction). GBIF currently only indexes species occurrence records. Species occurrences can be provided as core records or as extension records. In the case of sampling-event datasets, species occurrences in extension records will be augmented with information coming from its core event record wherever possible. 

