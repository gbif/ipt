[[Resource metadata|resourceMetadata]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; **Checklist Data** &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Occurrence Data|occurrenceData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Sample Event Data|sampleEventData]]

---

# Checklist Data
Resources comprising a list of species belonging to some category (e.g. taxonomic, geographic, trait-based, red list, crop wild relative) and optionally with higher classification and/or additional traits associated with each species.  Examples of such datasets include global or regional taxonomic checklists, global or national red lists, catalogues of species included in undigitised collections, park checklists, etc. If sufficient information exists in the source dataset (or applies consistently to all species in the checklist), it is recommended that these datasets are presented as [Occurrence Data (below)](howToPublish#occurrence-data).  These datasets include the same basic descriptive information included under [Resource Metadata (above)](howToPublish#resource-metadata).

### How to transform your data into checklist data

Ultimately your data needs to be transformed into a table structure using Darwin Core (DwC) term names as column names. 

Try putting your data into the [Excel template](checklistData#template), which includes all [required DwC fields](checklistData#required-dwc-fields) and [recommended DwC fields](checklistData#recommended-dwc-fields). 

Alternatively if your data is stored in a [[supported database|IPT2DatabaseConnection.wiki]], you can write an SQL table (view) using DwC column names. Be careful to include all [required DwC fields](checklistData#required-dwc-fields) and add as many [recommended DwC fields](checklistData#recommended-dwc-fields) as possible. 

For extra guidance, you can look at the [exemplar datasets](checklistData#exemplar-datasets). 

You can augment your table with extra DwC columns, but only DwC terms from this [list](http://rs.gbif.org/core/dwc_taxon_2015-04-24.xml).

#### Required DwC fields: 
* [taxonID](http://rs.tdwg.org/dwc/terms/#taxonID)
* [scientificName](http://rs.tdwg.org/dwc/terms/#scientificName)
* [taxonRank](http://rs.tdwg.org/dwc/terms/#taxonRank)

#### Recommended DwC fields: 
* [kingdom](http://rs.tdwg.org/dwc/terms/#kingdom) - and other higher taxonomy if possible
* [parentNameUsageID](http://rs.tdwg.org/dwc/terms/#parentNameUsageID) - in case a taxonomy is meant to be published
* [acceptedNameUsageID](http://rs.tdwg.org/dwc/terms/#acceptedNameUsageID) - in case a taxonomy is meant to be published

#### Template: 
[![Download Checklist Data Template][2]][1]

Populate it and upload it to the IPT. Try to augment it with as many [DwC terms](http://rs.tdwg.org/dwc/terms/) as you can.

  [1]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/downloads/checklist_ipt_template_v1.xlsx
  [2]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/excel-template.png (Download Checklist Data Template)

#### Exemplar datasets: 
* Database of Vascular Plants of Canada (VASCAN): [DwC-A](http://data.canadensys.net/ipt/archive.do?r=vascan) / [IPT homepage](http://data.canadensys.net/ipt/resource.do?r=vascan)
