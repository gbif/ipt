# How to publish biodiversity data through GBIF.org

## Table of Contents
+ [[Introduction|howToPublish#introduction]]
+ [[Prerequisites|howToPublish#prerequisites]]
+ [[Instructions|howToPublish#instructions]]

## Introduction

GBIF supports publication, discovery and use of four classes of data:

* [[Resource metadata|resourceMetadata]]
* [[Checklist Data|checklistData]] 
* [[Occurrence Data|occurrenceData]] 
* [[Sampling Event Data|samplingEventData]]  

At the simplest, GBIF enables sharing information describing a biodiversity data resource – even when no further digital information is currently available from the resource. Other data classes support an increasingly richer and wider range of information on species, their distributions and abundance.

Data publishers are strongly encouraged to share their data using the richest appropriate data class. This maximizes the usefulness of the data for users.

To give yourself an introduction to how the IPT can be used to publish biodiversity data through GBIF.org, it's highly recommended watching this concise 25 minute live demo below:

<a href="https://vimeo.com/221572634" target="_blank"><img src="https://i.vimeocdn.com/video/639873882_1280x720.jpg" alt="How to publish biodiversity data through GBIF.org using the IPT" width="640" height="384" border="10" /></a>

## Prerequisites
1. You require an account on a GBIF Integrated Publishing Toolkit (IPT) to publish your data. 
  
    * Hint: it is highly recommended that you save yourself time and money by requesting an account on a [[data hosting centre IPT|dataHostingCentres]] that is located in your country. 
    * Hint: you could install and maintain your own IPT instance if you have technical skills and capacity to maintain it online near 100% of the time.  
    * Hint: if no data hosting centre exists in your country, and you or your organisation don't have the technical skills and capacity to host an IPT, you can contact the GBIF Helpdesk <helpdesk@gbif.org> for assistance. 
2. Assuming that you would like to register your dataset with GBIF and make it globally discoverable via http://www.gbif.org, your dataset must be affiliated with an organisation that is registered with GBIF. 
    * Hint: to register your organisation with GBIF, start by completing [this online questionnaire](http://www.gbif.org/publishing-data/request-endorsement#/intro). The registration process can take days, so in parallel you can proceed to publish your data. 
    * Hint: if you aren't affiliated with any organisation, you can contact the GBIF Helpdesk <helpdesk@gbif.org> for assistance. In the meantime, you can proceed to publish your data. 

## Instructions 

To publish your data, follow the 7 steps below. 

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/flow-all.png' />

1. Select the class of biodiversity data you have from this list: 
    * [[Resource metadata|resourceMetadata]]
    * [[Checklist Data|checklistData]]
    * [[Occurrence Data|occurrenceData]]
    * [[Sampling Event Data|samplingEventData]]  
2. Transform your data into a table structure, using Darwin Core (DwC) terms as column names
    * Hint: try using an Excel template to structure your data, and understand what DwC terms are required and recommended
    * Hint: it is possible to use data stored in a [[supported database|IPT2DatabaseConnection.wiki]]
3. Upload your data to the IPT
    * Hint: refer to the [IPT User Manual](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki) for additional guidance
4. Map the data (e.g. [[Checklist Data|checklistData]] gets mapped to the [Taxon Core](http://rs.gbif.org/core/dwc_taxon_2015-04-24.xml), [[Occurrence Data|occurrenceData]] gets mapped to the [Occurrence Core](http://rs.gbif.org/core/dwc_occurrence_2015-07-02.xml), [[Sampling Event Data|samplingEventData]] gets mapped to the [Event Core](http://rs.gbif.org/core/dwc_event_2015_05_29.xml). 
5. Fill in resource metadata using the IPT's metadata editor
6. Publish the dataset (make it freely and openly available worldwide)
7. Register the dataset with GBIF. **Note**: your organisation must be registered with GBIF (see prerequisite #2 above) and added to your IPT by the IPT administrator. Otherwise, the organisation will not be available to choose from in the IPT. 