# How To Apply a License To a Dataset

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