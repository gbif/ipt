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

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/flow-sed.png' />

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

**Q.** How do I indicate that a sample event was part of a time series?

**A.** All sample events at the same location must share the same [locationID](http://rs.tdwg.org/dwc/terms/#locationID).

**Q.** How do I publish a hierarchy of events (recursive data type) using parentEventID?

**A.** The classic example is sub-sampling of a larger plot. To group all (child) sub-sampling events under the (parent) sampling event, the parentEventID of all sub-sampling events must be set to the eventID of the (parent) sampling event. To be valid, all parentEventIDs must reference eventIDs of records defined in the same dataset. Otherwise, the parentEventID must be globally unique identifier (e.g. DOI, HTTP URI, etc) that resolves to an event record described elsewhere. Ideally, all (child) sub-sampling events share the same date and location as the (parent) event it references. 

**Q.** How do I publish absence data?

**A.** Include sample event records even if the sampling yielded no derived species occurrences. This allows species absences to be inferred. This [example sample event dataset from Norway](http://gbif.vm.ntnu.no/ipt/resource?r=lepidurus-arcticus-survey_northeast-greenland_2013) demonstrates how this looks.  

Alternatively, if you can make species absences explicit by adding a species occurrence record for each species that could be observed at the time and place of sampling and by setting:

* [occurrenceStatus](http://rs.tdwg.org/dwc/terms/#occurrenceStatus)=["absent"](http://rs.gbif.org/vocabulary/gbif/occurrence_status.xml)
* [individualCount](http://rs.tdwg.org/dwc/terms/#individualCount)="0"
* [organismQuantity](http://rs.tdwg.org/dwc/terms/#organismQuantity)"0"

**Warning**: Currently GBIF indexes all species occurrences no matter if they "present" or "absent". Until this [issue](http://dev.gbif.org/issues/browse/POR-2864) is fixed, GBIF recommends applying the following filter on the IPT’s Occurrence Mapping page:

```Filter: afterTranslation -> occurrenceStatus -> NotEquals -> absent```

More information about how to apply a filter can be found in the IPT User Manual [here](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#data-mapping-detail-page).

To define the taxonomic scope of all sampling events included in the dataset, it is recommended to publish a timestamped checklist together with the sample event dataset, which represents the species composition that could be observed at the time and place of sampling given the sampling protocol (and/or the taxonomic coverage of the study and the expertise of the personnel carrying out identification). This would allow for accurate presence/absence data being recorded. In addition to the normal (expected) species composition, the checklist could include invasive (unexpected) species. For taxonomic and biogeographical/ecological reasons, however, this checklist would exist solely within the context of the sample event dataset. An investigation is needed how best to publish both datasets together since multiple DwC-As cannot be bundled together in the same .zip folder. 