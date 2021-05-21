# Tutorial - Create a Darwin Core Occurrence Resource
# Table of Contents


This tutorial explains how to create an example resource, install extensions, import source data, map source data fields to the extension, create data value translations, and publish the resource.

## Prerequisites
Following are the conditions assumed to be true in order to follow this tutorial in a working IPT environment.

1) A fresh, functional, non-production version of the IPT is installed, running, and connected to the Internet. Refer to the [Getting Started Guide](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Getting_Started_Guide) section of the IPT User Manual to see how to prepare the IPT for its first use.

2) The reader can log in to the IPT as a user having the Admin role.
## Steps
### 1) Login
Quick Reference Guide: [Common Features/Controls that appear on all pages/Header](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Header)

Use the default Admin user email address and password to log in to the IPT. After logging in successfully, the IPT menu bar will appear.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceLoggedIn.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceLoggedIn.png)

### 2) Navigate to Extensions
Quick Reference Guide: [Administration Menu/Configure Core Types and Extensions](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Configure_Extensions)

Click on the Administration Menu to open the options available to users having the Admin role.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/IPTAdminBeforeRegistration.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/IPTAdminBeforeRegistration.png)

Click on the Core Types and Extensions button to enter into the Extensions management page. If no extensions have been installed yet, the Core Type, Extensions, and Vocabularies sections will appear at the top of the page as in the following screen image. If core types or extensions have already been installed, they will appear in the appropriate section on the page above the Vocabularies section.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceExtensionsFirstTime.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceExtensionsFirstTime.png)

### 3) Update vocabularies
Quick Reference Guide: [Administration Menu/Configure Core Types and Extensions/Update vocabularies](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Update_vocabularies)

Click on the button labeled "Update" in the Vocabularies section to get the latest information from the GBIF Registry on controlled vocabularies used with the core types and extensions. After updating, an information message will appear at the top of the page indicating the results of the update attempt.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceVocabularyUpdated.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceVocabularyUpdated.png)

### 4) Install Darwin Core Occurrence extension
Quick Reference Guide: [Administration Menu/Configure Extensions/Install extension](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Install_extension)

Locate the row for the Darwin Core Occurrence Core Type. If it has not yet been installed, it will appear below the Vocabularies section and will have a button labeled "Install" in the right-hand column under the extension name (Darwin Core Occurrence). If the extension has already been installed, it will appear above the Vocabularies section (see screen image above, under step 2). If this is the case, skip this step, otherwise, click on the "Install" button. A message will appear indicating if the installation was successful. If it was, Darwin Core Occurrence will appear in the list of installed Core Types.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceExtensionsOccurrenceAdded.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceExtensionsOccurrenceAdded.png)

### 5) Navigate to Manage Resources menu
Click on Manage Resources in the menu bar. If this IPT installation does not yet have any resources configured, the page will appear as in the screen image below. Otherwise it will show table of existing resources above the section labeled "Create New Resource".

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceCreateNewResource.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceCreateNewResource.png)

### 6) Create the resource
Quick Reference Guide: [Manage Resources Menu/Create New Resource/Create an entirely new resource](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Create_an_entirely_new_resource)

Enter the value "My Occurrence Resource" in the text box labeled "Shortname", then click on the button labeled "Create". In this tutorial we will not create a resource from a pre-existing Darwin Core archive. If we wanted to do so, we would choose the archive file before clicking on the "Create" button.

Notice that the page shows that an invalid entry was made for the resource's Shortname.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceCreateNewResourceBadName.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceCreateNewResourceBadName.png)

Enter the value "MyOccurrenceResource" as a valid value in the Shortname text box and click on the Create button. If there are no further errors the Resource Overview page will appear. Note that the resource is currently known by its Shortname in all lower case, lacks required metadata, has no source data set, and is private.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceOverviewAfterCreate.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceOverviewAfterCreate.png)

### 7) Navigate to the Basic Metadata page
Click on the button labeled "Edit" to enter the Basic Metadata page, the first of a series of metadata pages and the only one having fields for metadata that are required for the resource to be published. The Basic Metadata page opens with minimal default values already selected. The current title is the same as the Shortname given in step 6, the metadata and resource languages are selected, the resource subtype is selected, and the Metadata Provider's First Name, Last Name, and Email address are filled in from the information about the currently logged in user.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceBasicMetadata.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceBasicMetadata.png)

### 8) Enter required metadata
Quick Reference Guide: [Manage Resources Menu/Resource Overview/Metadata](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Metadata)

The required metadata fields include the Title, the Description, and one of the following for the Resource Contact, the Resource Creator, and the Metadata Provider: Last Name, Position, Organisation. Change the Title from the current shortname to "My Occurrence Resource". Enter a Description. Select "occurrence resource" for the Subtype. Enter at least one of the required fields and any other fields you would like for the Resource Contact. Click on the "Copy details from contact" link in the Resource Creator section to see how the fields are filled from the Resource Contact section above. Enter or change any other fields for the Resource Contact, Creator, or Metadata Provider, without violating the metadata requirements. Click on the button labeled "Save". The Geographic Coverage page, the next in the series of metadata pages, will appear. In this tutorial it is not necessary to enter any metadata on any of the remaining metadata pages - there are no further metadata requirements to publish a resource.

### 9) Navigate to the Resource Overview page
Click on the Manage Resources menu. Now you will see the table of existing resources with a row for the newly edited resource named "My Occurrence Resource" having the Type "occurrence", 0 for the number of Records, a Last Modified corresponding to the date the metadata were last saved, and Visible to "You and 0 others".

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceListAfterMetadata.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceListAfterMetadata.png)

Click on the "My Occurrence Resource" link in the Name column to open the Resource Overview page again. Note that there is no longer a message in the Metadata area of the page saying that the metadata are incomplete. Also, the page title is now the Title of the resource instead of the resource shortname, and the resource description shows in right-hand, summary column of the metadata area.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceOverviewAfterMetadata.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceOverviewAfterMetadata.png)

### 10) Add a source data file
Quick Reference Guide: [Manage Resources Menu/Resource Overview/Source Data](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Source_Data)

Download the Occurrence example data file from the IPT project web site (http://gbif-providertoolkit.googlecode.com/files/OccurrenceData.txt). Click on the button labeled "Choose File" to navigate to and select the downloaded file. After the file is selected its name will appear in the Source Data area next to the "Choose File" button. Click on the button labeled "Add" to navigate to the Source Data File detail page. Note that the file summary in upper right shows that the file was readable given the parameters that were detected in the file and entered in the fields on this page automatically. The file contains 27 columns and 12 Rows.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceSourceDataAdd.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceSourceDataAdd.png)

To see the contents of the file as interpreted, click on the button labeled "Preview". Click anywhere on the preview page to return to the "Source Data" page

Click on the button labeled "Save" to preserve the source data file information and return to the Resource Overview page.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceOverviewAfterSourceData.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceOverviewAfterSourceData.png)

Note that the right-hand column of the Source Data area shows summary information for the source data file and a button labeled "Edit", which will reopen the Source Data File detail page if clicked. The Resource Overview page now also shows a select box in the left-hand area of the Darwin Core Mappings area. This select box contains a list of Core Types and Extensions that have been installed in this instance of the IPT. If this tutorial has been followed exactly, there will be only one item in the select box, "Darwin Core Occurrence", which represents the extension installed in step 4.

### 11) Select the extension and source data to map
Quick Reference Guide: [Manage Resources Menu/Resource Overview/Darwin Core Mappings](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Darwin_Core_Mappings)

Select the Darwin Core Occurrence extension and click on the button labeled "Add" to open the Data Source selection page.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceSourceDataSelect.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceSourceDataSelect.png)

The top of the page shows that the mapping will be done against the Darwin Core Occurrence extension. Below is a select box showing a list of possible data sources to map. If this tutorial has been followed exactly, there will be only one item in the select box, "occurrencedata", which represents the file added in step 10. Select the occurrencedata Data Source and click on the button labeled Save" to go to the Mappings Page.

### 12) Map source data fields to extension properties
Quick Reference Guide: [Manage Resources Menu/Resource Overview/Darwin Core Mappings](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Darwin_Core_Mappings)

Following is a partial screen image of the Mappings page as it appears when it is first opened.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceMappings.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceMappings.png)

On the top of the mappings page is a message indicating that 22 columns from the source data file have been automatically mapped to properties in the extension based on the equivalence of column headers names and extension property names. The 22 properties that have been mapped each have the column name selected in the first select box in the right-hand column of the Mappings are of the page. Remember that there are 27 columns in the occurrencedata.txt file, meaning that five columns could not be mapped automatically. These five fields include GUID, LINK, SOURCE\_ID, COLLECTOR, and EARLIEST\_DATE\_COLLECTED, which do not appear in the Darwin Core Occurrence extension properties. Nevertheless, most of these columns can be mapped to Darwin Core Occurrence extension properties.

The GUID in the source data file is a global unique identifier for the occurrence record, which means, theoretically, that no other occurrence record anywhere can be confused with this one. This meaning for GUID is in accord with the definition of the Darwin Core occurrenceID (http://rs.tdwg.org/dwc/terms/index.htm#occurrenceID), so the occurrenceID can be mapped to the GUID column in the source data. To do so, click on the left-most select box next to occurrenceID in the Mapping table and select GUID from the list of column names given.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceGUID.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceGUID.png)

Note that the occurrenceID select box contains three extra choices at the top of the list (No ID, UUID Generator, and Line Number) that are not column names in the source data.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceMappingOccurrenceID.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceMappingOccurrenceID.png)

These three extra options are provided in any extension property that is meant to be a record identifier, for example, occurrenceID for an an Occurrence extension, and taxonID for a Taxon extension. The IPT does not require a record identifier (occurrenceID in this case) if there will be no other mappings to other extensions for the same resource. The "No ID" option allows the user to state explicitly that there is no equivalent to a record identifier in the source data. The other two options generate occurrenceIDs automatically. The UUID Generator option creates global unique identifiers, while the Line Number creates IDs that are unique only with the resource and that are numbered sequentially based on the rows in the source data file.

The LINK column in the source data contains URLs to detailed information about the record. This is the equivalent of the Darwin Core occurrenceDetails property (http://rs.tdwg.org/dwc/terms/index.htm#occurrenceDetails), so the occurrenceDetails property can be mapped to the LINK column in the source data. Select LINK from the list of columns in the left-most select box next to occurrenceDetails in the Mapping table.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceLINK.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceLINK.png)

The SOURCE\_ID column in the source data contains record identifiers that are unique within the data set and have a use in the original data store. Whereas these could be used as an occurrenceID, the global unique identifier given in the GUID column in the source data is a better match to the purpose of occurrenceID. There is no other extension property that matches the meaning of the SOURCE\_ID, so we will leave it unmapped.

The COLLECTOR column in the source data corresponds exactly with the Darwin Core recordedBy property (http://rs.tdwg.org/dwc/terms/index.htm#recordedBy). Map the recordedBy property to the COLLECTOR column by selecting COLLECTOR from the list of column names in the left-most select box next to recordedBy in the Mapping Table.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceCOLLECTOR.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceCOLLECTOR.png)

The EARLIEST\_DATE\_COLLECTED column in the source data is the only date information shared in this example data set, and presumably signifies the beginning date in a date range. Though there may have been another column corresponding to a latest data collected, but that is not available in the source data file. With only this much information, there is no exact match to a Darwin Core Occurrence extension property. However, if we also know that the source data contains only collecting dates that occurred on a single day, then the earliest and lasted dates collected would be the same, and the meaning of EARLIEST\_DATE\_COLLECTED would correspond with the meaning of Darwin Core eventDate property (http://rs.tdwg.org/dwc/terms/index.htm#eventDate). Assume this is true and map the eventDate property to the EARLIEST\_DATE\_COLLECTED column by selecting EARLIEST\_DATE\_COLLECTED from the list of column names in the left-most select box next to eventDate in the Mapping Table.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceEARLIEST_DATE_COLLECTED.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceEARLIEST_DATE_COLLECTED.png)

At this point all of the columns in the source data file that can be mapped have been mapped. However, many extension properties remain without mappings to source data columns. Fortunately, values for extension properties without corresponding data source fields can be set if all of the records in the source data share the same value. This called "static mapping". Following are some examples of additional sensible mappings that might made using this data set.

The "type" extension property comes from the Dublin Core metadata standard and is one of the Darwin Core recommended record-level terms (http://rs.tdwg.org/dwc/terms/index.htm#dcterms:type). It is meant to provide the general category to which the record belongs. For this data set, all of the records are about specimens, which correspond to the Dublin Core type "PhysicalObject".

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceType.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceType.png)

Note that the row in the Mappings table for the "type" extension property differs from that of most other properties in two ways. First, there is a documentation icon ![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/Control-DocumentationIcon.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/Control-DocumentationIcon.png) between the information icon and the left-most select box. Second, there is a second select box to the right of the first one, whereas most rows in the table have a text box there instead. Both of these differences indicate that the "type" extension property is governed by a controlled vocabulary. Click on the documentation icon to open a page with details about the Dublin Core Type Vocabulary in a separate window.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceTypeVocabulary.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceTypeVocabulary.png)

The detail page gives a description of the purpose of the vocabulary and a link to the original documentation, followed by a table listing all of the valid values for "type"  in the left-hand column with preferred and alternative labels for the term in various languages in the right-hand column. Note that "PhysicalObject" is one of the valid "type" values and has a preferred English label "Physical Object".

Navigate back to the Darwin Core Mappings page from which the vocabulary detail page was opened. Click on the right-most select box for the "type" extension property. The select box contains a list of the preferred terms from the controlled vocabulary, which we just saw in the Dublin Core Type Vocabulary detail page. Select "Physical Object". By making this selection, we are telling the IPT that every record in the source data file may set to the valid "type" vocabulary value ("PhysicalObject") corresponding to the selected preferred term "Physical Object".

The datasetName extension property is another good candidate for static mapping. All of the records in the data set come from the same source, a data set called "PonTaurus". Simply type "PonTaurus" into the text box in the row in the Mappings table for datasetName and leave the corresponding select box empty. This will tell the IPT that there is no source data column from which to get the datasetName, so use the value in the text box for every record.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceDatasetName.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceDatasetName.png)

The country extension property was mapped automatically by the IPT, but closer examination will reveal that the country field actually contains country codes rather than country names as the Darwin Core description of country (http://rs.tdwg.org/dwc/terms/index.htm#country) recommends. There are two things we can do to remedy this situation. First, we can map the countryCode extension property directly to the COUNTRY data source column. Then we can use a translation to set the country name to the full name based on the country code value (see step 13).

To map the countryCode extension property to the COUNTRY data source column, simply choose COUNTRY from the list of column names in the select box for countryCode in the Mappings table.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceCountryCode.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceCountryCode.png)

### 13) Create a source data translation
Quick Reference Guide: [Manage Resources Menu/Resource Overview/Darwin Core Mappings](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Darwin_Core_Mappings)

The COUNTRY data source column contains country codes rather than full country names. You can see this by looking at the values shown next to the label "Source Sample" in the country row of the Mappings table, where the values of the COUNTRY column are given for the first five rows of the source data, separated by the character '|'.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceCountry.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceCountry.png)

We can supply the full name for the country based on translations of the country codes given in the COUNTRY column. Click on the button labeled "Add" next to the label "Translation" in right-hand column of the Mappings table for the country extension property. This will open a Value Translation page for the country extension property.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceValueTranslation.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceValueTranslation.png)

The message at the top of the page indicates that the IPT was able to discern just one distinct value in the COUNTRY column of the source data file. That one value, "TR" is given under the "Source Value" header of the Translation Table, with a blank text box under the "Translated Value" header. Type the value "Turkey" into the Translated Value column and click on the button labeled "Save". This will enable the translation from the value "TR" to the value "Turkey" for all records in the source data file.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceCountryAfterTranslation.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceCountryAfterTranslation.png)

The existence of the translation is now indicated by the the link labeled "1 terms" next to the label "Translation" in right-hand column of the Mappings table for the country extension property. This link replaces the Add button, and will open the existing translation table when you click on it.

For this particular data source we could have achieved the same result for the contents of the country extension property by creating a static mapping since there was only one value for the COUNTRY column in the whole source data file.

Return to the Resource Overview page by clicking on the link labeled "back to resource overview page" near the top of the page above the Mappings table. Note that the summary in the Darwin Core Mappings area of the Resource Overview page now shows that 27 terms from the Darwin Core Occurrence extension have now been mapped to columns in the occurrencedata source data file, or to static values.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceMappingsAfterMapping.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceMappingsAfterMapping.png)

### 14) Make the resource public
Quick Reference Guide: [Manage Resources Menu/Resource Overview/Visibility](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Visibility)

The resource entitled "My Occurrence Resource" is currently not visible to anyone other than those having managerial or administrative roles with respect to it, as indicated by the label "Private" in the Visibility area of the Resource Overview page.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceVisibilityPrivate.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceVisibilityPrivate.png)

To increase the visibility to all potential users, click on the button labeled "Public". Doing so will show a message at the top of the Resource Overview page indicating that the Visibility status has changed and will show a button labeled "Private" instead of the one labeled "Public" in the Visibility area of the page along with a disabled button labeled "Register". In addition, if the resource has not been published, a message to that effect will appear in the Visibility area.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceVisibilityPublic.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceVisibilityPublic.png)

### 14) Register the resource
If the IPT instance you are using is in production use (it is not supposed to be for tutorials, see prerequisite 1), please do not continue beyond this point in the tutorial, as registering test instances of the IPT and example resources will add spurious information to the GBIF registry. An IPT in non-production use will use an alternate test registry to avoid this problem.

Though the resource has been made public, it is not yet registered in the GBIF Registry, which means it is not discoverable by those who aren't informed of the resource's existence. Registering the resource is the best way to do this. Note the informational message in the Visibility area of the Resource Overview page (see screen image above). It suggests that a user having the Admin role has to configure organisations for the IPT before resource registration is possible. Normally this step would already have been taken by an IPT administrator. We'll take a detour now to accomplish this task.

### 15) Register the IPT
Quick Reference Guide: [Manage Resources Menu/Resource Overview/Configure Organisations](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Configure_Organisations)

Quick Reference Guide: [Manage Resources Menu/Resource Overview/Configure GBIF registration options](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Configure_GBIF_registration_options)

In step 14, the informational message suggested that the IPT has to be associated with one or more organisations before a resource can be registered. Organisations can be made available for use in the IPT through the Configure Organisations page under the Administration Menu. Click on the Administration Menu. Note that the Organisations button is disabled because the IPT has not yet been registered with GBIF (see screen image in step 2). To do so, click on the button labeled "GBIF registration options". The preliminary registration options page will open showing the Base URL for the IPT and a button labeled "Validate".

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/IPTAdminRegistrationStep1.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/IPTAdminRegistrationStep1.png)

The validation step tests to see if the IPT instance is properly accessible via the Internet. When the test is finished, controls to select the organisation as well as other basic IPT information will appear on the GBIF registration options page below the Validate button.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/IPTAdminRegistrationStep2.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/IPTAdminRegistrationStep2.png)

Select "AAA1Organisation" in the Organisation list box. This will populate the text box labeled "Alias" with the value "AAA1Organisation".

Change the value in the Alias text box to "AAA1". This will force the title of this organisation to the value of the Alias wherever this organisation appears in the IPT, including in select boxes with lists of available organisations.

Enter the value "aaa1" in the text box labeled "Organisation's Password". The password will be obscured as it is typed as a security measure. This is the password on record in the test registry for the organisation entitled "AAA1Organisation", and is needed to affirm that the administrator associating this IPT with the organisation has the right to do so based on knowing the organisation's password.

Enter the value "Tutorial IPT" in the text box labeled "Title for the IPT Installation". The IPT can be found via its title in the GBIF Registry (in this case the test version) once it has been successfully registered. Fill in all of the remaining fields. Of these, the Contact Email and IPT Password are required. Use your email address for the Contact Email and a password to be used to edit the entry for this IPT installation in the GBIF Registry (though we will not do so as a part of this tutorial). When finished entering the additional information, click on the button labeled "Save". If there are no validation errors, the GBIF registration options page will display a message indicating that the IPT has been successfully registered and show the titles as links to the entries in the GBIF Registry for both the IPT instance and the organisation with which it is associated. The IPT has now been associated with an organisation, which will be available to associate with individual resources.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceAfterRegistration.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceAfterRegistration.png)

For an IPT that hosts resources for just on organisation, this is all that has to be done. If the IPT will host resources for multiple institutions, these can be added through the Configure Organisations page, which is enabled now that the IPT has been registered. We will not configure further organisations in this tutorial.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceOrgsEnabled.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceOrgsEnabled.png)

### 16) Try again to register the resource
Quick Reference Guide: [Manage Resources Menu/Resource Overview/Visibility](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Visibility)

Navigate once again to the Resource Overview page for "My Occurrence Resource" by clicking on the Manage Resources Menu, then by clicking on the link for the resource title "My Occurrence Resource" in the table of resources you have the right to manage. The Visibility area of the Resource Overview page now shows an organisation select box in which the one associated organisation in this IPT, "AAA1Organisation" appears, a disabled button labeled "Register", and a message indicating that the resource needs to be published before it can be registered.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceVisibilityAfterIPTRegistration.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceVisibilityAfterIPTRegistration.png)

### 17) Publish the resource
Quick Reference Guide: [Manage Resources Menu/Resource Overview/Published Release](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Published_Release)

The Published Release area of the Resource Overview page contains a button labeled "Publish". Click on the Publish button to initiate the publication process.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/IPTManageResourcePublish.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/IPTManageResourcePublish.png)

When the publication process is finished, a status page will appear giving a summary of everything that just occurred. If no errors occurred, an information message will show that the archive was generated.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceAfterPublish.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceAfterPublish.png)

There will also be a link on the page labeled "resource overview". Click on this link to return to the resource overview page. The Published Release area of the page now shows summary information about the results of the publication process, including links to the files that were created, and to the publication report.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourcePublishedReleaseAfterPublish.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourcePublishedReleaseAfterPublish.png)

### 18) Try one more time to register the resource
Quick Reference Guide: [Manage Resources Menu/Resource Overview/Visibility](http://code.google.com/p/gbif-providertoolkit/wiki/IPT2ManualNotes#Visibility)

The Visibility area of the Resource Overview page now shows an enabled Register button and no messages indicating obstacles to registering.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceVisibilityAfterPublish.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceVisibilityAfterPublish.png)

Click on the Register button and then click on "Yes" when prompted by the question "Are you sure?" When the registration is complete, the left-hand column of the Visibility area will show a summary of the registration information for the resource, including a link to view the resource information in the GBIF Registry.

![http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceVisibilityAfterRegister.png](http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-ipt-docs/ipt2/OccurrenceResourceTutorial/IPTTutorialOccurrenceResourceVisibilityAfterRegister.png)

Congratulations! You have succeeded in creating and publishing a Darwin Core Occurrence resource.