# Best Practices in Publishing Sampling-event data
<sup>Version 2.0</sup>

## Document control

| Version | Description             | Date of release | Author(s)    |
|---------|-------------------------|-----------------|--------------|
| [1.0](http://links.gbif.org/ipt-sample-data-primer)   | Release Version         | 2015    | Éamonn Ó Tuama |
| 2.0 | Transferred to wiki, major changes | May 2017   | Kyle Braak |

## Suggested citation

> GBIF (2017). Best Practices in Publishing Sampling-event data. Accessible online at https://github.com/gbif/ipt/wiki/BestPracticesSamplingEventData

## Introduction

This guide provides details on how to utilise the Darwin Core Archive (DwC-A) format as a means to share sampling-event information in a standard way. It focuses on specific components of the Darwin Core Archive format, and some of the supporting extensions to the core event data class, and provides recommendations on how to best utilise these components to maximise the value of the shared data. This guide does not provide a detailed overview of the Darwin Core Archive format, instead please refer to the [Darwin Core Archives How-to Guide](https://github.com/gbif/ipt/wiki/DwCAHowToGuide).

The DwC-A format and the specific profile described here represent an internationally recognised and ratified data exchange format for sharing sampling-event data. All data exchange standards must strike a balance between the technical scope and capacity on one hand, and social acceptance and uptake on the other. Simple solutions sacrifice coverage and complexity in favour of ease-of-use. Highly complex formats provide more complete solutions for representing any type of data but at the expense of simplicity and require supporting software and expertise. The Darwin Core Archive format represents an intermediate position between the two ends of this spectrum. It focuses on the key elements of sampling-event data and enables an enriched set of data types to be linked to this core structure. The data contained in an archive can be readily understood and used by many ecologists and data managers familiar with basic structured text files. By providing an international standard that is relatively easy to produce and consume, and that supports many of the key elements that compose a sampling-event data resource, GBIF hopes to provide the creators and managers of sampling-event data with a standardised approach to sharing their data and promote common approaches to the subsequent citation and recognition of their work. The specific degree of coverage depends very much on the individual resource. A standard format also increases relevance and utility.

## What is sampling-event data?

Sampling-event data is a type of data available from thousands of environmental, ecological, and natural resource investigations. These can be one-off studies or monitoring programmes. Such data are usually quantitative, calibrated, and follow certain protocols so that changes and trends of populations can be detected. This is in contrast to opportunistic observation and collection data, which today form a significant proportion of openly accessible biodiversity data. 

### How to express sampling-event data in DwC-A?

Darwin Core Archive (DwC-A) is an informatics data standard that makes use of the Darwin Core terms to produce a single, self-contained dataset for checklist data. The collection of files in an archive form a self-contained dataset, which can be provided as a single compressed (Zip or GZIP) file. A dataset is composed of a descriptive metadata document and a set of one or more data files. For more information about DwC-A refer to the [Darwin Core Archives How-to Guide](https://github.com/gbif/ipt/wiki/DwCAHowToGuide).

### Sampling-event Metadata

Documenting the provenance and scope of datasets is required in order to publish sampling-event data through the GBIF network. Dataset documentation is referred to as ‘resource metadata’ and enables users to evaluate the fitness-for-use of a dataset. It may describe the sampling methodologies used for its compilation, and the individuals and organisations involved in its creation and management. Metadata is shared in a Darwin Core Archive as an XML document. GBIF provides a metadata profile for sampling-event datasets based on the Ecological Metadata Language. A How-to guide describes all the options for describing a sampling-event dataset using this format. See <http://links.gbif.org/gbif_metadata_profile_how-to_en_v1>

### Sampling-event Data

The Darwin Core Archive format provides the structural framework for publishing sampling-event data. Darwin Core Archives consist of a series of one or more text files, in standard comma- or tab-delimited format. The files are logically arranged in a star-like manner with one ***core file***, listing the sampling events (sampling protocol, sample size, location, etc.) surrounded by a number of ‘***extensions***’, that describe related data types (such as species occurrences, measurements or facts related to the sampling event, etc). Links between core and extension records are made using an event identifier (***eventID***) data element. In this way, many extension records can exist for each single core event record. This “star-schema” provides a simple relational data model that supports many types of annotations that are common to sampling-event datasets.

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/figures/dwc-a_event.png' width="600" height="400" />

Figure 1. Darwin Core Archive data files in 'star schema'

An alternative way to encode sampling-event data is listing the species occurrence in the core file, surrounded by a number of extensions, that describe the related data types (such as measurements relating to the species occurrences, etc). Note listing sampling events in the core file is preferable if a plot or site is the main focus of the study.

> TODO: Provide recommendations on how to work around limitations of DwC-A star schema such as not being able to relate measurements and facts to both events and occurrences in the same dataset. The current work around requires publishers to publish separate datasets. Note OBIS is prototyping an Extended Measurement or Facts Extension that could also help overcome this limitation. Discussion on this prototype extension is taking place in GitHub here. However, issues raised that this prototype extension does not explicitly make it clear if the measurement or fact relates to an occurrence or an event. One alternative is to add resourceID (and perhaps resourceType?) instead of adding eventID (and occurrenceID) as attribute to the measurement or fact extension as is explored by the OBIS extension. 

### Data file formatting recommendations

For ease in understanding, we may use the terms ***field*** in this guide to refer to the Darwin Core set of terms in the sampling-event publishing profile to which a users data will be mapped. For example, we will refer to the use of the ***dwc:scientificName field*** when referring to the Darwin Core term, ***scientificName***.

-   It is recommended to use TAB or Comma-Separated-Values instead of custom field delimiters and quotes.
-   Be careful and consistent with quotation.
-   Encode text files as UTF-8
-   Make sure you replace all line breaks in a data field, i.e. \\r \\n or \\r\\n with either simple spaces or use 2 characters like “$$” to replace "\\r" to escape the line break if the intention is to preserve them. Another option is to replace line breaks with the html &lt;br&gt; tag.
-   Encode NULLs as empty strings, i.e. no characters between 2 delimiters, or \\N or \\NULL, but no other text sequence!

### Sample size
The following Darwin Core fields store the sample size of a sampling event:
* **sampleSizeValue**: a numeric value for a measurement of the size (time duration, length, area, or volume) of a sample in a sampling event. 
* **sampleSizeUnit**: the unit of measurement of the size (time duration, length, area, or volume) of a sample in a sampling event. 

The value of sampleSizeValue is a number and must have a corresponding sampleSizeUnit. The value of sampleSizeUnit should be restricted to use only SI units/derived units or other non-SI units accepted for use within the SI (e.g. minute, hour, day, litre) as per the [Unit of Measurement Vocabulary](http://rs.gbif.org/vocabulary/gbif/unit_of_measurement_2015-07-10.xml). Examples are given in Table 1 below.

> TODO: Provide recommendations on how to represent the sampling area by choosing the appropriate WKT shape or simple latitude/longitude point location. Done correctly, the direction sampling was carried out can also be derived. For example, an ocean trawl line represented using a WKT shape LINESTRING allows the direction of the trawl to be determined based on the standard notation for writing the start and end points.

**Table 1. sampleSizeValue and sampleSizeUnit must be used together, e.g., 3 square metres, or 1 litre.**

| sampleSizeValue | sampleSizeUnit |
|--|--|
| 2 | hour |
| 3 | m2 |
| 17 | km |
| 1 | litre |
