
---

Copyright 2014 Global Biodiversity Information Facility Secretariat

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific language governing permissions
and limitations under the License.

---

# Release Notes

**IPT Version: 2.1**



### Upgrade instructions

A. Performing the upgrade

Warning 1: It is always a good idea to take the precaution of backing up the existing IPT
data directory before performing an upgrade.

Warning 2: Once an IPT has been upgraded to 2.1, it will not be possible to downgrade to an earlier version due to changes to the IPT's configuration files.

An upgrade can be performed in 3 steps:

  1. Replace the previous .war file with the latest edition
  1. Backup the existing data directory
  1. Reuse the existing data directory during setup

B. Post-upgrade instructions

Upgrading from 2.0.5, once the upgrade is complete, resource managers should:
  1. Republish all occurrence and checklist resources (Windows hosted IPTs only). A [bug](https://code.google.com/p/gbif-providertoolkit/issues/detail?id=1015) in 2.0.5 caused names of files inside the Darwin Core Archive to be prefixed by a backward slash, e.g. "\meta.xml".
  1. Check the geographic coverage is correct for each resource (if applicable). A bug in all earlier versions of the IPT caused bounding boxes to reset to global coverage following server restart on some locales. This was due to a [bug](https://code.google.com/p/gbif-providertoolkit/issues/detail?id=1043) interpreting decimal coordinates, and has been fixed in 2.1

Upgrading from 2.0.3, once the upgrade is complete, resource managers should:
  1. Reset the resource subtype (if applicable), making the selection on the basic metadata page. The reason being that this list now refers to a controlled vocabulary in all subsequent versions and previous selections will have been reset.
  1. Re-save the metadata for each resource. The reason being that additional validation has been added to the metadata pages in all subsequent versions in order to ensure better compliance with the GBIF metadata profile.

C. Other

  * IPT 2.1 now explicitly writes the occurrenceID/taxonID in the published Darwin Core Archive. In previous versions, the value mapped to the occurrenceID/taxonID was written to the ID column in the Darwin Core Archive.
  * IPT 2.1 now supports uploading Excel files as data sources. [GBIF Spreadsheet Templates](http://tools.gbif.org/spreadsheet-processor/) can be used to create new resources, but they have to be converted into a Darwin Core Archive first.
  * GBIF has released a new [guide](IPT2Core.md) on how to add custom cores to the IPT, aimed advaned users and communities prototyping new data standards.

### Dependency Notes:

This new version has been tested and designed to work on Tomcat 6.0 or 7.0. Please note IPT 2.1 no longer supports Java 5, and is designed to run on Java 6 or later.

### Viewing the IPT change log:

This version addressed a total of 85 issues: 38 Defects, 11 Enhancements, 18 Won't fix, 6 Duplicates, 1 Other, and 11 that were considered as Invalid.
These are detailed in the [issue tracking system](https://code.google.com/p/gbif-providertoolkit/issues/list?can=1&q=milestone=Release2.1&sort=status&colspec=ID%20Type%20Status%20Priority%20Milestone%20Owner%20Summary)

### When all else fails:

See the [FAQ](FAQ.md), which continues to be updated with good questions.