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
    <@s.hidden name="resource_id" value="${(resource.id)!}"/>
    <@s.hidden name="resourceType" value="${(resourceType)!}"/>

	<@s.textfield key="resource.title" cssClass="text large"/>
	<@s.textfield key="resource.meta.uddiID" cssClass="text large"/>
	<@s.textfield key="resource.contactName" required="true" cssClass="text large"/>
	<@s.textfield key="resource.contactEmail" required="true" cssClass="text large"/>

	<h3>Registered Services</h3>
	<#list services.keySet() as st>
		<@s.textfield name="services[${st}]" value="${services[st]}" cssClass="text large" theme="simple"/>
	</#list>
	
	<div class="breakRightButtons">
    	<@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
	    <@s.submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
	</div>
  </fieldset>
</@s.form>
	