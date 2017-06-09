## Welcome to the IPT Repository including Wiki, Issue Manager and Project Manager! 

Inside this repository you can find the [IPT User Manual](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki) and a variety of other valuable resources aimed at users, coders and translators. If you're searching for a more complete description of this software, its uptake statistics, release history, or roadmap, please visit the [IPT Website](http://www.gbif.org/ipt) instead.

### About the IPT

The Integrated Publishing Toolkit (IPT) is a free open source software tool written in Java that is used to publish and share biodiversity datasets through the GBIF network. The IPT can also be configured with either a DataCite or EZID account in order to assign DOIs to datasets transforming it into a data repository. 

### Latest Release: 2.3.4

Version 2.3.4 is available for download [here](http://repository.gbif.org/content/groups/gbif/org/gbif/ipt/2.3.4/ipt-2.3.4.war). This new version includes a security update that fixes a [critical vulnerability](https://struts.apache.org/docs/s2-045.html) that has been discovered in the Apache Struts web framework, which the IPT uses. According to [this article](http://thehackernews.com/2017/03/apache-struts-framework.html), this is a remote code execution vulnerability that could allow hackers to execute malicious commands on the IPT server. It also says that hackers are actively exploiting this vulnerability. **Therefore all users should plan to upgrade to this version immediately following the instructions in the [Release Notes](https://github.com/gbif/ipt/wiki/IPTReleaseNotes233.wiki).**


You can find out what features were added in version 2.3.3 in [this blog post](http://gbif.blogspot.dk/2017/01/ipt-v233-your-repository-for.html).

### Upcoming Release: 2.4

No release date has been set yet for version 2.4, however, progress working on issues included in this release can be browsed [here](https://github.com/gbif/ipt/projects/2).

### @Users

If you're only interested in trying out the IPT please request an account on the [Demo IPT](http://ipt.gbif.org) by sending an email to helpdesk@gbif.org. 

The simplest way to begin using the IPT is to request a free account on a [trusted data hosting centre](https://github.com/gbif/ipt/wiki/dataHostingCentres) allowing you to manage your own datasets and publish them through GBIF.org without the hassle of setting up and maintaining the IPT on your own server.

Otherwise if want to setup your own instance of the IPT the [Getting Started Guide](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#getting-started-guide) is your entry point. 

### @Coders 

The core development of the IPT is directed by GBIF, but the coding is a community effort and everyone is welcome to join. Start by browsing the [Open Issues](https://github.com/gbif/ipt/issues) to find something that you'd like to start working on. Kindly note that contributions are welcome in the form of a [pull request](https://help.github.com/articles/creating-a-pull-request/) using a branch or fork of the repository. Full instructions aimed at coders can be found [here](HowToContribute.wiki).

### @Translators

The IPT user interface and wiki both need internationalisation, but it's a community effort and everyone is welcome to join. Full instructions aimed at translators can be found [here](https://github.com/gbif/ipt/wiki/HowToTranslate.wiki).

Thanks to an enormous community effort, and by leveraging the power of the [Crowdin](https://crowdin.com/project/gbif-ipt) localisation tool, the user interface has already been translated into seven different languages: English, French, Spanish, Traditional Chinese, Brazilian Portuguese, Japanese, and Russian. 

### Acknowledgements

A large number of dedicated volunteers contribute to the success of this software. With your help, the IPT has become a successful tool in use all around the world.  

[JetBrains](http://www.jetbrains.com/) is kindly supporting this open source project by giving GBIF free open source licenses for its full-featured Java IDE - [IntelliJ](http://www.jetbrains.com/idea/). [JetBrains](http://www.jetbrains.com/) is the creator of a wide range of tools have been helping software developers be more efficient, no matter what technologies they use. Take a second now to look at their [leading software products](http://www.jetbrains.com/).

[Crowdin](https://crowdin.com/) is kindly supporting this open source project by giving GBIF a free access to its localisation management platform. Crowdin makes it possible to manage a large number of concurrent translations.

