# How To Apply a License To a Dataset

## Table of Contents
+ [[Introduction| IPT2ApplyingLicense.wiki#introduction]]
+ [[Dataset Level| IPT2ApplyingLicense.wiki#dataset-level]]
+ [[Record Level| IPT2ApplyingLicense.wiki#record-level]]
+ [[Supplementary Information| IPT2ApplyingLicense.wiki#supplementary-information]]
  + [[How To Manually Apply a License| IPT2ApplyingLicense.wiki#how-to-manually-apply-a-license]]

## Introduction

In accordance with the [GBIF licensing policy](http://www.gbif.org/terms/licences), a dataset should be made available for use under CC0, CC-BY, or CC-BY-NC. GBIF's recommended best practice is to use the most recent version, which 4.0 for CC-BY and CC-BY-NC. This is in line with Creative Commons' recommendation that: 

> [y]ou should always use the latest version of the Creative Commons licenses in order to take advantage of the many improvements described on the [license versions page](https://wiki.creativecommons.org/wiki/License_Versions). In particular, 4.0 is meant to be better suited to international use, and use in many different contexts, including [sharing data](https://blog.creativecommons.org/2011/08/23/data-governance-our-idea-for-the-moore-foundation/).

More information about what's new in the 4.0 CC license suite can be found [here](https://creativecommons.org/version4/). This is the version supported by the latest version of the IPT (v2.3.2).

The remainder of this page describes how to properly apply a license to a dataset, and how to ensure consistency at the dataset level and the record level.

Please note that CC0 is technically a waiver.

## Dataset Level

The license chosen must apply to the dataset as a whole. Only one license should be applied at the **dataset level**, even if multiple licenses are complimentary to each other (e.g. CC-BY and ODC-By).

To apply a license at the **dataset level** in the IPT, choose a license from the drop down on the basic metadata page:

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/v22/LicenseDropdownWide.png' />

In case different licenses apply to separate components of a dataset, the recommended best practice is to publish each component separately having their own metadata and identifier (e.g. DOI). This recommendation is in compliance with DataCite's recommended practice for such cases.

## Record Level

All licenses specified at the **record level** should comply with the license at the dataset level.

To apply a license at the **record level** using the IPT, publishers should use/map to the Darwin Core term [license](http://rs.tdwg.org/dwc/terms/index.htm#dcterms:license). As per Darwin Core's recommendation, the URI of the license should be used to populate the license. Be careful not to add contradictory usage restrictions in the Darwin Core term [accessRights](http://rs.tdwg.org/dwc/terms/index.htm#dcterms:accessRights). In theory, the license should provide sufficient access rights information without having to specify them in accessRights also.

Please note the Darwin Core term [rights](http://rs.tdwg.org/dwc/terms/history/#dcterms:rights) has now been deprecated and should no longer be used.

## Supplementary Information

### How To Manually Apply a License

Resource metadata can be populated automatically from an EML metadata document during resource creation. A license supplied in the EML document is interpretable so long as it is supplied in a machine readable format. 

To supply a license in EML in a machine readable format, use the `<ulink>` element inside `<intellectualRights><para>` to specify both the title and URL of the license. Be aware that documents complying with the GBIF Metadata Profile must use the latest version of the schema (v1.1) for this to be valid XML. 

Below is an example `<intellectualRights>` for the three licenses that GBIF supports demonstrating how to provide them in machine readable format. To apply, simply replace the `<intellectualRights>` block in your EML document with the `<intellectualRights>` below corresponding to the license of your choice.

##### Public Domain (CC0 1.0)
```
<intellectualRights>
   <para>To the extent possible under law, the publisher has waived all rights to these data and has dedicated them to the <ulink url="http://creativecommons.org/publicdomain/zero/1.0/legalcode"><citetitle>Public Domain (CC0 1.0)</citetitle></ulink>. Users may copy, modify, distribute and use the work, including for commercial purposes, without restriction</para>
</intellectualRights>
```
 
##### Creative Commons Attribution (CC-BY) 4.0
``` 
<intellectualRights>
  <para>This work is licensed under a <ulink url="http://creativecommons.org/licenses/by/4.0/legalcode"><citetitle>Creative Commons Attribution (CC-BY) 4.0 License</citetitle></ulink>.</para>
</intellectualRights>
```

##### Creative Commons Attribution Non Commercial (CC-BY-NC) 4.0
``` 
<intellectualRights>
  <para>This work is licensed under a <ulink url="http://creativecommons.org/licenses/by-nc/4.0/legalcode"><citetitle>Creative Commons Attribution Non Commercial (CC-BY-NC) 4.0 License</citetitle></ulink>.</para>
</intellectualRights>
```