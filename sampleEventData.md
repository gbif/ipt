<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/arrow-back-24.png' />[[Back to instructions|howToPublish#instructions]]

[[Resource metadata|resourceMetadata]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Checklist Data|checklistData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Occurrence Data|occurrenceData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; **Sample Event Data**

---

# Sample Event Data

## Table of contents
+ [Introduction](sampleEventData#introduction)
+ [How to transform your data into sample event data](sampleEventData#how-to-transform-your-data-into-sample-event-data)
+ [Template](sampleEventData#template)
+ [Required DwC fields](sampleEventData#required-dwc-fields)
+ [Recommended DwC fields](sampleEventData#recommended-dwc-fields)
+ [Exemplar datasets](sampleEventData#exemplar-datasets)
+ [FAQ](sampleEventData#faq)

### Introduction
Resources which present evidence not only of the occurrence of a species at a particular place and time, but also sufficient detail to assess community composition for a broader taxonomic group or relative abundance of species at multiple times and places.  Such datasets derive from standardized protocols for measuring and observing biodiversity.  Examples include vegetation transects, standardized bird census data, ecogenomic samples, etc. These add to Occurrence Data by indicating what protocol was followed, which occurrence records derive from a sampling event following the protocol, and ideally the relative abundance (by a suitable numerical measure) of species recorded in the sample.  These additional elements can support better comparison of the data from different times and places (where the same protocol is indicated) and may in some cases enable researchers to infer absence of particular species from particular sites. These datasets include the same basic descriptive information included under [[Resource Metadata|resourceMetadata]] and the same standard elements as in [[Occurrence Data|occurrenceData]].

### How to transform your data into sample event data

Ultimately your data needs to be transformed into a table structure using Darwin Core (DwC) term names as column names. 

Try putting your data into the [Excel template](sampleEventData#template), which includes all [required DwC fields](sampleEventData#required-dwc-fields) and [recommended DwC fields](sampleEventData#recommended-dwc-fields). 

Alternatively if your data is stored in a [[supported database|IPT2DatabaseConnection.wiki]], you can write an SQL table (view) using DwC column names. Be careful to include all [required DwC fields](sampleEventData#required-dwc-fields) and add as many [recommended DwC fields](sampleEventData#recommended-dwc-fields) as possible. 

For extra guidance, you can look at the [exemplar datasets](sampleEventData#exemplar-datasets). 

You can augment your table with extra DwC columns, but only DwC terms from this [list](http://rs.gbif.org/core/dwc_event_2015_05_29.xml).

#### Template: 
[![Download Sample Event Data Template][2]][1]

Populate it and upload it to the IPT.

  [1]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/downloads/event_ipt_template_v1.xlsx
  [2]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/excel-template.png (Sample Event Data Template)

#### Required DwC fields: 
* [eventID](http://rs.tdwg.org/dwc/terms/#eventID) - also required for associated occurrence data (to link them together)
* [eventDate](http://rs.tdwg.org/dwc/terms/#eventDate)
* [countryCode](http://rs.tdwg.org/dwc/terms/#countryCode)
* [samplingProtocol](http://rs.tdwg.org/dwc/terms/#samplingProtocol)
* [sampleSizeValue](http://rs.tdwg.org/dwc/terms/#sampleSizeValue) & [sampleSizeUnit](http://rs.tdwg.org/dwc/terms/#sampleSizeUnit)

#### Recommended DwC fields: 
* [parentEventID](http://rs.tdwg.org/dwc/terms/#parentEventID) - in situations where the event is part of an event series
* [samplingEffort](http://rs.tdwg.org/dwc/terms/#samplingEffort) - to provide evidence of rigour of sampling event
* [locationID](http://rs.tdwg.org/dwc/terms/#locationID) - in situations where the plot/transect being sampled has a unique identifier
* [decimalLatitude](http://rs.tdwg.org/dwc/terms/#decimalLatitude) & [decimalLongitude](http://rs.tdwg.org/dwc/terms/#decimalLongitude) & [geodeticDatum](http://rs.tdwg.org/dwc/terms/#geodeticDatum) - to provide a specific point location
* [footprintWKT](http://rs.tdwg.org/dwc/terms/#footprintWKT) - to provide a specific shape location
* [occurrenceStatus](http://rs.tdwg.org/dwc/terms/#occurrenceStatus) - only for associated occurrence data to record presence/absence data.

#### Exemplar datasets: 
* Israeli Butterfly Monitoring Scheme (BMS-IL): [DwC-A](http://cloud.gbif.org/eubon/archive.do?r=butterflies-monitoring-scheme-il&v=6.12) / [IPT homepage](http://cloud.gbif.org/eubon/resource?r=butterflies-monitoring-scheme-il)

#### FAQ:

**Q.** How do indicate that a sample event was part of a time series?

**A.** Populate [parentEventID](http://rs.tdwg.org/dwc/terms/#parentEventID) with an ID common to all sampling events at the same location.  