==========================================================================================
Copyright 2012 Global Biodiversity Information Facility Secretariat

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file 
except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software distributed under the 
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
either express or implied. See the License for the specific language governing permissions 
and limitations under the License.
==========================================================================================


                     GBIF Integrated Publishing Toolkit 2.0.4
                                 Release Notes


===============================
INFORMATION ABOUT THIS RELEASE:
===============================

* Upgrade instructions (important)
* Dependency notes
* Viewing the IPT change log
* When all else fails

======================
Upgrade instructions
======================
A. Performing the upgrade

Warning: It is always a good idea to take the precaution of backing up the existing IPT 
data directory before performing an upgrade. 

An upgrade can be performed in 3 steps:

1. Replace the previous .war file with the latest edition
2. Backup the existing data directory
3. Reuse the existing data directory during setup

B. Post-upgrade instructions
 
Once the upgrade is complete, resource managers should: 

1. Reset the resource subtype (if applicable), making the selection on the basic metadata 
page. The reason being that this list now refers to a controlled vocabulary in IPT 2.0.4 
and previous selections will have been reset. 
2. Re-save the metadata for each resource. The reason being that additional validation 
has been added to the metadata pages in IPT 2.0.4 in order to ensure better compliance 
with the GBIF metadata profile.

C. Other

The style/CSS in IPT 2.0.4 has been revised. The unfortunate outcome is that all 
customization guides written for earlier versions of the IPT are no longer applicable.
GBIF hopes to release an updated customization guide for IPT 2.0.4 shortly after release. 

==================
Dependency Notes:
==================
This new version has been tested and designed to work on Tomcat 6.0 or 7.0.
Please note IPT 2.0.4 is still designed to run on Java 5 and later. 

============================
Viewing the IPT change log:
============================
This version addressed a total of 108 issues: 38 Defects, 35 Enhancements, 7 Other, 
5 Patches, 18 Won't fix, 4 Duplicates, and 1 that was considered as Invalid. 

These are detailed in the issue tracking system: 
http://code.google.com/p/gbif-providertoolkit/issues/list?can=1&q=milestone%3DRelease2.0.4
  
====================
When all else fails:
====================
See the FAQ:
http://code.google.com/p/gbif-providertoolkit/wiki/FAQ