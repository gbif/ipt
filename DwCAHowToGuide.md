# Darwin Core Archives – How-to Guide
<sup>Version 2.0</sup>

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/figures/cover_art_cicindelinae.png' align="right" width="300" height="250" />

## Table of Contents
* Introduction 
* Darwin Core Archive 
  * Darwin Core Archive Components 
* DWC-A Data Publishing Solutions 
  * Publishing DwC-A using the Integrated Publishing Toolkit (IPT) / Data HostingCenters 
    * Registering your Dataset using IPT 
  * Publishing DwC-A using GBIF Spreadsheet Templates 
  * Create your own Darwin Core Archive
* Validation of Darwin Core Archives
  * Registering data using Spreadsheet Processor, Make-Your-Own DwC-A, or other community tools
* Annex 1: Reference Guides to Terms and Vocabularies
  * Metadata
  * Data (Occurrence and Taxon)
  * Taxonomic Data/Annotated Species Checklists
  * Vocabularies
* Annex 2: Preparing Your Data
  * Character Encoding Conversion
  * Outputting Data From a MySQL Database Into a Textfile
Annex 3: Darwin Core Archive Examples

## Document Control

| Version | Description                  | Date of release | Author(s) |
|---------|------------------------------|-----------------|-----------|
| [1.0](http://links.gbif.org/gbif_dwc-a_how_to_guide_en_v1)    | Content review and additions | April 2011     | David Remsen, Markus Döring |
| 2.0     | Transferred to wiki, major edits | 9 May 2017      | Kyle Braak |

## Suggested citation                                                                                                                                                                                                                                                                        
                                                                                                                                                                                                                                                                                               
> GBIF (2017). Darwin Core Archives – How-to Guide, version 2.0, released on 9 May 2011, (contributed by Remsen D, Braak, K, Döring M, Robertson, T), Copenhagen: Global Biodiversity Information Facility, accessible online at: https://github.com/gbif/ipt/wiki/DwCAHowToGuide

_Cover art credit: Kim Wismann, Cicindelinae_

Introduction
============

Darwin Core Archive (DwC-A) is an internationally recognised biodiversity informatics data standard that simplifies the publication of biodiversity data. It is based on Darwin Core, a standard developed and maintained by the [Biodiversity Information Standards group](http://www.tdwg.org).

The [Darwin Core](http://rs.tdwg.org/dwc/) is body of standards. It includes a glossary of terms intended to facilitate the sharing of information about biological diversity by providing standard reference terms that include definitions, examples, and commentaries. The Darwin Core is primarily based on taxa and their occurrence in nature, as documented by observations, specimens, samples, and related information. The [Darwin Core terms](http://rs.tdwg.org/dwc/terms/) can be organised into schema or profiles and include guidelines on their use in XML or plain text documents.

The Darwin Core standard is used to mobilise the vast majority of specimen occurrence and observational records within the GBIF network. It was originally conceived to facilitate the discovery, retrieval, and integration of information about modern biological specimens, their spatio-temporal occurrence, and their supporting evidence housed in collections (physical or digital). The Darwin Core achieved this by defining a set of items in an ordered list, published in an XML document.

The Darwin Core today is broader in scope and application. It aims to provide a stable, standard reference for sharing information on biological diversity. As a glossary of terms, the Darwin Core provides stable semantic definitions with the goal of being maximally reusable in a variety of contexts. This means that Darwin Core may still be used in the same way it has historically been used, but may also serve as the basis for building enriched exchange formats, while still ensuring interoperability through a common set of terms. This guide defines one of these formats that may be used to publish specimen-occurrence and observational data as well as species-level information such as taxonomic checklists.

Darwin Core Archive
===================

Darwin Core Archive (DwC-A) is a biodiversity informatics data standard that makes use of the Darwin Core terms to produce a single, self contained dataset for sharing both species-level (taxonomic) and species-occurrence data. An archive is a set of text files, in standard comma- or tab-delimited format, with a simple descriptor file (called ***meta.xml***) to inform others how your files are organised. The format is defined in the [Darwin Core Text Guidelines](http://rs.tdwg.org/dwc/terms/guides/text/index.htm). ***It is the preferred format for publishing data in the GBIF network.***

The central idea of an archive is that its data files are logically arranged in a star-like manner, with one core data file surrounded by any number of ‘extension’ data files. Core and extension files contain data records, one per line. Each extension record (or ‘extension file row’) points to a record in the core file; in this way, many extension records can exist for each single core record. This is sometimes referred to as a “star schema”.

Sharing entire datasets as Darwin Core Archives instead of using page-able web services like [DiGIR](http://digir.sourceforge.net/) and [TAPIR](http://tdwg.github.io/tapir/docs/tdwg_tapir_specification_2010-05-05.html) allows much simpler and more efficient data transfer. For example, retrieving 260,000 records via TAPIR takes about nine hours, and involves issuing 1,300 http requests to transfer 500 MB of XML-formatted data. The exact same dataset, when encoded as DwC-A and zipped becomes a 3 MB file. Therefore, GBIF highly recommends compressing an archive using ZIP or GZIP when generating a DwC-A. In addition, producing Darwin Core Archives does not require any dedicated software to be installed by a data publisher, making it a much simpler option.

The production of a Darwin Core Archive requires the use of stable identifiers for core records, but not for extensions. For any kind of shared data it is therefore necessary to have some sort of local record identifiers. It is good practice to maintain – with the original data – identifiers that are stable over time and are not being reused after the record is deleted. If possible, please provide globally unique identifiers instead of local ones<sup>[1](DwCAHowToGuide#references)</sup>. This identifier is referred to as the “core ID” in Darwin Core Archives and the specific Darwin Core term that it corresponds to is dependent on the data type being published.

Darwin Core Archive Components
------------------------------

A Darwin Core Archive may consist of a single data file or multiple files, depending on the scope of the published data. The specific types of files that may be included in an archive are the following:

1.  ***A required core data file*** consisting of a standard set of [Darwin Core terms](http://rs.tdwg.org/dwc/terms/). The data file is formatted as ***fielded text***, where data records are expressed as rows of text, and data elements (columns) are separated with a standard delimiter such as a tab or comma (commonly referred to as CSV or ‘[comma-separated value’ files](http://en.wikipedia.org/wiki/Comma-separated_values)). The first row of the data file may optionally contain data or represent a ‘header row’. In general, if a header row is included, it contains the names of the Darwin Core terms represented in the succeeding rows of data.
GBIF currently supports the following three biodiversity data types as the basis for a core data file:
    1.  ***Occurrence or Primary Biodiversity Data*** - The category of information pertaining to evidence of an occurrence in nature, in a collection, or in a dataset (specimen, observation, etc.). Core files of this type are used to share information about a specific instance of a taxon such as a specimen or observation. The required core ID is represented by ***dwc:occurrenceID***. The definitive list of Occurrence terms can be found in the [Occurrence (Core) Extension](http://rs.gbif.org/core/dwc_occurrence_2015-07-02.xml).
    2.  ***Taxon*** - The category of information pertaining to taxa or taxon concepts, such as species. Core files of this type are used to share annotated species checklists, taxonomic catalogues, and other information about taxa. The required core ID is represented by ***dwc:taxonID***. The definitive list of core Taxon terms can be found in the [Taxon (Core) Extension](http://rs.gbif.org/core/dwc_taxon_2015-04-24.xml).
    3.  ***Event*** - The category of information pertaining to a sampling event. Core files of this type are used to share information about ecological investigations that can be one off studies or monitoring programmes that are usually quantitative, calibrated and follow certain protocols so that changes and trends of populations can be detected. The required core ID is represented by ***dwc:eventID***. The definitive list of core Event terms can be found in the [Event (Core) Extension](http://rs.gbif.org/core/dwc_event_2016_06_21.xml).

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/figures/core_data_file.png' width="601" height="124" />

Figure 3. A core data file is a simple, tabular, text file

2.  ***Optional “extension” files*** support the exchange of additional, described classes of data that relate to the core data type (Occurrence or Taxon). An extension record points to a record in the core data file. Extensions may only apply to Taxa or Occurrences or may apply to both. For example, the Vernacular Names extension (illustrated below) is an extension to the Taxon class, whereas an Images extension may be used in both. Extensions can be created and added to the GBIF Extension Repository following a consultation and development process with GBIF. The definitive list of supported Extensions can be found on the [GBIF Extension Repository](http://rs.gbif.org/extension/) 

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/figures/extension_data_file.png' width="601" height="124" />

Figure 4. An extension is linked to the core file via the common taxon ID

3.  A descriptor ***metafile*** describes how the files in your archive are organised. It describes the files in the archive and maps each data column to a corresponding standard Darwin Core or Extension term. The metafile is a relatively simple XML file format. GBIF provides an online tool for making this file but the format is simple enough that many data administrators will be able to generate it manually. These options are described in the Publishing Options section of this document.

> A metafile is ***required*** when an archive includes any extension files or if a single core data file uses non-standard column names in the first (header) row of data. A complete reference guide to this metafile is  [available](http://links.gbif.org/gbif_dwc-a_metafile_en_v1). **(TODO - merge content here)**

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/figures/meta_file.png' width="557" height="166" />

Figure 3. The metafile describes the file names and fields in the core and extension files

4.  Datasets require documentation. This is achieved in a Darwin Core Archive by including a ***resource metadata document*** that provides information about the dataset itself such as a description (abstract) of the dataset, the agents responsible for authorship, publication and documentation, bibliographic and citation information, collection methods and much more. GBIF currently supports a metadata profile based on the [Ecological Metadata Language](https://knb.ecoinformatics.org/#external//emlparser/docs/eml-2.1.1/index.html) but other metadata standards exist and may be supported. The GBIF Metadata Profile's XML Schema description can be found on the [GBIF Schema Repository](http://rs.gbif.org/schema/eml-gbif-profile/)

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/figures/metadata_file.png' width="605" height="182" />

Figure 4. A metadata document describes the complete dataset

The entire collection of files (core data, extensions, metafile, and resource metadata) can be compressed into a single archive file. Compression formats include [ZIP](http://en.wikipedia.org/wiki/ZIP_(file_format)) and TAR.GZ /[TGZ](http://en.wikipedia.org/wiki/Tar_(file_format)).

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/figures/zipped_archive.png' width="484" height="130" />

Figure 5. Text files are zipped into a single archive

This single, compressed file is the Darwin Core Archive file! This file is easily transported via email, or FTP. It can be served to GBIF simply by putting the file on a web server and registering the URL with GBIF. Details on registering are provided in the Validation and Registration section of this document. See: DwC-A Data Publishing Solutions.

DwC-A Data Publishing Solutions
===============================

There are a number of different options for generating a Darwin Core Archive.

To help select the most appropriate solution for creating your own archive, answering the following questions can help your decision:

1.  Have your data been digitised? (If yes, it is assumed that you can easily convert the data into CSV or Tab format).
2.  Are your data stored in a relational database?
3.  How many separate datasets (DwC-Archives) do you plan to publish?

The ***Integrated Publishing Toolkit*** is most suitable when:

-   Your data have been digitised already.
-   Your data either are or are not already in a relational database
-   You need to create/manage multiple archives.
-   You would like to document datasets using the GBIF Metadata Profile.

The ***GBIF Darwin Core Spreadsheet Templates*** are most suitable when:

-   Your data have not been digitised already.
-   You already maintain basic species lists in a spreadsheet file.
-   You need a simple solution for authoring and managing a limited number metadata documents to describe datasets you manage in another system and would like to publish through GBIF.

The ***Make Your Own*** option is most suitable when:

-   Your data have been digitised already.
-   Your data may be in a relational database.
-   You only need to create/manage a small number of archives, or have the option to automate / script the archive generation process.

A more detailed discussion of these three options follows.

Publishing DwC-A using the Integrated Publishing Toolkit (IPT) / Data HostingCenters
------------------------------------------------------------------------------------

***Assumption: Your data are already stored as a CSV/Tab text file, or in one of the supported relational database management systems (MySQL, PostgreSQL, Microsoft SQL Server, Oracle, Sybase). Preferably, you are already using Darwin Core terms as column names, although this is not compulsory.***

The Integrated Publishing Toolkit (IPT) is GBIF’s flagship tool for publishing Darwin Core Archives. There are two configuration options available.

1.  You can install and host a local version of the IPT at your home institution.
2.  You can access a hosted instance of an IPT at a GBIF-endorsed Data Hosting Centre and publish your data there:
    1.  DanBIF Data Hosting Center
    2.  Endangered Wildlife Trust Data Hosting Center

Please contact <helpdesk@gbif.org> for more information on using a Data Hosting Centre.

The IPT can be used to publish Occurrence Data, Taxon Data, and/or Metadata-only.

Below is a set of instructions on how to create a DwC-Archive using the IPT. For more detailed information on installing and operating the IPT, please refer to the [IPT User Manual.](IPT2ManualNotes.wiki) A separate How-To guide for producing metadata is also available (GBIF Extended Metadata Profile: [How-To Guide](http://links.gbif.org/gbif_metadata_profile_how-to_en_v1)). Additional information on the IPT is available from the [project’s website](http://www.gbif.org/ipt).

To generate a DwC-Archive using the IPT:

1.  Follow the instructions in section “Preparing Your Data” (See: Annex to this document, below).
2.  Create a new resource in the IPT editor.
3.  [Upload the source data](IPT2ManualNotes.wiki#source-data) and configure it:
    1.  For CSV/Tab files: use the "upload file" option.
    2.  For a database: create a new SQL source.
4.  Create a mapping between the source data and the Darwin Core terms, using the IPT interface to match your own column headers against the terms.

Depending on the type of data you are publishing, you will need to ensure that the appropriate core types and extensions are loaded. This is based on initial configurations when the IPT instance was installed. For example:

-   To publish Occurrence data (specimen or observation) data, the core type ***Darwin Core Occurrence*** must be loaded.
-   To publish common names with a species checklist the core type ***Darwin Core Taxon*** and the ***Vernacular Names Extension*** must be loaded.

The IPT automatically maps all columns that use Darwin Core terms in the first (header) row in the source data file. Using Darwin Core terms in your source data helps to save time when generating the mapping. Otherwise, the IPT assists the mapping process through a help dialog. For each term, a definition, an example, and link to the Darwin Core documentation on that term is available. In addition, fields that expect values from controlled vocabularies will present those values in a drop-down list. Whenever a problem exists with a mapping, it is highlighted and brought to the user’s attention to try to ensure that all columns get successfully mapped.

1.  Publish the new DwC-Archive, using the IPT dialogue. This will create the DwC-Archive, bundling the data sources together with the metadata in one zipped archive. On successful processing of the archive, both the archive and the metadata file (EML) will be assigned their own URLs.

### Registering your Dataset using IPT

The IPT supports automatic registration in the GBIF network. In the “Managing Resources” page of your resource, there is a “Visibility” section. If the status is set to “public”, then there will be a “Register” button and a drop-down list for institutions. Choose the institution with which the resource or dataset is associated, and click the “Register” button. Now your dataset and metadata are registered with the GBIF Registry. See the [IPT User Manual](IPT2ManualNotes.wiki#visibility) for further details.

Publishing DwC-A using GBIF Spreadsheet Templates
-------------------------------------------------

***Assumption: The occurrence or simple taxonomic data to be published are not yet captured in digital format OR a simple solution for creating a metadata document to describe a dataset is desired.***

GBIF provides a set of pre-configured Microsoft Excel spreadsheet files that serve as templates for capturing metadata, occurrence data, simple species checklists, and sampling-event datasets. The spreadsheets are linked to an online processing system that validates the uploaded (or emailed) spreadsheet file and then transforms the data to a Darwin Core Archive and returns this to the user.

Below is a set of instructions on how to create a DwC-Archive using one of the GBIF Darwin Core Spreadsheet Templates. Each template provides inline help and instructions in the worksheets. Filling in the metadata is outside the scope of these instructions; check the separate [GBIF Extended Metadata Profile: How-To Guide](http://links.gbif.org/%20gbif_metadata_profile_guide_en_v1)[25]. 

Generate a DwC-Archive using the Spreadsheet Templates:

1.  Choose the appropriate template:
    1.  ***Metadata Template***: suitable for composing a metadata document.
    2.  ***Occurrence Template***: suitable for occurrence data (specimen, observation).
    3.  ***Checklist Template***: suitable for basic species checklists. Several options are provided that cater to different styles for representing classifications.
    4.  ***Sampling-event Template***: suitable for sampling-event data.

2.  Fill in the template, using the inline help and reference guides included on the project site. To access the inline information, hover the cursor over cells with red upper-right corners.
3.  Upload the completed template to the IPT and publish it

To use the validator:

1.  Combine the text file(s), metafile (meta.xml), and metadata (eml.xml) together in one zipped folder.
2.  Upload the zipped folder using the form provided in the Validator web page.
3.  Validate the DwC-Archive
4.  Review and address any response that refers to a validation error.
5.  Repeat the process until the file is successfully validated.
6.  Contact the GBIF Helpdesk if you get stuck (helpdesk@gbif.org).
7.  Registering data using Spreadsheet Processor, Make-Your-Own DwC-A, or other community tools” below.

Create your own Darwin Core Archive
-----------------------------------

***Assumption: Data is already in, or can easily generate, a CSV/Tab text file, or in one of the supported relational database management systems (MySQL, PostgreSQL, Microsoft SQL Server, Oracle, Sybase). The publisher does not wish to host an IPT instance but does have access to a web server.***

Darwin Core Archives can be created without installing any dedicated software. These instructions target data managers who are familiar with the dataset to be published and are comfortable working with their data management system. 

Below is a set of instructions on how to manually create and validate a DwC-Archive:

1.  Unless the data are already stored in a CSV/Tab text file, the publisher needs to prepare a text file(s) from the source. If the data are stored in a database, generate an output of delimited text from the source database into an outfile. Most database management systems support this process; an example is given in the Annex to this guide, below, in the section “Outputting Data From a MySQL Database Into a Textfile”. As the metafile maps the columns of the text file to Darwin Core terms, it is not necessary to use Darwin Core terms as column header in the resultant text file, though it may help to reduce errors. A general recommendation is to produce a single core data file and a single file for each extension if the intention is to output data tied to an extension.
2.  Create a Metafile: There are three different ways to generate the file:
    1. Create it manually by using an XML editor and using a sample metafile as a guiding example. A complete description of the metafile format can be found in the [Darwin Core Text Guide](http://rs.tdwg.org/dwc/terms/guides/text/index.htm).
    2. Create it using the GBIF IPT, automatically generating a metafile as part of the DwC-A publication process.    
    3. Create it using the online application [Darwin Core Archive Assistant](http://tools.gbif.org/dwca-assistant/) <img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/figures/dwc-a_assistant.png' align="right" width="300" height="250" /> Simply select the fields of data to be published, provide some details about the files and save the resultant XML. This only needs to be done once unless the set of published fields changes at some later time. **Warning: this tool is no longer supported by GBIF. Support for the Event core is missing. Publishers also need to manually add term dwc:taxonID to Taxon core and dwc:occurrenceID to Occurrence core, to ensure they are explicitly included.** 
3. Create a metadata file (eml.xml) that describes the data resource. Complete instructions on doing this are available in the [GBIF Extended Metadata Profile: How-To Guide](http://links.gbif.org/%20gbif_metadata_profile_guide_en_v1). It is best practice to include a metadata file and the simplest way to produce one is using the IPT's built-in metadata editor.
4. Ensure the data files, the metafile (meta.xml) and metadata file (eml.xml) are in the same directory or folder. Compress the folder using one of the support compression formats. The result is a Darwin Core Archive.

***Note***: ***Metadata authored using IPT can be output as an RTF document, which can then be submitted as ‘Data Paper’ manuscript to Zookeys, PhytoKeys and BioRisks. See instructions to authors for ‘Data Paper’ submission to these journals.***

Validation of Darwin Core Archives
==================================

GBIF provides an online [DwC-Archive Validator](http://tools.gbif.org/dwca-validator/) to validate the completed archive. Archives should be validated to ensure they are properly composed before the final publishing/registration step.

To use the validator:

1.  Combine the text file(s), metafile (meta.xml), and metadata (eml.xml) together in one zipped folder.
2.  Upload the zipped folder using the form provided in the Validator web page.
3.  <span id="_Ref163105804" class="anchor"></span>Validate the DwC-Archive
4.  Review and address any response that refers to a validation error.
5.  Repeat the process until the file is successfully validated.
6.  Contact the GBIF Helpdesk if you get stuck (helpdesk@gbif.org).

### Registering data using Spreadsheet Processor, Make-Your-Own DwC-A, or other community tools

Registration is the final step of data publication using Darwin Core Archive. An entry for the resource is made in the GBIF Registry that enables the resource to be discoverable and accessible. There is no automatic registration for these options. An email should be sent to [*helpdesk@gbif.org*](mailto:helpdesk@gbif.org) with the following information:

1.  Dataset title
2.  Dataset description
3.  Technical contact (the person to be contacted in matters regarding technical availability or resource configuration issues on the side of the dataset or data publisher)
4.  Administrative contact (the person to be contacted in all matters regarding scientific data content and usage of a specific dataset or data publisher)
5.  Institution name
6.  Your relation to this Institution
7.  The name of the GBIF Participant Node that can endorse the publishing institution
8.  The dataset URL: either the wrapper URL (if you are publishing using one of the wrappers), or the DwC-Archive URL (if you are publishing via a zipped DwC-Archive)
9.  The metadata document URL

Please ensure you have all of the information before you send the email. You will receive a confirmation email, and a URL representing the resource entry in the Registry.

Annex 1: Reference Guides to Terms and Vocabularies
===================================================

This section provides links to both online and printable reference guides to terms and vocabularies that support the Darwin Core Archive format. The definitive source for these terms is the GBIF Resources Repository at <http://rs.gbif.org>. It provides a simple menu of options and clear lists and definitions of terms and supporting vocabularies.

Metadata
--------

***GBIF Extended Metadata Profile Reference Guide*** – This document introduces and defines all the terms and their use in the GBIF Metadata Profile built around the Ecological Metadata Language (EML).

A printable guide can be found at: <http://links.gbif.org/gbif_metadata_profile_guide_en_v1>

In addition, a How-To Guide for composing metadata can be found at: <http://links.gbif.org/gbif_metadata_profile_how-to_en_v1>

Data (Occurrence, Taxon and Event)
---------------------------

The definitive online list of core Occurrence, Taxon and Event terms are available via the GBIF Resource Repository at <http://rs.gbif.org>

Taxonomic Data/Annotated Species Checklists
-------------------------------------------

Refer to the [Best Practices Guide for publishing species checklists](https://github.com/gbif/ipt/wiki/BestPracticesChecklists). This lists the GBIF Checklist Extensions that can be used in publishing annotated species checklists and taxonomic catalogue data.

Vocabularies 
-------------

Many terms in the core and extension profiles recommend (but do not require) the use of controlled vocabularies to enhance consistency and validation. Some of these vocabularies are listed in the [GBIF Resource Repository](http://rs.gbif.org/vocabulary/).

Annex 2: Preparing Your Data
============================

For All Sources

Mandatory Terms (must be included)

For Text File Source Only:
-   Files must be encoded using UTF-8. For converting character encodings of files, see section “Character Encoding Conversion”.

For Database Source Only:
-   Setup a SQL view to use functions (this can also be done in the IPT SQL source definition)
    -   Concatenate or split strings as required, e.g. to construct the full scientific name string (watch out for autonyms)
    -   Format dates to conform to ISO datetime format
    -   Create year/month/day by parsing native SQL date types
-   Use a UNION to merge 2 or more tables, e.g. accepted taxa and synonyms, or specimen and observations
-   Select static values

Character Encoding Conversion
-----------------------------

Simple resources for Unix and Windows to convert character encodings of files:

-   <http://en.wikipedia.org/wiki/Iconv>
-   <http://www.gnu.org/software/libiconv/>
-   <http://gnuwin32.sourceforge.net/packages/libiconv.htm>

Ex.: Convert character encodings from Windows-1252 to UTF-8 using [*iconv*](http://unixhelp.ed.ac.uk/CGI/man-cgi?iconv):

\#iconv -f CP1252 -t utf-8 example.txt &gt; exampleUTF8.txt

Outputting Data From a MySQL Database Into a Textfile
-----------------------------------------------------

It is very easy to produce ***delimited text*** using the SELECT INTO outfile command from MySQL. The encoding of the resulting file will depend on the server variables and collations used, and might need to be modified before the operation is done. Note that MySQL will export NULL values as \\N by default. Use the IFNULL() function as shown in the following example to avoid this:

> SELECT
IFNULL(id, ''), IFNULL(scientific\_name, ''), IFNULL(count,'')
INTO outfile '/tmp/dwc.txt'
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\\n'
FROM`
dwc;

Annex 3: Darwin Core Archive Examples
=====================================

The following URLS refer to example Darwin Core Archive files that can be accessed as reference files.

Checklist: <http://gbif-ecat.googlecode.com/files/Whales-DWC-A.zip>

Occurrence: <http://www.siba.ad/andorra/dwcaMolluscsAndorra.zip>

## References
[1] A Beginner’s Guide to Persistent Identifiers, http://links.gbif.org/persistent_identifiers_guide_en_v1.pdf