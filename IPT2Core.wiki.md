<font color='red'>
<i>Warning: This page contains configuration instructions for advanced IPT users only</i>
</font>

# How To Add a New Core



## Introduction

The IPT always ships with 2 cores installed by default: taxon and occurrence. Since IPT 2.1, there is now the possibility to add custom cores to the IPT, which is useful for communities prototyping new data standards.

[EU BON](http://www.eubon.eu/), for example, is currently working on a new [Event Core](http://rs.gbif.org/sandbox/core/dwc_event.xml), and have configured the [EU BON IPT](http://eubon-ipt.gbif.org/) to use it. In this way, their community can start trying to map their data to it, and iteratively refine its set of properties.

The 3 steps below explain how to 1) create your new core, 2) register it with GBIF, and 3) configure an IPT to use it.

## Instructions

  1. Write Core XML Definition
> > The core XML definition has to comply with the [GBIF Extension Schema](http://rs.gbif.org/schema/extension.xsd). It is easiest to simply adapt an existing core definition, such as EU BON's [Event Core](http://rs.gbif.org/sandbox/core/dwc_event.xml). Please note, the core definition must contain a property that serves as the identifier (e.g. http://rs.tdwg.org/dwc/terms/eventID for the [Event Core](http://rs.gbif.org/sandbox/core/dwc_event.xml)). The process of creating a new non-core extension is exactly the same as for a core extension. The process of creating a new vocabulary (as a data type for a property within the core, or non-core extension) is different only in that the XML definition has to comply with the [GBIF Thesaurus Schema](http://rs.gbif.org/schema/thesaurus.xsd). Once again, it is easiest to adapt an existing vocabulary definition, such as EU BON's [quantity type vocabulary](http://rs.gbif.org/sandbox/vocabulary/gbif/quantity-type.xml).
  1. Register Core with GBIF
> > While the core definition is still undergoing changes, it gets registered into the GBIF Sandbox Registry. To register your core, email your formatted and validated XML definition to the GBIF development team (dev@gbif.org). If it passes inspection, it will be uploaded to [http://rs.gbif.org/sandbox/core/](http://rs.gbif.org/sandbox/core/), and included in the [Sandbox Registry's list of extensions](http://gbrdsdev.gbif.org/registry/extensions.json). When the core definition has been finalized, meaning that its set of properties has been frozen, it will be hosted at [http://rs.gbif.org/core/](http://rs.gbif.org/core/) and included in the [Live Registry's list of extensions](http://gbrds.gbif.org/registry/extensions.json) The same process applies to registering non-core extensions and vocabularies.
  1. Configure IPT
> > To configure the IPT to use the [Event Core](http://rs.gbif.org/sandbox/core/dwc_event.xml), add the following 2 lines to $IPT\_DATA\_DIR/config/ipt.properties.
```
ipt.core_rowTypes=http://rs.tdwg.org/dwc/terms/Event 
ipt.core_idTerms=http://rs.tdwg.org/dwc/terms/eventID
```
> > This configures the IPT to recognize all extensions with rowType http://rs.tdwg.org/dwc/terms/Event as core types, and to use http://rs.tdwg.org/dwc/terms/eventID as its identifier term. Multiple cores can be specified, delimiting them with the pipe (|) character. The first entry of ipt.core\_idTerms is the ID for the first entry of core\_rowTypes, and so on. Lastly, save the ipt.properties file, restart Tomcat, and then [install the core](https://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes?tm=6#Install_extension). The core is now available to use in the IPT.

