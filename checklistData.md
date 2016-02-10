[[Resource metadata|resourceMetadata]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; **Checklist Data** &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Occurrence Data|occurrenceData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Sample Event Data|sampleEventData]]

---

# Checklist Data
Resources comprising a list of species belonging to some category (e.g. taxonomic, geographic, trait-based, red list, crop wild relative) and optionally with higher classification and/or additional traits associated with each species.  Examples of such datasets include global or regional taxonomic checklists, global or national red lists, catalogues of species included in undigitised collections, park checklists, etc. If sufficient information exists in the source dataset (or applies consistently to all species in the checklist), it is recommended that these datasets are presented as [Occurrence Data (below)](howToPublish#occurrence-data).  These datasets include the same basic descriptive information included under [Resource Metadata (above)](howToPublish#resource-metadata).

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
