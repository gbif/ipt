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

The DwC-A format and the specific profile described here represent an internationally recognised and ratified data exchange format for sharing sampling-event data. All data exchange standards must strike a balance between the technical scope and capacity on one hand, and social acceptance and uptake on the other. Simple solutions sacrifice coverage and complexity in favour of ease-of-use. Highly complex formats provide more complete solutions for representing any type of data but at the expense of simplicity and require supporting software and expertise. The Darwin Core Archive format represents an intermediate position between the two ends of this spectrum. It focuses on the key elements of sampling-event data and enables an enriched set of data types to be linked to this core structure. The data contained in an archive can be readily understood and used by many ecologists and data managers familiar with basic structured text files. By providing an international standard that is relatively easy to produce and consume, and that supports many of the key elements that compose a sampling-event data resource, GBIF hopes to provide the creators and managers of sampling-event data with a standardised approach to sharing their data and promote common approaches to the subsequent citation and recognition of their work. A standard format also increases relevance and utility.

## What is sampling-event data?

Sampling-event data is a type of data available from thousands of environmental, ecological, and natural resource investigations. These can be one-off studies or monitoring programmes. Such data are usually quantitative, calibrated, and follow certain protocols so that changes and trends of populations can be detected. This is in contrast to opportunistic observation and collection data, which today form a significant proportion of openly accessible biodiversity data. 

From the ecological perspective, sampling event data is qualitative presence-only or presence-absence data, or quantitative abundance data collected together with documented methodology, such as sampling technique, temporal, taxonomic, and spatial “envelope”, encompassing the reported observations.

To better understand the difference in data richness level, supported by GBIF [“onion” visualization based on Donald’s slides] one may think of checklist as of WHAT data, with name as the central element, of occurrence as of WHAT-WHERE-WHENs, individual observations, backed by the specimens or not. Sampling event data is different in richness and in packaging biodiversity data: events are WHERE-WHENs and occurrences WHAT-HOW-MUCH are listed for each of the event in a single table with optional quantitative data. Arguably the most important element of a dataset is metadata, containing WHO and HOW details.