<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/arrow-back-24.png' />[[Back to instructions|howToPublish#instructions]]

**Resource Metadata** &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Checklist Data|checklistData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Occurrence Data|occurrenceData]] &nbsp;&nbsp;``—>``&nbsp;&nbsp; [[Sample Event Data|sampleEventData]]

---

#Resource Metadata

## Table of contents
+ [Introduction](resourceMetadata#introduction)
+ [How to transform your data into checklist data](resourceMetadata#how-to-transform-your-data-into-checklist-data)
+ [Template](resourceMetadata#template)
+ [Required DwC fields](resourceMetadata#required-dwc-fields)
+ [Recommended DwC fields](resourceMetadata#recommended-dwc-fields)
+ [Exemplar datasets](resourceMetadata#exemplar-datasets)
+ [FAQ](resourceMetadata#faq)

### Introduction
Description and contact details for a biodiversity information resource where no digital data can currently be shared.  All other classes of GBIF data also include this basic information.  Such metadata may be a valuable tool for researchers to discover resources which are not yet available online.  This is also a useful way to assess the importance and value of non-digital resources for future digitization. GBIF ensures that every dataset is associated with a Digital Object Identifier (DOI) to facilitate citation.

### How to write resource metadata
Ultimately your metadata needs to be transformed into an XML metadata document. The XML must conform to the GBIF Metadata Profile, which is based on the Ecological Metadata Language (EML). 

No Excel template exists for resource metadata. Simply use the IPT's built-in metadata editor to populate the metadata. The IPT makes sure it's in the proper valid XML format. 

Alternatively if your metadata is already in EML or Dublin Core, you can upload it in these formats to the IPT. Guidance on how to do that can be found [here](IPT2ManualNotes.wiki#upload-a-metadata-file).

For extra guidance, you can look at the [exemplar datasets](resourceMetadata#exemplar-datasets). 

#### Template: 
No Excel template exists for resource metadata. Simply use the IPT's built-in metadata editor to populate the metadata.

#### Required EML fields: 
* title
* description
* publishing organisation
* type
* license
* contact(s)
* creator(s)
* metadata provider(s)

#### Recommended EML fields: 
* sampling methodology - in case describing sample event data
* citation - to ensure your dataset gets cited the way you want

#### Exemplar datasets: 
* Inter-Valley Soil Comparative Survey of the McMurdo Dry Valleys: [EML](http://ipt.biodiversity.aq/eml.do?r=ictar_ivscs&v=1) / [IPT homepage](http://ipt.biodiversity.aq/resource.do?r=ictar_ivscs)

#### FAQ: 

**Q.** What should I do if my data cannot be made freely available?

**A.** You should publicise its existence by publishing metadata about it. You can indicate the data can be made available by request, to encourage future collaboration and meta-analysis. 