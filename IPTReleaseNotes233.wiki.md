---

Copyright 2015 Global Biodiversity Information Facility Secretariat

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific language governing permissions
and limitations under the License.

---

# Release Notes

**IPT Version: 2.3**



## Upgrade instructions

### A. Performing the upgrade

**Warning 1**: Be sure to backup the IPT data directory before performing an upgrade. It is highly recommended to run scheduled backups of the IPT data directory in general.

**Warning 2**: Once an IPT has been upgraded to 2.3, it will not be possible to downgrade to an earlier version due to changes to the IPT's configuration files.

**Warning 3**: IPT 2.3 will be the last version that supports running on a server with Java 6. Since Java 7 reached its end of life in April 2015, the GBIF development team is now in the process of upgrading its projects to use Java 8 instead. This will include the next version of the IPT. 

An upgrade can be performed in 4 steps:

  1. Replace the previous .war file with the latest edition having the same name (e.g. ipt.war). **Note**: If you replace the .war file while Tomcat is stopped, be sure to delete the associated expanded directory (e.g. /ipt) before restarting Tomcat, so that the updated WAR file will be re-expanded when Tomcat restarts.
  2. Backup the existing data directory
  3. Immediately open the IPT in a web browser, and reuse the existing data directory on the initial IPT setup page
  4. To be sure the upgrade worked, try logging in and look for the new version number in the footer (**restart Tomcat if cached version still appears**)

### B. Post-upgrade instructions

New in IPT 2.3 is the ability to publish sample-based data. Once the upgrade is complete, the IPT administrator must install the Darwin Core Event core. Note the Darwin Core Event core is installed by default in new installations.

Additionally, the IPT administrator should update all installed cores and extensions to the latest versions. This will allow publishers to take advantage of any new terms and vocabularies included in the latest versions. 

### C. New Features / Other
  * IPT 2.3 supports publishing sample-based datasets (datasets pertaining to a sampling event). Sample-based datasets use the new Darwin Core Event core, installed by default in new installations. Note: IPT administrators upgrading to IPT 2.3 need to install this new core. Please see these [[instructions|IPT2ManualNotes.wiki#install-extension]] for help installing a new core.
  * IPT 2.3 supports versionable IPT extensions, making it possible to update to newer versions whenever they become available. To understand how this works, refer to the [[Update Extension|IPT2ManualNotes.wiki#update-extension]] section of the IPT User Manual.
  * IPT 2.3 can be installed without an organisation both in production and in test mode. Previously in version 2.2, the IPT had to be installed with an organisation in order to publish datasets because the publishing organisation became a mandatory metadata field. For more information about this change, refer to [issue 1179](https://github.com/gbif/ipt/issues/1179).
  * IPT 2.3 includes a slightly redesigned mapping page, making it easier to map your source data. 
  * The IPT project site  has moved to GitHub! 

## Dependency Notes

This new version has been tested and designed to work on Tomcat 6.0, 7.0, and 8.0. IPT 2.3 can still run on servers with Java 6, however, future versions will be designed to run on Java 8 so please be proactive and plan a Java upgrade on your server soon.

## Viewing the IPT change log

This version addressed a total of 38 issues: 15 Defects, 15 Enhancements, 4 Won't fix, and 4 that were considered as Tasks.
These are detailed in the [issue tracking system](https://github.com/gbif/ipt/issues?q=is%3Aissue+label%3AMilestone-Release2.3)

## When all else fails

See the [FAQ](FAQ.wiki), which continues to be updated with good questions.