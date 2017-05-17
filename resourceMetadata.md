<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/arrow-back-24.png' />[[Back to instructions|howToPublish#instructions]]

**Resource Metadata** &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Checklist Data|checklistData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Occurrence Data|occurrenceData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Sampling Event Data|samplingEventData]]

---

# Resource Metadata

## Table of contents
+ [Introduction](resourceMetadata#introduction)
+ [How to write resource metadata](resourceMetadata#how-to-write-resource-metadata)
+ [Template](resourceMetadata#template)
+ [Required metadata fields](resourceMetadata#required-metadata-fields)
+ [Recommended metadata fields](resourceMetadata#recommended-metadata-fields)
+ [Exemplar datasets](resourceMetadata#exemplar-datasets)
+ [FAQ](resourceMetadata#faq)

### Introduction
Description and contact details for a biodiversity information resource where no digital data can currently be shared.  All other classes of GBIF data also include this basic information.  Such metadata may be a valuable tool for researchers to discover resources which are not yet available online.  This is also a useful way to assess the importance and value of non-digital resources for future digitization. GBIF ensures that every dataset is associated with a Digital Object Identifier (DOI) to facilitate citation.

### How to write resource metadata

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/flow-rm.png' />

Ultimately your metadata needs to be transformed into an XML metadata document. The XML must conform to the GBIF Metadata Profile, which is based on the Ecological Metadata Language (EML). 

No Excel template exists for resource metadata. Simply use the IPT's built-in metadata editor to populate the metadata. The IPT makes sure it's in the proper valid XML format. 

Alternatively if your metadata is already in EML or Dublin Core, you can upload it in these formats to the IPT. Guidance on how to do that can be found [here](IPT2ManualNotes.wiki#upload-a-metadata-file).

For extra guidance, you can look at the [exemplar datasets](resourceMetadata#exemplar-datasets). 

#### Template: 
No Excel template exists for resource metadata. Simply use the IPT's built-in metadata editor to populate the metadata.

#### Required metadata fields: 
* title
* description
* publishing organisation
* type
* license
* contact(s)
* creator(s)
* metadata provider(s)

#### Recommended metadata fields: 
* sampling methodology - in situations where data comes from a sampling event
* citation - to ensure your dataset gets cited the way you want

#### Exemplar datasets: 
* Inter-Valley Soil Comparative Survey of the McMurdo Dry Valleys: [EML](http://ipt.biodiversity.aq/eml.do?r=ictar_ivscs&v=1.0) / [IPT homepage](http://ipt.biodiversity.aq/resource.do?r=ictar_ivscs)

#### FAQ: 

##### Q. What should I do if my data cannot be made freely available?

**A.** You should publicise its existence by publishing metadata about it. You can indicate the data can be made available by request, to encourage future collaboration and meta-analysis.

##### Q. How can I apply a license to my dataset?

**A.** Simply use the IPT's built-in metadata editor following [these instructions](https://github.com/gbif/ipt/wiki/IPT2ApplyingLicense.wiki#dataset-level).  

Alternatively if your metadata is already in EML you should assign the dataset a machine readable license before uploading it to the IPT following [these instructions](https://github.com/gbif/ipt/wiki/IPT2ApplyingLicense.wiki#supplementary-information) 

##### Q. How can I add a link to the dataset's related data paper?

**A.** Reference the related data paper in the IPT's Bibliographic Citations metadata section. Ensure the citation is properly formatted, including its DOI as a linkable URL (e.g. https://doi.org/10.1038/sdata.2017.16). The makes it possible to discover the data paper while reading the dataset metadata. 

##### Q. GBIF assigned my dataset a DOI - can I add this to my metadata?

**A.** Yes, the GBIF DOI, written as a linkable URL (e.g. https://doi.org/10.15468/nc6rxy) can be added to the IPT dataset metadata in the following places:
* Citation DOI (remember to turn on citation [auto-generation](https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki#citations), to ensure your dataset's citation complies with the [best-practice format](https://github.com/gbif/ipt/wiki/IPT2Citation.wiki)).  
* Alternate identifiers list 

Note GBIF only assigns a DOI to a dataset during registration, if no DOI was previously assigned to it.

Also note that using the GBIF DOI in the citation may mislead users, as it resolves to the GBIF dataset page - not the online dataset. A DOI is preferred to using a URL to the online dataset, however, because it guarantees persistent access. 