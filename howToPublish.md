## How to guide: Publishing Biodiversity Datasets through GBIF.org

This guide explains [what classes of biodiversity data can be published through GBIF.org](#what-types-of-biodiversity-datasets-can-be-published-through-gbiforg). For each data class you will find:

* A **description**, including its **recommended fields**
* A **spreadsheet template** used to capture this type of data, for upload to the IPT

This guide also explains how to publish the data through GBIF.org using the IPT.

### What types of biodiversity datasets can be published through GBIF.org?

GBIF supports publication, discovery and use of four classes of data. At the simplest, GBIF enables institutions to share information describing a biodiversity data resource – even when no further digital information is currently available from the resource. Other data classes support increasingly rich sharing of information on species, their distributions and abundance. Data publishers are strongly encouraged to share their data using the richest appropriate data class. This maximizes the usefulness of the data for users. 

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/4classes.png' />

#### Resource metadata

Description and contact details for a biodiversity information resource where no digital data can currently be shared.  All other classes of GBIF data also include this basic information.  Such metadata may be a valuable tool for researchers to discover resources which are not yet available online.  This is also a useful way to assess the importance and value of non-digital resources for future digitization. GBIF ensures that every dataset is associated with a Digital Object Identifier (DOI) to facilitate citation.

#### Checklist Data

Resources comprising a list of species belonging to some category (e.g. taxonomic, geographic, trait-based, red list, crop wild relative) and optionally with higher classification and/or additional traits associated with each species.  Examples of such datasets include global or regional taxonomic checklists, global or national red lists, catalogues of species included in undigitised collections, park checklists, etc. If sufficient information exists in the source dataset (or applies consistently to all species in the checklist), it is recommended that these datasets are presented as [Occurrence Data (below)](howToPublish#occurrence-data).  These datasets include the same basic descriptive information included under [Resource Metadata (above)](howToPublish#resource-metadata).

##### Required fields: 
* [taxonID](http://rs.tdwg.org/dwc/terms/#taxonID)
* [scientificName](http://rs.tdwg.org/dwc/terms/#scientificName)
* [taxonRank](http://rs.tdwg.org/dwc/terms/#taxonRank)

##### Recommended fields: 
* [kingdom](http://rs.tdwg.org/dwc/terms/#kingdom) - and other higher taxonomy if possible
* [parentNameUsageID](http://rs.tdwg.org/dwc/terms/#parentNameUsageID) - in case a taxonomy is meant to be published
* [acceptedNameUsageID](http://rs.tdwg.org/dwc/terms/#acceptedNameUsageID) - in case a taxonomy is meant to be published

[![Download Checklist Data Template][2]][1]

  [1]: https://gbif-spreadsheet-processor.googlecode.com/svn/trunk/templates/checklist/checklist-3_v1.xlsx
  [2]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/excel-template.png (Download Checklist Data Template)

#### Occurrence Data

Resources which present evidence of the occurrence of a species at a particular place and normally on a specified date.  These datasets expand on most Checklist Data because they contribute to mapping the historical or current distribution of a species. At the most basic, such datasets may provide only general locality information (even limited to a country identifier).  Ideally they also include coordinates and a coordinate precision to support fine scale mapping.  In many cases, these datasets may separately record multiple individuals of the same species. Examples of such datasets include databases of specimens in natural history collections, citizen science observations, data from species atlas projects, etc.  If sufficient information exists in the source dataset (or applies consistently to all occurrences in the dataset), it is recommended that these datasets are presented as [Sample Event Data (below)](howToPublish#sample-event-data).  These datasets include the same basic descriptive information included under [Resource Metadata (above)](howToPublish#resource-metadata).

##### Required fields: 
* [occurrenceID](http://rs.tdwg.org/dwc/terms/#occurrenceID)
* [basisOfRecord](http://rs.tdwg.org/dwc/terms/#basisOfRecord)
* [scientificName](http://rs.tdwg.org/dwc/terms/#scientificName)
* [eventDate](http://rs.tdwg.org/dwc/terms/#eventDate)
* [countryCode](http://rs.tdwg.org/dwc/terms/#countryCode)

##### Recommended fields: 
* [taxonRank](http://rs.tdwg.org/dwc/terms/#taxonRank) - to substantiate scientificName 
* [kingdom](http://rs.tdwg.org/dwc/terms/#kingdom) / [phylum](http://rs.tdwg.org/dwc/terms/#phylum) / [class](http://rs.tdwg.org/dwc/terms/#class) / [order](http://rs.tdwg.org/dwc/terms/#order) / [family](http://rs.tdwg.org/dwc/terms/#family) / [genus](http://rs.tdwg.org/dwc/terms/#family) - to substantiate scientificName 
* [decimalLatitude](http://rs.tdwg.org/dwc/terms/#decimalLatitude) / [decimalLongitude](http://rs.tdwg.org/dwc/terms/#decimalLongitude) / [geodeticDatum](http://rs.tdwg.org/dwc/terms/#geodeticDatum) - to provide a specific location

[![Download Occurrence Data Template][2]][1]

  [1]: https://gbif-spreadsheet-processor.googlecode.com/svn/trunk/templates/checklist/checklist-3_v1.xlsx
  [2]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/excel-template.png (Download Occurrence Data Template)

#### Sample Event Data

Resources which present evidence not only of the occurrence of a species at a particular place and time, but also sufficient detail to assess community composition for a broader taxonomic group or relative abundance of species at multiple times and places.  Such datasets derive from standardized protocols for measuring and observing biodiversity.  Examples include vegetation transects, standardized bird census data, ecogenomic samples, etc. These add to Occurrence Data by indicating what protocol was followed, which occurrence records derive from a sampling event following the protocol, and ideally the relative abundance (by a suitable numerical measure) of species recorded in the sample.  These additional elements can support better comparison of the data from different times and places (where the same protocol is indicated) and may in some cases enable researchers to infer absence of particular species from particular sites. These datasets include the same basic descriptive information included under [Resource Metadata (above)](howToPublish#resource-metadata) and the same standard elements as in [Occurrence Data (above)](howToPublish#occurrence-data).

[![Download Sample Event Data Template][2]][1]

  [1]: https://gbif-spreadsheet-processor.googlecode.com/svn/trunk/templates/checklist/checklist-3_v1.xlsx
  [2]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/excel-template.png (Sample Event Data Template)