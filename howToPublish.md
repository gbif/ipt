# How to publish biodiversity data through GBIF.org

## Table of Contents
+ [[Introduction|howToPublish#introduction]]
+ [[Instructions|howToPublish#instructions]]

## Introduction

GBIF supports publication, discovery and use of four classes of data. At the simplest, GBIF enables institutions to share information describing a biodiversity data resource – even when no further digital information is currently available from the resource. Other data classes support increasingly rich sharing of information on species, their distributions and abundance. 

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/4classes-no-text.png' />

[**Resource metadata**](howToPublish#resource-metadata) &nbsp;&nbsp;``—>``&nbsp;&nbsp; [**Checklist Data**](howToPublish#checklist-data) &nbsp;&nbsp;``—>``&nbsp;&nbsp; [**Occurrence Data**](howToPublish#occurrence-data) &nbsp;&nbsp;``—>``&nbsp;&nbsp; [**Sample Event Data**](howToPublish#sample-event-data)

Data publishers are strongly encouraged to share their data using the richest appropriate data class. This maximizes the usefulness of the data for users. 

## Instructions
To publish your data, follow these steps:

1. Determine the class of biodiversity data you have: [[Resource metadata|resourceMetadata]], [[Checklist Data|checklistData]], [[Occurrence Data|occurrenceData]], [[Sample Event Data|sampleEventData]]  
2. Ensure your organisation is registered with GBIF by completing [this online questionnaire](http://www.gbif.org/publishing-data/how-to-publish#/intro)
3. Transform your data into a table structure, using Darwin Core (DwC) terms as column names
  1. Try using an Excel template to structure your data, and understand what DwC terms are required and recommended
  2. It is possible to use data stored in a [[supported database|IPT2DatabaseConnection.wiki]], but ensure your table(s) included all DwC required terms 
4. Choose which GBIF Integrated Publishing Toolkit (IPT) you will use to publish your data:
  1. Save yourself time and money, and use a [[data hosting centre IPT|dataHostingCentres]] located in your country.
  2. Or, setup your own IPT if your organisation has the technical capacity
  3. If all else fails - contact the GBIF Helpdesk <helpdesk@gbif.org>
5. Upload your data to the IPT
  1. Refer to the [IPT User Manual](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki) for additional guidance
6. Map the data to Darwin Core
7. Fill in resource metadata
8. Publish the dataset (share it publicly online):
9. Register the dataset with GBIF, and make it globally discoverable on http://www.GBIF.org. Note: Step #2 (registration of your organisation with GBIF) must be completed first. This can take days or weeks in some cases, so please be patient.  

