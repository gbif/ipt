## Quick Overview

Properties from the GMP (GBIF Metadata Profile) will be added to the metadata describing data resources beyond those currently supported by the IPT. IPT modifications will include an XML system (for viewing and downloading these metadata using different schemas) and UI modifications that support user viewing, editing, and searching these metadata.

_Note: This spec is simply a distilled version of the GBIF "Extended IPT Metadata Design Document"._

## Use Cases

#### Editing
Users need forms to edit new resource metadata that are grouped into categories:
  * Primary
  * Geographic
  * Taxonomic
  * Project and methods
  * Physical data
  * Keywords
  * Temporal

#### Viewing
Users need to view a one page summary of all metadata available for a given resource. This summary needs to be viewable and downloadable in all of the following formats:
  * Internal GBIF Metadata Schema Profile
  * EML
  * GBIF Global Strategy and Action Plan for mobilization of Natural History Collections data (GSAP NHC) NCD profile
  * Dublin Core
  * NAP/ISO 19115

#### Output
From Tim (3 Feb Subject: Metadata) "Note that the IPT 1.1 will only output EML format, so please ignore the section [the Extended IPT Metadata Design Document](in.md) contradicting this."

#### Transformations
Is there a use case for mapping one format to another? Unless we're missing something, the _viewing_ use case above accounts for viewing and downloading metadata in any of the supported formats. It's not entirely clear why a transformation from one format to another would be needed.

## Key Design Concerns

#### XML caching
Will metadata XML be dynamically generated or cached? If cached, what kind of replacement algorithm should be used? (John: My understanding that all management is done through the XML files.)

#### Uploading existing metadata
Although not defined as a use case, should uploading existing XML metadata be supported for a resource? (John: No suggestion of this in existing documentation.)

#### Controlled vocabularies
Which GMP properties have controlled vocabulary lists? Who will maintain these lists? To what extent should the system support updating these lists? Hard coded? API? Admin upload?

#### Form validation
Aside from required fields, what other validation issues come into play? Should validation occur on the client or server? How should validation errors be handled? Do they need to be logged?

#### Adding support for new schemas
To what extent does the IPT need to scale in support of new XML schemas?