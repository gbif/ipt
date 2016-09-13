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
TODO
## Who did that?
TODO
## When did it take place?

| Fields | Requirements |
|:--------------- |:---------------|
| `eventDate` | The date, date-time, date range, or date-time range during which the Event occurred should be entered in `eventDate` in [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) format. Partial dates can be provided if they have at least a year and month, e.g. "2007-03". |
| `verbatimEventDate` | If the original value has to be converted into [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) `verbatimEventDate` should be filled in with the original value. |
| `eventTime`, `year`, `month`, `day`, `startDayOfYear` | Although it appears redundant, it is recommended trying to fill in `year`, `month`, `day`, `eventTime` and `startDayOfYear` for single dates/date-times. |
| `eventRemarks` | If no `eventDate` can be filled in, an explanation should be provided in `eventRemarks` |

#### Case 1: Single date
| Field | Value | Constraint |
|:--------------- |:---------------|:---------------|
| `eventDate` | 2007-03-20 | Must be in [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) format |
| `year` | 2007 | Must be four-digit year |
| `month` | 3 | Must be between 1-12 | 
| `day` | 20 | Must be between 1-31 |
| `startDayOfYear` | 60 | Must be between 1-365 |
| `verbatimEventDate` | "Mar 20, 07" | Original date or date description |

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

#### Case 4: Missing date
| Field | Value |
|:--------------- |:---------------|
| `eventRemarks` | "Event date was not found in legacy data" |

## Where did it take place?

| Fields | Requirements |
|:--------------- |:---------------|
| `decimalLatitude`, `decimalLongitude`, `geodeticDatum` | The point location coordinates should be entered in decimal degrees in `decimalLatitude` and `decimalLongitude`. The spatial reference system upon which the coordinates are based must be entered in `geodeticDatum` using the EPSG code, e.g. "EPSG:4326". |
|`footprintWKT`, `footprintSRS` | To provide a specific shape location enter a well-Known Text (WKT) representation of the shape in `footprintWKT`. The corresponding spatial reference system upon which the shape is based must be entered in `footprintSRS` using the EPSG code, e.g. "EPSG:4326". | 
|`coordinateUncertaintyInMeters`, `dataGeneralizations` | If the uncertainty of the GPS reading is known, use `coordinateUncertaintyInMeters` to express the uncertainty in meters, but make sure the value is reasonable. For large uncertainties check `dataGeneralizations` to see if the location was generalized on purpose, e.g. to protect sensitive species. |
|`verbatimCoordinates`, `verbatimLatitude`, `verbatimLongitude`, `verbatimCoordinateSystem`, `verbatimSRS` | If the original point location coordinates had to be converted from another coordinate system such as 'degrees minutes seconds' `verbatimCoordinates`, `verbatimLatitude`, `verbatimLongitude`, `verbatimCoordinateSystem`, `verbatimSRS` should be filled in with the original coordinates of the Location. |
| `dataGeneralizations` | If actions were taken to make the point location less specific than in its original form an explanation should be provided in `dataGeneralizations`. |
|`informationWitheld` | If the point location exists, but has not been entered, an explanation should be provided in `informationWitheld`. |
| `georeferenceRemarks` | If the point location does not exist, or the point location is calculated from the center of a grid cell (versus from GPS reading) an explanation should be provided in `georeferenceRemarks`. |
| `continent`, `waterBody`, `islandGroup`, `island`, `country`, `countryCode`, `stateProvince`, `county`, `municipality`, `locality`, `locationRemarks` | As much supplementary information as possible about the location should also be provided. If no `country` and `countryCode` can be provided then an explanation as to why should be entered in `locationRemarks` |

#### Case 1: Point location converted from degrees minutes seconds to decimal degrees
| Field | Value | Constraint |
|:--------------- |:---------------|:---------------|
| `decimalLatitude` | 42.4566 | Must be between -90 and 90, inclusive |
| `decimalLongitude` | -76.45442 | Must be between -180 and 180, inclusive |
| `geodeticDatum` | "EPSG:4326" | Must be an [EPSG code](http://spatialreference.org/ref/epsg/wgs-84/) |
| `coordinateUncertaintyInMeters` | 500 | Zero is NOT a valid value |
| `verbatimCoordinates` | 42° 27' 23.76", -76° 27' 15.91" | |
| `verbatimLatitude` | 42° 27' 23.76" | |
| `verbatimLongitude` | -76° 27' 15.91" | |
| `verbatimCoordinateSystem` | "degrees minutes seconds" | |
| `continent` | "North America" | Must be preferred English name according to [Getty Thesauraus of Georgraphic Names](http://www.getty.edu/research/tools/vocabularies/tgn/) |
| `country` | "United States" | Must be preferred English name according to [Getty Thesauraus of Georgraphic Names](http://www.getty.edu/research/tools/vocabularies/tgn/) |
| `countryCode` | "US" | Must be [ISO 3166-1-alpha-2 country code](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2) |
| `stateProvince` | "New York" | |
| `county` | "Tomkins County" | |
| `locality` | "Ithaca, Forest Home, CU Rifle Range" | Must be a specific description of the place |

#### Case 2: Point location that was generalized
| Field | Value |
|:--------------- |:---------------|
| `decimalLatitude` | 42.44 |
| `decimalLongitude` | -76.33 |
| `geodeticDatum` | "EPSG:4326" |
| `coordinateUncertaintyInMeters` | 5000 |
| `dataGeneralizations` | "Point location obscured by a factor of 5000m" |

#### Case 3: Point location exists but not provided
| Field | Value |
|:--------------- |:---------------|
| `informationWitheld` | "Point location hidden to protect sensitive species. Available upon request." |

#### Case 4: Point location does not exist
| Field | Value |
|:--------------- |:---------------|
| `dataGeneralizations` | "Point location was not found in legacy data" |

## Why did that happen?
TODO

## Dataset Metadata

The dataset metadata should contain enough information to facilitate reuse of the data while preventing misinterpretation. Publishers should also provide evidence of the rigor that went into producing the data while acknowledging its various contributors and funders. Ultimately this may lead to new sources of collaboration and funding.

| Field | Requirements | Example |
|:--------------- |:---------------|:---------------|
| `Title` | is a concise name that describes the contents of the dataset and that distinguishes it from others| _"Insects from light trap (1992–2009), rooftop Zoological Museum, Copenhagen"_|
| `Description` | is a short paragraph (abstract) describing the content of the dataset. | _"This dataset contains records of bony fishes and elasmobranchs collected by Reef Life Survey (RLS) divers along 50 m transects on shallow rocky and coral reefs, worldwide. Abundance information is available for all records found within quantitative survey limits (50 x 5 m swathes during a single swim either side of the transect line, each distinguished as a Block), and out-of-survey records are identified as presence-only (Method 0)."_ |
| `Project identifier` | is a GUID or other identifier that is near globally unique. _Note this is required for BID projects._ | _"BID-AF2015-0134-REG"_ |