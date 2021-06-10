[![Build Status](https://builds.gbif.org/job/ipt/badge/icon?style=flat-square)](https://builds.gbif.org/job/ipt/)

# Ceiba IPT v2.4
## Welcome to the IPT Repository including Wiki, Issue Manager and Project Manager!

Inside this repository you can find the [IPT User Manual](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki), [FAQ](https://github.com/gbif/ipt/wiki/FAQ.wiki) and a variety of other valuable resources aimed at users, coders and translators. Do you have a question that you can't find the answer to? Then send your question to the [IPT Mailing List](https://lists.gbif.org/mailman/listinfo/ipt) to get it answered!

### About the IPT

The Integrated Publishing Toolkit (IPT) is a free, open source software tool written in Java that is used to publish and share biodiversity datasets through the GBIF network. The IPT can also be configured with a DataCite account in order to assign DOIs to datasets transforming it into a data repository.

To understand how the IPT works, try watching this concise 25 minute live demo showing how a dataset gets properly published and registered through GBIF.org (if you are unable to watch the video in your country, it can also be [downloaded directly](http://videos.contentful.com/q553fnlofhvs/3iCjm4lxRSiCYE6Qq2A4GG/63b5690e48de42b0872ba4c25d629fe9/Introduction_to_publishing_using_the_GBIF_Integrated_Publishing_Toolkit__28IPT_29.mp4)):

<a href="https://www.youtube.com/embed/eDH9IoTrMVE?ecver=1" target="_blank"><img src="https://raw.githubusercontent.com/wiki/gbif/ipt/gbif-ipt-docs/screenshots/IPTDemoVideoIntroSlide.png" alt="How to publish biodiversity data through GBIF.org using the IPT" width="560" height="315" border="10" /></a>

### Latest Release: 2.4.2

Version 2.4.2 is available for download [here](https://repository.gbif.org/content/groups/gbif/org/gbif/ipt/2.4.2/ipt-2.4.2.war), via a [CentOS repository](./package/rpm/README.md) or [using Docker](https://hub.docker.com/r/gbif/ipt/).  A Debian repository is now also available, see [this issue](https://github.com/gbif/ipt/pull/1470).

Version 2.4.2 includes a bugfix from an issue introduced by the  update in version 2.4.1.  It also allows streaming large datasets from large PostgreSQL databases, [see issue](https://github.com/gbif/ipt/issues?q=is%3Aissue+milestone%3A2.4.2+is%3Aclosed).

#### Previous releases

Version 2.4.1 includes a security update for Apache Struts, and other very [minor changes](https://github.com/gbif/ipt/issues?q=is%3Aissue+milestone%3A2.4.1+is%3Aclosed).

Version 2.4.0 includes [these changes](https://github.com/gbif/ipt/issues?q=is%3Aissue+milestone%3A2.4.1+is%3Aclosed).

Version 2.4.0 updates the integration with DataCite for assigning DOIs to datasets, has [minor bug fixes](https://github.com/gbif/ipt/issues?q=is%3Aissue+milestone%3A2.4.0+is%3Aclosed) and updates dependency versions to improve the robustness and security of the IPT.

Version 2.3.6 improves coverage of translations, has [minor bug fixes](https://github.com/gbif/ipt/issues?q=is%3Aissue+milestone%3A2.3.6+is%3Aclosed) and updates dependency versions to improve the robustness and security of the IPT.

Version 2.3.5 fixes cross site scripting vulnerabilities, and [an issue](https://github.com/gbif/ipt/issues/1344) that caused the dataset inventory web service request to fail.

Version 2.3.4 includes a security update that fixes a [critical vulnerability](https://struts.apache.org/docs/s2-045.html) that has been discovered in the Apache Struts web framework, which the IPT uses. According to [this article](http://thehackernews.com/2017/03/apache-struts-framework.html), this is a remote code execution vulnerability that could allow hackers to execute malicious commands on the IPT server. It also says that hackers are actively exploiting this vulnerability. **Therefore all users should plan to upgrade to this version immediately following the instructions in the [Release Notes](https://github.com/gbif/ipt/wiki/IPTReleaseNotes233.wiki).**

You can find out what features were added in version 2.3.3 in [this blog post](https://gbif.blogspot.com/2017/01/ipt-v233-your-repository-for.html).

### Next release

No release date has been set yet for the next release.  Progress working on issues for the next release can be browsed [here](https://github.com/gbif/ipt/projects/2).

Minor issues and security issues will be addressed in patch releases.

### @Users

If you're only interested in trying out the IPT please request an account on the [Demo IPT](https://ipt.gbif.org) by sending an email to helpdesk@gbif.org.

The simplest way to begin using the IPT is to request a free account on a [trusted data hosting centre](https://github.com/gbif/ipt/wiki/dataHostingCentres) allowing you to manage your own datasets and publish them through GBIF.org without the hassle of setting up and maintaining the IPT on your own server.

Otherwise if want to setup your own instance of the IPT the [Getting Started Guide](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#getting-started-guide) is your entry point.

_Be sure to sign up to the [IPT Mailing List](https://lists.gbif.org/mailman/listinfo/ipt/), which serves as a support group for IPT users. It is essential that the IPT is kept up to date to be as secure and robust as possible, so if you are responsible for administering an IPT, then you should be signed up to be notified of new releases so that you can update immediately._

### @Coders

The core development of the IPT is directed by GBIF, but the coding is a community effort and everyone is welcome to join. Start by browsing the [Open Issues](https://github.com/gbif/ipt/issues) to find something that you'd like to start working on. Kindly note that contributions are welcome in the form of a [pull request](https://help.github.com/articles/creating-a-pull-request/) using a branch or fork of the repository. Full instructions aimed at coders can be found [here](HowToContribute.wiki).

### @Translators

The IPT user interface and wiki both need internationalization, but it's a community effort and everyone is welcome to join. Full instructions aimed at translators can be found [here](https://github.com/gbif/ipt/wiki/HowToTranslate.wiki).

Thanks to an enormous community effort, and by leveraging the power of the [Crowding](https://crowdin.com/project/gbif-ipt) localization tool, the user interface has already been translated into seven different languages: English, French, Spanish, Traditional Chinese, Brazilian Portuguese, Japanese, and Russian.

### Acknowledgements

A large number of dedicated volunteers contribute to the success of this software. With your help, the IPT has become a successful tool in use all around the world.

[Crowdin](https://crowdin.com/) is kindly supporting this open source project by giving GBIF a free access to its localization management platform. Crowdin makes it possible to manage a large number of concurrent translations.
