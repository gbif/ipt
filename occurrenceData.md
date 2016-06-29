<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/arrow-back-24.png' />[[Back to instructions|howToPublish#instructions]]

[[Resource metadata|resourceMetadata]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Checklist Data|checklistData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; **Occurrence Data** &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Sample Event Data|sampleEventData]]

---

# Occurrence Data

## Table of contents
+ [Introduction](occurrenceData#introduction)
+ [How to transform your data into occurrence data](occurrenceData#how-to-transform-your-data-into-occurrence-data)
+ [Template](occurrenceData#template)
+ [Required DwC fields](occurrenceData#required-dwc-fields)
+ [Recommended DwC fields](occurrenceData#recommended-dwc-fields)
+ [Exemplar datasets](occurrenceData#exemplar-datasets)
+ [FAQ](occurrenceData#faq)

### Introduction
Resources which present evidence of the occurrence of a species at a particular place and normally on a specified date.  These datasets expand on most Checklist Data because they contribute to mapping the historical or current distribution of a species. At the most basic, such datasets may provide only general locality information (even limited to a country identifier).  Ideally they also include coordinates and a coordinate precision to support fine scale mapping.  In many cases, these datasets may separately record multiple individuals of the same species. Examples of such datasets include databases of specimens in natural history collections, citizen science observations, data from species atlas projects, etc.  If sufficient information exists in the source dataset (or applies consistently to all occurrences in the dataset), it is recommended that these datasets are presented as [[Sample Event Data|sampleEventData]].  These datasets include the same basic descriptive information included under [[Resource Metadata|resourceMetadata]].

### How to transform your data into occurrence data

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/flow-od.png' />

Ultimately your data needs to be transformed into a table structure using Darwin Core (DwC) term names as column names. 

Try putting your data into the [Excel template](occurrenceData#template), which includes all [required DwC fields](occurrenceData#required-dwc-fields) and [recommended DwC fields](occurrenceData#recommended-dwc-fields). 

Alternatively if your data is stored in a [[supported database|IPT2DatabaseConnection.wiki]], you can write an SQL table (view) using DwC column names. Be careful to include all [required DwC fields](occurrenceData#required-dwc-fields) and add as many [recommended DwC fields](occurrenceData#recommended-dwc-fields) as possible. 

For extra guidance, you can look at the [exemplar datasets](occurrenceData#exemplar-datasets). 

You can augment your table with extra DwC columns, but only DwC terms from this [list](http://rs.gbif.org/core/dwc_occurrence_2015-07-02.xml).

#### Template: 
[![Download Occurrence Data Template][2]][3]

Populate it and upload it to the IPT. Try to augment it with as many [DwC terms](http://rs.tdwg.org/dwc/terms/) as you can.

  [3]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/downloads/occurrence_ipt_template_v1.xlsx
  [2]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/excel-template.png (Download Occurrence Data Template)

#### Required DwC fields: 
* [occurrenceID](http://rs.tdwg.org/dwc/terms/#occurrenceID)
* [basisOfRecord](http://rs.tdwg.org/dwc/terms/#basisOfRecord)
* [scientificName](http://rs.tdwg.org/dwc/terms/#scientificName)
* [eventDate](http://rs.tdwg.org/dwc/terms/#eventDate)
* [countryCode](http://rs.tdwg.org/dwc/terms/#countryCode)

#### Recommended DwC fields: 
* [taxonRank](http://rs.tdwg.org/dwc/terms/#taxonRank) - to substantiate scientificName 
* [kingdom](http://rs.tdwg.org/dwc/terms/#kingdom) - and other higher taxonomy if possible 
* [decimalLatitude](http://rs.tdwg.org/dwc/terms/#decimalLatitude) & [decimalLongitude](http://rs.tdwg.org/dwc/terms/#decimalLongitude) & [geodeticDatum](http://rs.tdwg.org/dwc/terms/#geodeticDatum) - to provide a specific point location
* [individualCount](http://rs.tdwg.org/dwc/terms/#individualCount) / [organismQuantity](http://rs.tdwg.org/dwc/terms/#organismQuantity) & [organismQuantityType](http://rs.tdwg.org/dwc/terms/#organismQuantityType) - to record the quantity of a species occurrence

#### Exemplar datasets: 
* CUMV Amphibian Collection (Arctos): [DwC-A](http://ipt.vertnet.org:8080/ipt/archive.do?r=cumv_amph) / [IPT homepage](http://ipt.vertnet.org:8080/ipt/resource.do?r=cumv_amph)

#### FAQ: 

##### Q. How do I indicate a species was absent?

**A.** Set [occurrenceStatus](http://rs.tdwg.org/dwc/terms/#occurrenceStatus)=["absent"](http://rs.gbif.org/vocabulary/gbif/occurrence_status.xml). In addition, [individualCount](http://rs.tdwg.org/dwc/terms/#individualCount) and [organismQuantity](http://rs.tdwg.org/dwc/terms/#organismQuantity) should be equal to 0. 