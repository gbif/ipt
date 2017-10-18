---

Copyright 2016 Global Biodiversity Information Facility Secretariat

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific language governing permissions
and limitations under the License.

---

# Release Notes

**IPT Versions: 2.3.3, 2.3.4, 2.3.5**



## Upgrade instructions

### A. Performing the upgrade

**Warning 1**: Be sure to backup the IPT data directory before performing an upgrade. As per the [Data Hosting Centre Criteria](https://github.com/gbif/ipt/wiki/dataHostingCentres#data-hosting-centre-criteria) it is highly recommended to run scheduled backups of the IPT data directory in general.

**Warning 2**: Once an IPT has been upgraded to 2.3.x from 2.2 or earlier, it will not be possible to downgrade to an earlier version due to changes to the IPT's configuration files.

**Warning 3**: IPT 2.3.4+ requires Java 8 to run. Since Java 7 reached its end of life in April 2015, the GBIF development team is now in the process of upgrading all its projects to use Java 8 instead.  With Java 7, the IPT will be unable to communicate with the GBIF registry.

An upgrade can be performed in 4 steps:
  1. Backup the existing data directory and any [custom styling](https://github.com/gbif/ipt/wiki/IPT2Customization.wiki) (images, stylesheets, etc) previously applied to your IPT.
  2. Replace the previous .war file with the latest edition having the same name (e.g. ipt.war). **Note**: If you replace the .war file while Tomcat is stopped, be sure to delete the associated expanded directory (e.g. /ipt) before restarting Tomcat, so that the updated WAR file will be re-expanded when Tomcat restarts.
  3. Immediately open the IPT in a web browser, and reuse the existing data directory on the initial IPT setup page
  4. To be sure the upgrade worked, try logging in and look for the new version number in the footer (**restart Tomcat if cached version still appears**)
  5. Reapply your custom styling (if applicable)

### B. Post-upgrade instructions

Following the upgrade, a warning message may appear indicating the some resources failed to load. This is caused when an old resource is missing required metadata hence it hasn't been republished for a long time. Resource managers can fix this problem by re-publishing the affected resources. 

New in IPT 2.3.x is the ability to publish sample-based data. Once the upgrade is complete, the IPT administrator must install the Darwin Core Event core. Note the Darwin Core Event core is installed by default in new installations.

Additionally, the IPT administrator should update all installed cores and extensions to the latest versions. This will allow publishers to take advantage of any new terms and vocabularies included in the latest versions. 

### C. New Features / Other
  * IPT 2.3.x shows record counts for all files in the DwC-A inside a bar graph on the "Data Records" section of the resource homepage. Note: for the bar graph to appear, resource managers must republish the resource using IPT 2.3.x. 
  * IPT 2.3.x supports publishing sample-based datasets (datasets pertaining to a sampling event). Sample-based datasets use the new Darwin Core Event core, installed by default in new installations. Note: IPT administrators upgrading to IPT 2.3.x from 2.2 or earlier need to install this new core. Please see these [[instructions|IPT2ManualNotes.wiki#install-extension]] for help installing a new core.
  * IPT 2.3.x supports versionable IPT extensions, making it possible to update to newer versions whenever they become available. To understand how this works, refer to the [[Update Extension|IPT2ManualNotes.wiki#update-extension]] section of the IPT User Manual.
  * IPT 2.3.x can be installed without an organisation both in production and in test mode. Previously in version 2.2, the IPT had to be installed with an organisation in order to publish datasets because the publishing organisation became a mandatory metadata field. For more information about this change, refer to [issue 1179](https://github.com/gbif/ipt/issues/1179).
  * IPT 2.3.x includes a slightly redesigned mapping page, making it easier to map your source data. 

## Dependency Notes
* This version has been tested and designed to work on Tomcat 6.0, 7.0, and 8.0, however, the end of life for Apache 6.0.x is 31 December 2016. Therefore you should plan to upgrade to a newer version of Tomcat if necessary. More information about the end of life for Apache 6.0.x can be found [here](http://tomcat.apache.org/tomcat-60-eol.html).
* This version is designed to run on Java 8. Please refer to the [Java version Roadmap](http://www.oracle.com/technetwork/java/eol-135779.html) for an overview of when Oracle plans to discontinue support for the version of Java you currently use.

## Viewing the IPT change log

Version 2.3.3 addressed a total of 88 issues: 22 Defects, 17 Enhancements, 36 Won't fix, 10 Duplicates, and 3 Other.
These are detailed in the [issue tracking system](https://github.com/gbif/ipt/projects/1).

Version 2.3.4 addressed a total of 6 issues: 3 Defects, 2 Enhancements, and 1 Other.
These are detailed in the [issue tracking system](https://github.com/gbif/ipt/projects/3).

Version 2.3.5 addressed a total of 3 issues: 3 Defects, 3 maintenance tasks
These are detailed in the [issue tracking system](https://github.com/gbif/ipt/projects/4).


## When all else fails

See the [FAQ](FAQ.wiki), which continues to be updated with good questions.