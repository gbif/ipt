<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<head>
    <title><@s.text name="dataResource.metadata"/></title>
    <meta name="resource" content="${resource.title!}"/>
    <meta name="menu" content="AdminMenu"/>
	<meta name="heading" content="${resource.title!}"/>    
</head>

<@s.form id="resourceForm" action="saveResource" method="post">
  <fieldset>
    <@s.hidden name="resourceId" value="${(resource.id)!}"/>
    <@s.hidden name="resourceType" value="${(resourceType)!}"/>

	<@s.textfield key="resource.meta.uddiID" cssClass="text large"/>
	<@s.textfield key="resource.contactName" required="true" cssClass="text large"/>
	<@s.textfield key="resource.contactEmail" required="true" cssClass="text large"/>

	<div class="break"></div>
	<h3><@s.text name='resourceform.registeredservices'/></h3>
	<#if (resource.services?size>0)>
	<#list resource.services.keySet() as st>
		<@s.textfield label="${st}" name="resource.services[${st}]" value="${resource.services[st]}" cssClass="text large" />
	</#list>
	<#else>
		<p>None</p>
	</#if>

	<div class="break"></div>
	<@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
    <@s.submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>

  </fieldset>
</@s.form>	