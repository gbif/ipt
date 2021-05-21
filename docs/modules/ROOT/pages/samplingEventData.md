<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/arrow-back-24.png' />[[Back to instructions|howToPublish#instructions]]

[[Resource metadata|resourceMetadata]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Checklist Data|checklistData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Occurrence Data|occurrenceData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; **Sampling Event Data**

---

# Sampling Event Data

## Table of contents
+ [Introduction](samplingEventData#introduction)
+ [How to transform your data into sampling event data](samplingEventData#how-to-transform-your-data-into-sampling-event-data)
+ [Templates](samplingEventData#templates)
+ [Required DwC fields](samplingEventData#required-dwc-fields)
+ [Recommended DwC fields](samplingEventData#recommended-dwc-fields)
+ [Exemplar datasets](samplingEventData#exemplar-datasets)
+ [FAQ](samplingEventData#faq)

### Introduction
Resources which present evidence not only of the occurrence of a species at a particular place and time, but also sufficient detail to assess community composition for a broader taxonomic group or relative abundance of species at multiple times and places.  Such datasets derive from standardized protocols for measuring and observing biodiversity.  Examples include vegetation transects, standardized bird census data, ecogenomic samples, etc. These add to Occurrence Data by indicating what protocol was followed, which occurrence records derive from a sampling event following the protocol, and ideally the relative abundance (by a suitable numerical measure) of species recorded in the sample.  These additional elements can support better comparison of the data from different times and places (where the same protocol is indicated) and may in some cases enable researchers to infer absence of particular species from particular sites. These datasets include the same basic descriptive information included under [[Resource Metadata|resourceMetadata]] and the same standard elements as in [[Occurrence Data|occurrenceData]].

### How to transform your data into sampling event data

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/flow-sed.png' />

Ultimately your data needs to be transformed into two tables using Darwin Core (DwC) term names as column names: one table of sampling events and another table of species occurrences derived from (associated to) each sampling event.

Try putting your data into the [Excel template](samplingEventData#templates), which includes two sheets: one for sampling events and another for associated species occurrences. 

Alternatively if your data is stored in a [[supported database|IPT2DatabaseConnection.wiki]], you can write two SQL tables (views) using DwC column names: one for sampling events and another for associated species occurrences.

Each sampling event record should include all [required DwC fields](samplingEventData#required-dwc-fields) and as many [recommended DwC fields](samplingEventData#recommended-dwc-fields) as possible. You can augment your table with extra DwC columns, but only DwC terms from this [list](http://rs.gbif.org/core/dwc_event_2015_05_29.xml).

Similarly each species occurrence record should include all [required DwC fields](occurrenceData#required-dwc-fields) and as many [recommended DwC fields](occurrenceData#recommended-dwc-fields) as possible. You can augment your table with extra DwC columns, but only DwC terms from this [list](http://rs.gbif.org/core/dwc_occurrence_2015-07-02.xml). Some DwC terms will be redundant meaning they are added to both sampling event and species occurrence records. As a general rule, try not to add redundant terms with the same values. It is fine if they have different values though, for example if you wanted to define a location of an event and then define more specific locations for individual occurrences. Otherwise when the location of individual occurrences isn't supplied, its location gets inherited from the event.

For extra guidance, you can refer to the guide [Best Practices in Publishing Sampling-event data](https://github.com/gbif/ipt/wiki/BestPracticesSamplingEventData) and look at the [template populated with example data](samplingEventData#templates) or the list of [exemplar datasets](samplingEventData#exemplar-datasets). 

#### Templates: 
[![Download Sampling Event Data Template][2]][1]
[![Download Sampling Event Data Template][4]][3]

Populate it and upload it to the IPT.

  [1]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/downloads/event_ipt_template_v2.xlsx
  [2]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/excel-template2.png (Download Sampling Event Data Template)
  [3]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/downloads/event_ipt_template_v2_example_data.xlsx
  [4]: https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/excel-template-data2.png (Download Sampling Event Data Template)

#### Required DwC fields: 
* [eventID](http://rs.tdwg.org/dwc/terms/#eventID) - also required for associated occurrence data (to link them together)
* [eventDate](http://rs.tdwg.org/dwc/terms/#eventDate)
* [samplingProtocol](http://rs.tdwg.org/dwc/terms/#samplingProtocol)

#### Recommended DwC fields: 
* [sampleSizeValue](http://rs.tdwg.org/dwc/terms/#sampleSizeValue) & [sampleSizeUnit](http://rs.tdwg.org/dwc/terms/#sampleSizeUnit)
* [parentEventID](http://rs.tdwg.org/dwc/terms/#parentEventID) - in situations where the event is part of an event series
* [samplingEffort](http://rs.tdwg.org/dwc/terms/#samplingEffort) - to provide evidence of rigour of sampling event
* [locationID](http://rs.tdwg.org/dwc/terms/#locationID) - in situations where the plot/transect being sampled has a unique identifier
* [decimalLatitude](http://rs.tdwg.org/dwc/terms/#decimalLatitude) & [decimalLongitude](http://rs.tdwg.org/dwc/terms/#decimalLongitude) & [geodeticDatum](http://rs.tdwg.org/dwc/terms/#geodeticDatum) - to provide a specific point location
* [footprintWKT](http://rs.tdwg.org/dwc/terms/#footprintWKT) & [footprintSRS](http://rs.tdwg.org/dwc/terms/#footprintSRS) - to provide a specific shape location
* [countryCode](http://rs.tdwg.org/dwc/terms/#countryCode)
* [occurrenceStatus](http://rs.tdwg.org/dwc/terms/#occurrenceStatus) - only for associated occurrence data to record presence/absence data.

#### Exemplar datasets: 
* Israeli Butterfly Monitoring Scheme (BMS-IL): [DwC-A](http://cloud.gbif.org/eubon/archive.do?r=butterflies-monitoring-scheme-il) / [IPT homepage](http://cloud.gbif.org/eubon/resource?r=butterflies-monitoring-scheme-il)
* Dutch Vegetation Database (LVD): [DwC-A](http://cloud.gbif.org/eubon/archive.do?r=lvd) / [IPT homepage](http://cloud.gbif.org/eubon/resource?r=lvd)
* Reef Life Survey: Global reef fish dataset: [DwC-A](http://ipt.ala.org.au/archive.do?r=global) / [IPT homepage](http://ipt.ala.org.au/resource?r=global)
* Lepidurus arcticus survey Northeast Greenland 2013: [DwC-A](http://gbif.vm.ntnu.no/ipt/archive.do?r=lepidurus-arcticus-survey_northeast-greenland_2013) / [IPT homepage](http://gbif.vm.ntnu.no/ipt/resource?r=lepidurus-arcticus-survey_northeast-greenland_2013)
* Insects from light trap (1992–2009), rooftop Zoological Museum, Copenhagen: [DwC-A](http://danbif.au.dk/ipt/archive.do?r=rooftop&v=1.4) / [IPT homepage](http://danbif.au.dk/ipt/resource?r=rooftop)


#### FAQ:

##### Q. How do I indicate that a sampling event was part of a time series?

**A.** All sampling events at the same location must share the same [locationID](http://rs.tdwg.org/dwc/terms/#locationID).

##### Q. How do I publish a hierarchy of events (recursive data type) using parentEventID?

**A.** The classic example is sub-sampling of a larger plot. To group all (child) sub-sampling events under the (parent) sampling event, the parentEventID of all sub-sampling events must be set to the eventID of the (parent) sampling event. To be valid, all parentEventIDs must reference eventIDs of records defined in the same dataset. Otherwise, the parentEventID must be globally unique identifier (e.g. DOI, HTTP URI, etc) that resolves to an event record described elsewhere. Ideally, all (child) sub-sampling events share the same date and location as the (parent) event it references. 

##### Q. How do I publish absence data?

**A.** **Step #1**: Include sampling event records even if the sampling yielded no derived species occurrences. This allows species absences to be inferred. This [example sampling event dataset from Norway](http://gbif.vm.ntnu.no/ipt/resource?r=lepidurus-arcticus-survey_northeast-greenland_2013) demonstrates how this looks.  

Alternatively, you can make species absences explicit by adding a species occurrence record for each species that could have been observed at the time and place of sampling, but was not observed, by setting the following fields:

Mandatory:
* [occurrenceStatus](http://rs.tdwg.org/dwc/terms/#occurrenceStatus)=["absent"](http://rs.gbif.org/vocabulary/gbif/occurrence_status.xml)

Optional (provide one or both):
* [individualCount](http://rs.tdwg.org/dwc/terms/#individualCount)="0"
* [organismQuantity](http://rs.tdwg.org/dwc/terms/#organismQuantity) & [organismQuantityType](http://rs.tdwg.org/dwc/terms/#organismQuantityType) pair. "e.g. 0 individuals"

**Warning**: Currently GBIF indexes all species occurrences no matter if they ["present"](http://rs.gbif.org/vocabulary/gbif/occurrence_status.xml) or ["absent"](http://rs.gbif.org/vocabulary/gbif/occurrence_status.xml). Until this [issue](http://dev.gbif.org/issues/browse/POR-2864) is fixed, GBIF recommends excluding all species absences by using the following filter on the IPT’s Occurrence Mapping page:

```Filter: afterTranslation -> occurrenceStatus -> NotEquals -> absent```

More information about how to apply a filter can be found in the IPT User Manual [here](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#data-mapping-detail-page).

**Step #2**: Define the taxonomic scope of all sampling events included in the dataset, it is recommended to publish a timestamped checklist together with the sampling event dataset, which represents the species composition that could be observed at the time and place of sampling given the sampling protocol (and/or the taxonomic coverage of the study and the expertise of the personnel carrying out identification). This would allow for accurate presence/absence data being recorded. In addition to the normal (expected) species composition, the checklist could include invasive (unexpected) species. For taxonomic and biogeographical/ecological reasons, however, this checklist would exist solely within the context of the sampling event dataset. 

Instructions how to create a checklist can be found [here](https://github.com/gbif/ipt/wiki/checklistData). Detailed metadata should be included with the checklist describing a) the people who performed the identifications and their taxonomic expertise and b) how it was decided that these species were detectable & identifiable at the time and place of sampling.

To link the checklist to the sampling event dataset, add the checklist to the dataset metadata in the [External links](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#external-links) section. 