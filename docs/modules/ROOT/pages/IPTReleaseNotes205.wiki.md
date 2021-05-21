
---

Copyright 2013 Global Biodiversity Information Facility Secretariat

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific language governing permissions
and limitations under the License.

---

# Release Notes

**IPT Version: 2.0.5**



### Upgrade instructions

A. Performing the upgrade

Warning: It is always a good idea to take the precaution of backing up the existing IPT
data directory before performing an upgrade.

An upgrade can be performed in 3 steps:

  1. Replace the previous .war file with the latest edition
  1. Backup the existing data directory
  1. Reuse the existing data directory during setup

B. Post-upgrade instructions

If upgrading from 2.0.3, once the upgrade is complete, resource managers should:

  1. Reset the resource subtype (if applicable), making the selection on the basic metadata page. The reason being that this list now refers to a controlled vocabulary in IPT 2.0.4/5 and previous selections will have been reset.
  1. Re-save the metadata for each resource. The reason being that additional validation has been added to the metadata pages in IPT 2.0.4/5 in order to ensure better compliance with the GBIF metadata profile.

C. Other

GBIF has released a new style/CSS customization guide for IPT 2.0.5 entitled [How to Style Your IPT](IPT2Customization.md)

### Dependency Notes:

This new version has been tested and designed to work on Tomcat 6.0 or 7.0.
Please note IPT 2.0.5 is still designed to run on Java 5 and later. This will be the last
version, however, that maintains support for Java 5 so please plan an upgrade
of your Java version soon to be able to support future versions.

### Viewing the IPT change log:

This version addressed a total of 45 issues: 15 Defects, 17 Enhancements, 2 Patches,
7 Won't fix, 3 Duplicates, and 1 that was considered as Invalid.
These are detailed in the [issue tracking system](http://code.google.com/p/gbif-providertoolkit/issues/list?can=1&q=milestone%3DRelease2.0.5)

### When all else fails:

See the [FAQ](FAQ.md), which continues to be updated with good questions.