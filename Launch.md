# New IPT Version Launch Procedure

## Pre-release steps:

1. Label all issues related to version
    * Label issues with issue type (bug, enhancement, duplicate, won't fix, task, other, etc.), used in statistical reporting.
    * Assign milestone to each issue (e.g. "2.3.5"), used to group all issues addressed since last version was released.
    * Assign project to issues worked on (e.g. [2.3.5](https://github.com/gbif/ipt/projects/4)), used internally and externally to transparently show work done for this version. 
2. Finalise work
    * Work on each issue should be "Done", the meaning of "Done" being understood and agreed on by the entire team. Ideally this includes writing automated testing and manual (UI) testing.
3. Finalise translations 
    * Work on each language by volunteers in [Crowdin](https://crowdin.com/project/gbif-ipt) should be 100% complete
4. Test release candidate
    * Test new features
    * Test areas

## Release steps: 

1. Release new version using Jenkins
2. Update GBIF IPTs to new version
3. Update User Manual
4. Update release history
5. Create/update Release Notes
6. Publish blog post
7. Update GBIF.org
8. Announce to IPT Mailing List
9. Broadcast on social media
10. Reward volunteers


