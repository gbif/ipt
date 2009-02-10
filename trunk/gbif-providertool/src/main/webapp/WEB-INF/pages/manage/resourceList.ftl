<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<head>
    <title><@s.text name="occResourceList.title"/></title>
    <meta name="heading" content="<@s.text name='resourceClass.${resourceType}'/>"/>
    <meta name="menu" content="MainMenu"/>
    <meta name="submenu" content="manage"/>
</head>

<style>
	h1{ margin-bottom: -5px;}
</style>

<#include "/WEB-INF/pages/inc/resourceTypeSelector.ftl">  

<#include "/WEB-INF/pages/inc/resourceManagerList.ftl">  

<@s.form id="publishingForm" action="publish" method="post" validate="false">
	<@s.submit cssClass="button right" key="button.publishAll" />
	<@s.submit cssClass="button right" method="republish" key="button.republishAll" />
</@s.form>
