# New IPT Version Launch Procedure

## Pre-release steps:

1. Label all issues related to version
    * Label issues with issue type (bug, enhancement, duplicate, won't fix, task, other, etc.), used in statistical reporting.
    * Assign milestone to each issue, used to group all issues addressed since last version was released. For example, here are [all issues addressed in version 2.3.5](https://github.com/gbif/ipt/issues?q=is%3Aissue+milestone%3A2.3.5+is%3Aclosed).
    * Assign project to issues worked on, used internally and externally to transparently show work done for this version. For example, here is the [project for 2.3.5](https://github.com/gbif/ipt/projects/4).
2. Finalise work
    * Work on each issue should be considered "Done". The meaning of "Done" being understood and agreed on by the entire team, but ideally this also includes having written automated testing, performed code reviews as well as UI testing.
3. Finalise translations 
    * Work translating each language (by volunteer translators in [Crowdin](https://crowdin.com/project/gbif-ipt)) should be both 100% translated and approved. 
    * The translations should be exported from Crowdin and committed to the IPT repository following the instructions found in each properties file, e.g. [ApplicationResources_fr.native](https://github.com/gbif/ipt/blob/master/src/main/resources/ApplicationResources_fr.native).
    * Open the [UAT IPT](https://ipt.gbif-uat.org/) to volunteer translators to verify their work in vivo (see step below). 
4. Test release candidate
    * Update the [UAT IPT](https://ipt.gbif-uat.org/) with the release candidate
    * When it makes sense, invite volunteer testers to join efforts by sending an invitation to the [IPT mailing list](https://lists.gbif.org/mailman/listinfo/ipt) explaining how to request an account on the [UAT IPT](https://ipt.gbif-uat.org/) and what areas of testing to focus on.    
    * Test new features - issues labelled as "enhancement". Directly involve the reporter of the enhancement in testing, to verify its implementation meets their expectations. 
    * Test bug fixes - issues labelled as "bug". Try to reproduce the bug following the detailed instructions provided in the issue description. 
    * Test all areas possibly affected by code changes. Build a list of affected areas to test by scanning the commit history.
    * Always ensure that GBIF can index the data published by the IPT, for example using the new GBIF Data Validator.
    * Where applicable, test the IPT in both production and test mode. 
    * Where applicable, perform cross-browser testing.

## Release steps: 

1. Release new version using Jenkins
    * **Note**: comment out the integration tests (ITs) for Jenkins to release the IPT successfully. Remember to uncomment the ITs in master afterwards. 
2. Update GBIF IPTs to new version
    * Production instances:
        * [BID IPT](https://cloud.gbif.org/bid) - customized (see below for help)
        * [EU BON IPT](https://cloud.gbif.org/eubon) - customized (see below for help)
        * [ALA IPT](http://ipt.ala.org.au/) - needs to be customized todo with ALA logo (see below for help). **Note** this instance still runs in HTTP and should be converted to HTTPS with Dave Martin's help. **Note** this instance runs on ALA's server and requires special access credentials from Dave Martin/Kyle Braak.
        * [EIA IPT](https://cloud.gbif.org/eia/) - vanilla
        * [GIASIP IPT](https://giasip.gbif.org) - vanilla **Note** this runs in test mode but is treated like it's in production.
        * [TEST EU BON IPT](http://eubon-ipt.gbif.org/) - vanilla **Note** this runs in test mode but is treated like it's in production as it's embedded in EU BON's portal.
    * Test/Sandbox instances:
        * [DEMO IPT](https://ipt.gbif.org/) - vanilla **Note** It is always a good idea to cleanup old resources to save disk space.
        * [UAT IPT](https://ipt.gbif-uat.org/) - vanilla
        * [DEV IPT](https://ipt.gbif-dev.org/) - vanilla
3. Update User Manual
4. Update release history
5. Create/update Release Notes
6. Publish blog post
7. Update GBIF.org
8. Announce to IPT Mailing List
9. Broadcast on social media
10. Reward volunteers


