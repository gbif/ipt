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

## What happened?
## Who did that?
## When did it take place?

| Fields | Requirements |
|:--------------- |:---------------|
| `eventDate`, `verbatimEventDate`, `year`, `month`, `day`, `eventTime`, `startDayOfYear` | The date, date-time, date range, or date-time range during which the Event occurred should be entered in `eventDate` in [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) format. If the original value has to be converted into [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) `verbatimEventDate` should be filled in with the original value. Partial dates can be provided if they have at least a year and month, e.g. "2007-03". Although it appears redundant, it is recommended trying to fill in `year`, `month`, `day`, `eventTime` and `startDayOfYear` for single dates/date-times. If no eventDate can be filled in, an explanation should be provided in `eventRemarks` |
#### Case 1: Single date
| Field | Value |
|:--------------- |:---------------|
| `eventDate` | 2007-03-20 |
| `year` | 2007 |
| `month` | 3 |
| `day` | 20 |
| `startDayOfYear` | 60 |
| `verbatimEventDate` | "Mar 20, 07" |

#### Case 2: Date-time range
| Field | Value |
|:--------------- |:---------------|
| `eventDate` | 2007-03-20T00:00:00Z/2007-03-27T06:00:00Z |
| `eventTime` | 00:00:00Z/06:00:00Z |
| `verbatimEventDate` | "The third week in March 07, for 6 hours starting at midnight." |

#### Case 3: Partial date
| Field | Value |
|:--------------- |:---------------|
| `eventDate` | 2007-03 |
| `year` | 2007 |
| `month` | 3 |
| `eventRemarks` | "Exact collection day was never recorded" |

#### Case 3: Missing date
| Field | Value |
|:--------------- |:---------------|
| `eventRemarks` | "Event date was not found in legacy data" |

## Where did it take place?

| Fields | Requirements | Examples |
|:--------------- |:---------------|:---------------|
| `decimalLatitude`, `decimalLongitude`, `geodeticDatum` | The point location coordinates should be entered in `decimalLatitude` and `decimalLongitude`. The spatial reference system upon which the coordinates are based must be entered in `geodeticDatum` using the EPSG code, e.g. "EPSG:4326". If the uncertainty of the GPS reading is known, use `coordinateUncertaintyInMeters` to express the uncertainty in meters, but make sure the value is reasonable. If the original point location coordinates had to be converted from another coordinate system such as 'degrees minutes seconds' `verbatimCoordinates`, `verbatimLatitude`, `verbatimLongitude`, `verbatimCoordinateSystem`, `verbatimSRS` should be filled in with the original coordinates of the Location. If actions were taken to make the point location less specific than in its original form an explanation should be provided in 'dataGeneralizations'. If the point location exists, but has not been entered, an explanation should be provided in `georeferenceRemarks`. | _"EPSG:4326", "WGS84"_|





## Why did that happen?

## Dataset Metadata

The dataset metadata should contain enough information to facilitate reuse of the data while preventing misinterpretation. Publishers should also provide evidence of the rigor that went into producing the data while acknowledging its various contributors and funders. Ultimately this may lead to new sources of collaboration and funding.

| Field | Requirements | Example |
|:--------------- |:---------------|:---------------|
| `Title` | is a concise name that describes the contents of the dataset and that distinguishes it from others| _"Insects from light trap (1992–2009), rooftop Zoological Museum, Copenhagen"_|
| `Description` | is a short paragraph (abstract) describing the content of the dataset. | _"This dataset contains records of bony fishes and elasmobranchs collected by Reef Life Survey (RLS) divers along 50 m transects on shallow rocky and coral reefs, worldwide. Abundance information is available for all records found within quantitative survey limits (50 x 5 m swathes during a single swim either side of the transect line, each distinguished as a Block), and out-of-survey records are identified as presence-only (Method 0)."_ |
| `Project identifier` | is a GUID or other identifier that is near globally unique. _Note this is required for BID projects._ | _"BID-AF2015-0134-REG"_ |