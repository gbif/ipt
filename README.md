[![Build Status](https://builds.gbif.org/job/ipt/badge/icon?style=flat-square)](https://builds.gbif.org/job/ipt/)

## Welcome to the IPT Repository including source code, documentation and issues!

Inside this repository you can find the IPT source code, for the software and the [IPT User Manual](https://ipt.gbif.org/manual/), and the issue tracker.

Most users should look at the [IPT website](https://www.gbif.org/ipt) and [User Manual](https://ipt.gbif.org/manual/), and send any questions to the [IPT Mailing List](https://lists.gbif.org/mailman/listinfo/ipt).

### About the IPT

The Integrated Publishing Toolkit (IPT) is a free, open source software tool written in Java that is used to publish and share biodiversity datasets through the GBIF network. The IPT can also be configured with a DataCite account in order to assign DOIs to datasets transforming it into a data repository.

### Latest Release: 2.4.2

Version 2.4.2 is available for download [here](https://repository.gbif.org/content/groups/gbif/org/gbif/ipt/2.4.2/ipt-2.4.2.war), via a [CentOS repository](./package/rpm/README.md) or [using Docker](https://hub.docker.com/r/gbif/ipt/).  A Debian repository is now also available, see [this issue](https://github.com/gbif/ipt/pull/1470).

Version 2.4.2 includes a bugfix from an issue introduced by the update in version 2.4.1.  It also allows streaming large datasets from large PostgreSQL databases, [see issue](https://github.com/gbif/ipt/issues?q=is%3Aissue+milestone%3A2.4.2+is%3Aclosed).

### Next release

No release date has been set yet for the next release.  Progress working on issues for the next release can be browsed [here](https://github.com/gbif/ipt/milestones).

Minor issues and security issues will be addressed in patch releases.

### Users

If you're only interested in trying out the IPT please request an account on the [Demo IPT](https://ipt.gbif.org) by sending an email to helpdesk@gbif.org.

The simplest way to begin using the IPT is to request a free account on a [trusted data hosting centre](https://ipt.gbif.org/manual/en/ipt/2.5/data-hosting-centres/) allowing you to manage your own datasets and publish them through GBIF.org without the hassle of setting up and maintaining the IPT on your own server.

Otherwise if want to setup your own instance of the IPT the [Getting Started Guide](https://ipt.gbif.org/manual/en/ipt/2.5/getting-started/) is your entry point.

_Be sure to sign up to the [IPT Mailing List](https://lists.gbif.org/mailman/listinfo/ipt/), which serves as a support group for IPT users. It is essential that the IPT is kept up to date to be as secure and robust as possible, so if you are responsible for administering an IPT, then you should be signed up to be notified of new releases so that you can update immediately._

### Developers

The core development of the IPT is directed by GBIF, but the coding is a community effort and everyone is welcome to join. Start by browsing the [Open Issues](https://github.com/gbif/ipt/issues) to find something that you'd like to start working on. Kindly note that contributions are welcome in the form of a [pull request](https://help.github.com/articles/creating-a-pull-request/) using a branch or fork of the repository. Full instructions aimed at coders can be found [here](HowToContribute.wiki).

### Translators

The IPT user interface and wiki both need internationalization, but it's a community effort and everyone is welcome to join. Full instructions aimed at translators can be found [here](https://ipt.gbif.org/manual/en/ipt/2.5/translations/).

Thanks to an enormous community effort, and by leveraging the power of the [Crowdin](https://crowdin.com/project/gbif-ipt) localization tool, the user interface has already been translated into seven different languages: English, French, Spanish, Traditional Chinese, Brazilian Portuguese, Japanese, and Russian.

### Acknowledgements

A large number of dedicated volunteers contribute to the success of this software. With your help, the IPT has become a successful tool in use all around the world.

[Crowdin](https://crowdin.com/) is kindly supporting this open source project by giving GBIF a free access to its localization management platform. Crowdin makes it possible to manage a large number of concurrent translations.
