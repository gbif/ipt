# Data Quality Checklist

## Introduction

Use this checklist to help review a dataset and ensure it is both complete and valid.

To be complete, the data should contain answers the five Ws: 

* [x] [What happened?](https://github.com/gbif/ipt/wiki/dataQualityChecklist#what-happened)
* [x] [Who did that?](https://github.com/gbif/ipt/wiki/dataQualityChecklist#who-did-that)
* [x] [When did it take place?](https://github.com/gbif/ipt/wiki/dataQualityChecklist#when-did-it-take-place)
* [x] [Where did it take place?](https://github.com/gbif/ipt/wiki/dataQualityChecklist#where-did-it-take-place)
* [x] [Why did that happen?](https://github.com/gbif/ipt/wiki/dataQualityChecklist#why-did-that-happen)

When information for one or more Ws is missing, a plausible explanation should still be provided. Otherwise users will think data is missing for lack of effort, not because it doesn't exist for example. 

In parallel, answers to the five Ws should also be provided in the dataset metadata. The more complete the metadata is, the more it will facilitate reuse of the dataset while preventing misinterpretation of the data. Provide evidence of the rigor that went into producing the data and acknowledge its various contributors and funders. Who knows, it may also lead to new sources of collaboration and funding if others' are impressed by your work.

## Checklist

### Metadata

| Field | Requirements | Example |
|:--------------- |:---------------|:---------------|
| `Title` | is a concise name that describes the contents of the dataset and that distinguishes it from others| _"Insects from light trap (1992–2009), rooftop Zoological Museum, Copenhagen"_|
| `Description` | is a short paragraph (abstract) describing the content of the dataset. | _"This dataset contains records of bony fishes and elasmobranchs collected by Reef Life Survey (RLS) divers along 50 m transects on shallow rocky and coral reefs, worldwide. Abundance information is available for all records found within quantitative survey limits (50 x 5 m swathes during a single swim either side of the transect line, each distinguished as a Block), and out-of-survey records are identified as presence-only (Method 0)."_ |
| `Project identifier` | is a GUID or other identifier that is near globally unique. _Note this is required for BID projects._ | _"BID-AF2015-0134-REG"_ |

### What happened?
### Who did that?
### When did it take place?
### Where did it take place?
### Why did that happen?