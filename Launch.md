# New IPT Version Launch Procedure

## Table of Contents
+ [[Introduction| Launch#introduction]]
+ [[Pre-release steps| Launch#pre-release-steps]]
+ [[Release and public launch steps| Launch#release-and-public-launch-steps]]

## Introduction

A checklist documenting the ordered steps of the release and launch procedure of the IPT. Learn as you go - review and adjust the process to streamline it. 

Many players are involved in the process, not least the GBIF Communications team and GBIF Systems Administrator. Make sure they are on standby ready to help before embarking on the launch. 

## Pre-release steps

1. Label all issues related to version
    * Label issues with issue type (`Bug`, `Enhancement`, `Duplicate`, `Won't fix`, `Type-Task`, `Type-Other`, etc.), used in statistical reporting.
    * Label issues requiring a change in the UserManual/Wiki with `UserManual`.
    * Assign milestone to each issue, used to group all issues addressed since last version was released. For example, here are [all issues addressed in version 2.3.5](https://github.com/gbif/ipt/issues?q=is%3Aissue+milestone%3A2.3.5+is%3Aclosed).
2. Finalise work
    * Work on each issue should be considered "Done". The meaning of "Done" being understood and agreed on by the entire team, but ideally this also includes having written automated testing, performed code reviews as well as UI testing.
3. Finalise translations 
    * Work translating each language (by volunteer translators in [Crowdin](https://crowdin.com/project/gbif-ipt)) should be both 100% translated and approved. 
    * The translation pull request from CrowdIn should be merged.
    * Open the [UAT IPT](https://ipt.gbif-uat.org/) to volunteer translators to verify their work in vivo (see step below). 
4. Test release candidate
    * Update the [UAT IPT](https://ipt.gbif-uat.org/) with the release candidate
    * When it makes sense, invite volunteer testers to join efforts by sending an invitation to the [IPT mailing list](https://lists.gbif.org/mailman/listinfo/ipt) explaining how to request an account on the [UAT IPT](https://ipt.gbif-uat.org/) and what areas of testing to focus on.    
    * Test new features - issues labelled as `Enhancement`. Directly involve the reporter of the enhancement in testing, to verify its implementation meets their expectations. 
    * Test bug fixes - issues labelled as `Bug`. Try to reproduce the bug following the detailed instructions provided in the issue description. 
    * Test all areas possibly affected by code changes. Build a list of affected areas to test by scanning the commit history.
    * Always ensure that GBIF can index the data published by the IPT, for example using the new GBIF Data Validator.
    * Put on different user hats, testing as an 'Admin', 'Manager' and 'Manager with registration rights'.
    * Where applicable, test the IPT in both production and test mode. 
    * Where applicable, perform cross-browser testing.

## Release and public launch steps 

1. Release new version using Jenkins
    * Note: comment out the integration tests (ITs) for Jenkins to release the IPT successfully. Remember to uncomment the ITs in master afterwards. 
2. Update GBIF IPTs to new version
    * Update production instances:
        * [BID IPT](https://cloud.gbif.org/bid) - customized (see below for help).
        * [EU BON IPT](https://cloud.gbif.org/eubon) - customized (see below for help).
        * [ALA IPT](http://ipt.ala.org.au/) - customised (see below for help). Note this instance runs on ALA's server.
        * [EIA IPT](https://cloud.gbif.org/eia/) - vanilla.
        * [GIASIP IPT](https://giasip.gbif.org) - vanilla. Note this runs in test mode but is treated like it's in production.
        * [TEST EU BON IPT](http://eubon-ipt.gbif.org/) - vanilla. Note this runs in test mode but is treated like it's in production as it's embedded in EU BON's portal.
    * Update Test/Sandbox instances:
        * [DEMO IPT](https://ipt.gbif.org/) - vanilla. Note It is always a good idea to cleanup old resources to save disk space.
        * [UAT IPT](https://ipt.gbif-uat.org/) - vanilla
        * [DEV IPT](https://ipt.gbif-dev.org/) - vanilla
    * Simple customisations for the above IPTs are done by a) changing the logo image in [menu.ftl#L12](https://github.com/gbif/ipt/blob/master/src/main/webapp/WEB-INF/pages/inc/menu.ftl#L12), b) removing the test image in [menu.ftl#L20](https://github.com/gbif/ipt/blob/master/src/main/webapp/WEB-INF/pages/inc/menu.ftl#L20) (where applicable) and c) tweaking the CSS in [main.css#L297](https://github.com/gbif/ipt/blob/master/src/main/webapp/styles/main.css#L297). Note: before an upgrade, the custom logo image(s) and CSS need to be backed-up/preserved and then copied back to the expanded data directory. 
3. Update [User Manual](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki) and [Wiki](https://github.com/gbif/ipt/wiki)
    * When major changes are expected, archive the User Manual before applying any changes. For example, here is the archived copy of the [User Manual for version 2.0.5](https://github.com/gbif/ipt/wiki/IPTUserManualv205.wiki). 
    * Add/update User Manual or wiki content - see issues labelled as `UserManual`. 
    * State the IPT version that the manual corresponds to underneath the title. 
    * Add a link to the former version(s) of the manual to make it easy to find.
    * After applying all changes in English, request Spanish translators (e.g. SIB Colombia) to apply all changes to the [Spanish User Manual](https://github.com/gbif/ipt/wiki/IPT2ManualNotes_ES.wiki) also. As highlighted in the [GBIF Data Publishing Analysis](https://docs.google.com/document/d/1epPxmHeTsEoDGQwIPmBY5AdC2R_Cpm-5HlgEXL-FuBE/edit?usp=sharing), the updates from v2.3 to v2.3.4 are still outstanding.
4. Create/update Release Notes
    * When major changes are expected, create a new Release Notes, otherwise extend the existing notes as has been done for the [2.3.3 Release Notes](https://github.com/gbif/ipt/wiki/IPTReleaseNotes233.wiki).
    * The Release Notes should contain all the information needed to properly upgrade their IPT to the latest version. Typically it contains the following sections:
        * Upgrade instructions
        * Post-upgrade instructions
        * New Features / Other
        * Dependency Notes
        * Viewing the IPT change log
        * When all else fails
5. Publish blog post
    * Publicise in some detail, select improvements that users will value. For example, here is an example blog post for [IPT v2.2](http://gbif.blogspot.dk/2015/03/ipt-v22.html).
    * Be sure to acknowledge volunteer translators and coders that contributed to the release.
    * Review the blog post with the help of the GBIF Communications team before publishing. 
6. Update [release history](https://github.com/gbif/ipt/wiki/Releases)
    * Add section for new version including a link to the .war download, release notes, user manual, how many issues were addressed broken down by type, blog post and a short summary of what changed.  
7. Update [Roadmap](https://github.com/gbif/ipt/wiki/Roadmap)
    * Keep this in sync with the [Github IPT Readme](https://github.com/gbif/ipt/blob/master/README.md) and [GBIF.org IPT page](https://www.gbif.org/ipt), which also show the Roadmap and get updated in subsequent steps below.
8. Update [Github IPT Readme](https://github.com/gbif/ipt/blob/master/README.md)
    * Advertise the new version, highlighting what changes will be interesting and valuable to users linking to blog post when applicable.
9. Update [GBIF.org IPT page](https://www.gbif.org/ipt)
    * Mirror relevant changes made to IPT Readme in step above
    * Update IPT uptake statistics, e.g. number of installations and number of countries having installations displayed at bottom of map. 
10. Announce to [IPT mailing list](https://lists.gbif.org/mailman/listinfo/ipt)
    * Keep the message short so that people actually read it, linking to the blog post when applicable that has more detailed information about the release. Here are a couple example announcements for [2.2 major release](https://lists.gbif.org/pipermail/ipt/2015-March/000591.html), [2.3.3 minor release](https://lists.gbif.org/pipermail/ipt/2017-January/000666.html) and [2.3.4 security patch release](https://lists.gbif.org/pipermail/ipt/2017-March/000671.html) 
    * Highlight GBIF's vigilance in keeping the IPT secure, while reminding people of the importance of updating their instance with this latest version. 
11. Broadcast on social media
    * Done using the [GBIF Facebook](https://www.facebook.com/gbifnews) and [GBIF Twitter](https://twitter.com/GBIF) accounts via the GBIF Communications team
12. Reward volunteers
    * Say thank you again, in addition to saying it in the blog post and mailing list announcement.
    * Encourage volunteers to include this experience on their CV


