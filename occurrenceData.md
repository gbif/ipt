<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/arrow-back-24.png' />[[Back to instructions|howToPublish#instructions]]

[[Resource metadata|resourceMetadata]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Checklist Data|checklistData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; **Occurrence Data** &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Sampling Event Data|samplingEventData]]

---

# Occurrence Data

## Table of contents
+ [Introduction](occurrenceData#introduction)
+ [How to transform your data into occurrence data](occurrenceData#how-to-transform-your-data-into-occurrence-data)
+ [Templates](occurrenceData#templates)
+ [Required DwC fields](occurrenceData#required-dwc-fields)
+ [Recommended DwC fields](occurrenceData#recommended-dwc-fields)
+ [Exemplar datasets](occurrenceData#exemplar-datasets)
+ [FAQ](occurrenceData#faq)

### Introduction
Resources which present evidence of the occurrence of a species at a particular place and normally on a specified date.  These datasets expand on most Checklist Data because they contribute to mapping the historical or current distribution of a species. At the most basic, such datasets may provide only general locality information (even limited to a country identifier).  Ideally they also include coordinates and a coordinate precision to support fine scale mapping.  In many cases, these datasets may separately record multiple individuals of the same species. Examples of such datasets include databases of specimens in natural history collections, citizen science observations, data from species atlas projects, etc.  If sufficient information exists in the source dataset (or applies consistently to all occurrences in the dataset), it is recommended that these datasets are presented as [[Sampling Event Data|samplingEventData]].  These datasets include the same basic descriptive information included under [[Resource Metadata|resourceMetadata]].

### How to transform your data into occurrence data

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/flow-od.png' />

Ultimately your data needs to be transformed into a table structure using Darwin Core (DwC) term names as column names. 

Try putting your data into the [Excel template](occurrenceData#templates), which includes all [required DwC fields](occurrenceData#required-dwc-fields) and [recommended DwC fields](occurrenceData#recommended-dwc-fields). 

Alternatively if your data is stored in a [[supported database|IPT2DatabaseConnection.wiki]], you can write an SQL table (view) using DwC column names. Be careful to include all [required DwC fields](occurrenceData#required-dwc-fields) and add as many [recommended DwC fields](occurrenceData#recommended-dwc-fields) as possible. 

For extra guidance, you can look at the [exemplar datasets](occurrenceData#exemplar-datasets). 

You can augment your table with extra DwC columns, but only DwC terms from this [list](http://rs.gbif.org/core/dwc_occurrence_2015-07-02.xml).

#### Templates: 
[![Download Occurrence Data Template][2]][3]
[![Download Occurrence Data Template][4]][5]

Populate it and upload it to the IPT. Try to augment it with as many [DwC terms](http://rs.tdwg.org/dwc/terms/) as you can.

  [3]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/downloads/occurrence_ipt_template_v2.xlsx
  [2]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/excel-template2.png (Download Occurrence Data Template)
  [5]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/downloads/occurrence_ipt_template_v2_example_data.xlsx
  [4]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/excel-template-data2.png (Download Occurrence Data Template)


#### Required DwC fields: 
* [occurrenceID](http://rs.tdwg.org/dwc/terms/#occurrenceID)
* [basisOfRecord](http://rs.tdwg.org/dwc/terms/#basisOfRecord)
* [scientificName](http://rs.tdwg.org/dwc/terms/#scientificName)
* [eventDate](http://rs.tdwg.org/dwc/terms/#eventDate)

#### Recommended DwC fields: 
* [taxonRank](http://rs.tdwg.org/dwc/terms/#taxonRank) - to substantiate scientificName 
* [kingdom](http://rs.tdwg.org/dwc/terms/#kingdom) - and other higher taxonomy if possible 
* [decimalLatitude](http://rs.tdwg.org/dwc/terms/#decimalLatitude) & [decimalLongitude](http://rs.tdwg.org/dwc/terms/#decimalLongitude) & [geodeticDatum](http://rs.tdwg.org/dwc/terms/#geodeticDatum) - to provide a specific point location
* [countryCode](http://rs.tdwg.org/dwc/terms/#countryCode)
* [individualCount](http://rs.tdwg.org/dwc/terms/#individualCount) / [organismQuantity](http://rs.tdwg.org/dwc/terms/#organismQuantity) & [organismQuantityType](http://rs.tdwg.org/dwc/terms/#organismQuantityType) - to record the quantity of a species occurrence

#### Exemplar datasets: 
* CUMV Amphibian Collection (Arctos): [DwC-A](http://ipt.vertnet.org:8080/ipt/archive.do?r=cumv_amph) / [IPT homepage](http://ipt.vertnet.org:8080/ipt/resource.do?r=cumv_amph)

#### FAQ: 

##### Q. How do I indicate a species was absent?

**A.** Set [occurrenceStatus](http://rs.tdwg.org/dwc/terms/#occurrenceStatus)=["absent"](http://rs.gbif.org/vocabulary/gbif/occurrence_status.xml). In addition, [individualCount](http://rs.tdwg.org/dwc/terms/#individualCount) and [organismQuantity](http://rs.tdwg.org/dwc/terms/#organismQuantity) should be equal to 0. 

##### Q. How can I generalize sensitive species occurrence data?

**A.** How you generalize sensitive species data (e.g. restrict the resolution of the data) depends on the species' category of sensitivity. Note it is the responsibility of the publisher to protect sensitive species occurrence data. For guidance, please refer to this [best-practice guide](http://www.gbif.org/resource/80512). Additionally, you could refer to this [recent essay in Science](http://science.sciencemag.org/content/356/6340/800), which presents a simplified way to assess the risks from publishing sensitive species data using a simple scheme.

When generalizing data you should try not to reduce the value of the data for analysis, and make users aware how and why the original record was modified using the Darwin Core term [informationWithheld](http://rs.tdwg.org/dwc/terms/#informationWithheld). 

As indicated in the [best-practice guide](http://www.gbif.org/resource/80512), you should also publish a checklist of the sensitive species being generalized. For each species you should explain: 
* the rationale for inclusion in the list
* the geographic coverage of sensitivity
* its sensitivity category
* the date to review its sensitivity

This will help alert other data custodians that these species are regarded as potentially sensitive in a certain area and that they should take the sensitivity into account when publishing the results of their analyses, etc. 

###### Helpful formulas for generalizing point location  

A. The following formula obscures a latitude/longitude point by a factor of 5000m. Note pointX and pointY must be provided in 'length in meters' and TRUNC truncates the number to an integer by removing the decimal part:
```
pointX = TRUNC(pointX / 5000) * 5000
pointY = TRUNC(pointY / 5000) * 5000
```