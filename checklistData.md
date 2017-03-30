<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/arrow-back-24.png' />[[Back to instructions|howToPublish#instructions]]

[[Resource metadata|resourceMetadata]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; **Checklist Data** &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Occurrence Data|occurrenceData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Sampling Event Data|samplingEventData]]

---

# Checklist Data

## Table of contents
+ [Introduction](checklistData#introduction)
+ [How to transform your data into checklist data](checklistData#how-to-transform-your-data-into-checklist-data)
+ [Templates](checklistData#templates)
+ [Required DwC fields](checklistData#required-dwc-fields)
+ [Recommended DwC fields](checklistData#recommended-dwc-fields)
+ [Exemplar datasets](checklistData#exemplar-datasets)
+ [FAQ](checklistData#faq)

### Introduction
Resources comprising a list of species belonging to some category (e.g. taxonomic, geographic, trait-based, red list, crop wild relative) and optionally with higher classification and/or additional traits associated with each species.  Examples of such datasets include global or regional taxonomic checklists, global or national red lists, catalogues of species included in undigitised collections, park checklists, etc. If sufficient information exists in the source dataset (or applies consistently to all species in the checklist), it is recommended that these datasets are presented as [[Occurrence Data|occurrenceData]]. These datasets include the same basic descriptive information included under [[Resource Metadata|resourceMetadata]].

### How to transform your data into checklist data

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/flow-cd.png' />

Ultimately your data needs to be transformed into a table structure using Darwin Core (DwC) term names as column names. 

Try putting your data into the [Excel template](checklistData#templates), which includes all [required DwC fields](checklistData#required-dwc-fields) and [recommended DwC fields](checklistData#recommended-dwc-fields). 

Alternatively if your data is stored in a [[supported database|IPT2DatabaseConnection.wiki]], you can write an SQL table (view) using DwC column names. Be careful to include all [required DwC fields](checklistData#required-dwc-fields) and add as many [recommended DwC fields](checklistData#recommended-dwc-fields) as possible. 

For extra guidance, you can look at the [exemplar datasets](checklistData#exemplar-datasets). 

You can augment your table with extra DwC columns, but only DwC terms from this [list](http://rs.gbif.org/core/dwc_taxon_2015-04-24.xml).

#### Templates: 
[![Download Checklist Data Template][2]][1]
[![Download Checklist Data Template][4]][3]

Populate it and upload it to the IPT. Try to augment it with as many [DwC terms](http://rs.tdwg.org/dwc/terms/) as you can.

  [1]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/downloads/checklist_ipt_template_v1.xlsx
  [2]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/excel-template2.png (Download Checklist Data Template)
  [3]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/downloads/checklist_ipt_template_v1_example_data.xlsx
  [4]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/excel-template-data2.png (Download Checklist Data Template)
#### Required DwC fields: 
* [taxonID](http://rs.tdwg.org/dwc/terms/#taxonID)
* [scientificName](http://rs.tdwg.org/dwc/terms/#scientificName)
* [taxonRank](http://rs.tdwg.org/dwc/terms/#taxonRank)

#### Recommended DwC fields: 
* [kingdom](http://rs.tdwg.org/dwc/terms/#kingdom) - and other higher taxonomy if possible
* [parentNameUsageID](http://rs.tdwg.org/dwc/terms/#parentNameUsageID) - in situations where a taxonomy is meant to be published
* [acceptedNameUsageID](http://rs.tdwg.org/dwc/terms/#acceptedNameUsageID) - in situations where a taxonomy is meant to be published

#### Exemplar datasets: 
* Database of Vascular Plants of Canada (VASCAN): [DwC-A](http://data.canadensys.net/ipt/archive.do?r=vascan) / [IPT homepage](http://data.canadensys.net/ipt/resource.do?r=vascan)

#### FAQ: 
##### Q. **How do I add common names to a taxon record?** 

**A.** Make a table of common names. The table must include a taxonID column. That way, each row can link to the (core) taxon record. You can augment your common names table with extra columns, but only using term names from this [list](http://rs.gbif.org/extension/gbif/1.0/vernacularname.xml). You can upload this table to the IPT, and map it to the [Vernacular Name extension](http://rs.gbif.org/extension/gbif/1.0/vernacularname.xml). 

##### Q. **How do I add the threat status of a species as defined by IUCN?** 

**A.** Make a table of geographic distributions of a taxon. The table must include a taxonID column. That way, each row can link to the (core) taxon record. You can augment your geographic distributions table with extra columns such as the threat status, but only using term names from this [list](http://rs.gbif.org/extension/gbif/1.0/distribution.xml). You can upload this table to the IPT, and map it to the [Species Distribution extension](http://rs.gbif.org/extension/gbif/1.0/distribution.xml). 

##### Q. **How can I update the [GBIF Backbone Taxonomy](http://www.gbif.org/dataset/d7dddbf4-2cf0-4f39-9b2a-bb099caae36c) with names from my checklist?** 

**A.** First, you must publish your checklists, making it publicly available under a GBIF-supported license (CC0, CC-BY, CC-BY-NC). GBIF can then manually review it to determine if it is a suitable backbone source, e.g. by looking at how its names overlap with the backbone. Ideally the checklist will use minimal classification, and be of high data quality meaning it has few name usage issues, include [scientificNameAuthorship](http://rs.tdwg.org/dwc/terms/#scientificNameAuthorship) of names, supplying the [namePublishedIn](http://rs.tdwg.org/dwc/terms/#namePublishedIn) reference, etc 