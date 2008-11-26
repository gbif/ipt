<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="${resource.title!}"/>
    <meta name="submenu" content="manage_resource"/>
</head>

<p>Please describe the dataset you want to publish as a whole</p>

<#include "/WEB-INF/pages/manage/resourceMetadataForm.ftl">  

<#if resource.creator??>
<p>
	<@s.text name="dataResource.lastModified"/> ${resource.modified} by ${resource.modifier.getFullName()}
</p>
</#if>