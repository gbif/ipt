# Data Quality Checklist

## Introduction

Use this checklist to help review a dataset. 

To be complete, the data should contain valid answers to the five Ws: 

* [x] [What happened?](dataQualityChecklist#what-happened)
* [x] [Who did that?](dataQualityChecklist#who-did-that)
* [x] [When did it take place?](dataQualityChecklist#when-did-it-take-place)
* [x] [Where did it take place?](dataQualityChecklist#where-did-it-take-place)
* [x] [Why did that happen?](dataQualityChecklist#why-did-that-happen)

To facilitate reuse of the data, complimentary answers to the five Ws must also be provided in the [dataset metadata](dataQualityChecklist#dataset-metadata). 

## Checklist

### Dataset Metadata

The dataset metadata should contain enough information to facilitate reuse of the data while preventing misinterpretation. Publishers should also provide evidence of the rigor that went into producing the data while acknowledging its various contributors and funders. Ultimately this may lead to new sources of collaboration and funding.

| Field | Requirements | Example |
|:--------------- |:---------------|:---------------|
| `Title` | is a concise name that describes the contents of the dataset and that distinguishes it from others| _"Insects from light trap (1992–2009), rooftop Zoological Museum, Copenhagen"_|
| `Description` | is a short paragraph (abstract) describing the content of the dataset. | _"This dataset contains records of bony fishes and elasmobranchs collected by Reef Life Survey (RLS) divers along 50 m transects on shallow rocky and coral reefs, worldwide. Abundance information is available for all records found within quantitative survey limits (50 x 5 m swathes during a single swim either side of the transect line, each distinguished as a Block), and out-of-survey records are identified as presence-only (Method 0)."_ |
| `Project identifier` | is a GUID or other identifier that is near globally unique. _Note this is required for BID projects._ | _"BID-AF2015-0134-REG"_ |

### What happened?
### Who did that?
### When did it take place?

When information is missing or incomplete, a explanation should always be provided.

| Fields | Requirements | Example |
|:--------------- |:---------------|:---------------|
| `eventDate`, `verbatimEventDate`, `year`, `month`, `day`, `eventTime`, `startDayOfYear`, `endDayOfYear` | The date, date-time, date range, or date-time range during which the Event occurred in ISO 8601 format. If the original value has to be converted into ISO 8601 `verbatimEventDate` should be filled in. Partial dates can be provided if they have at least a year and month, e.g. "2007-03". Although it appears redundant, it is recommended trying to fill in `year`, `month`, `day`, `eventTime`, `startDayOfYear` and `endDayOfYear` for single dates/date-times. If no eventDate can be filled in, an explanation should be provided in `eventRemarks` | _"2007-03-01", "2016-09-12T15:28:48Z", "2007-03", "2007-03-01/2007-05-01"_|

### Where did it take place?
### Why did that happen?