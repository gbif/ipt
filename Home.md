# Welcome to the IPT wiki

Here you can find the [[IPT User Manual|IPT2ManualNotes.wiki]] and a variety of other valuable resources.

## New version with security update released!

**Version 2.4.1** is available for download [here](https://repository.gbif.org/content/groups/gbif/org/gbif/ipt/2.4.1/ipt-2.4.1.war). Version 2.4.1 has minor bugfixes and dependency updates to improve the security of the IPT, following similar updates made for version 2.4.0.

**Version 2.4.0** also includes updates for the integration to issue DOIs directly with DataCite.

This builds on top of the security update added in **Version 2.3.4** that fixed a [critical vulnerability](https://struts.apache.org/docs/s2-045.html) that was discovered in the Apache Struts web framework, which the IPT uses. According to [this article](http://thehackernews.com/2017/03/apache-struts-framework.html), this was a remote code execution vulnerability that could allow hackers to execute malicious commands on the IPT server. It also says that hackers are actively exploiting this vulnerability.

**Therefore all users should aim to run the latest version, updating if needed by following the instructions in the [[Release Notes|IPTReleaseNotes233.wiki]].**

## Featured content

_**Need help validating your dataset?**_ For help, refer to this new [data quality checklist](https://github.com/gbif/ipt/wiki/dataQualityChecklist). It is particularly suited for checking occurrence and sampling event datasets and will help ensure that the dataset is both valid and complete

_**Confused about what types of data GBIF supports? Confused about how to publish data and register it with GBIF?**_ Check out the new [[How to Publish Guide|howToPublish]]. It starts by describing the four classes of biodiversity data that can be published through GBIF.org. For each class of data you'll find a simple set of instructions guiding you through the publication steps. Try out the simple Excel templates designed to simplify publication even more!

_**Interested in writing your own resource metadata in XML?**_ Check out [this new how-to guide](https://github.com/gbif/ipt/wiki/How-to-write-your-own-EML-XML-file).
