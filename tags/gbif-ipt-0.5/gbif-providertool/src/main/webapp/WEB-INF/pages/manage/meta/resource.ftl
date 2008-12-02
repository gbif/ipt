<head>
    <title><@s.text name="resourceOverview.title"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="submenu" content="manage"/>
    
</head>

<#assign placeholder="<br/><br/><br/><br/>">

<@s.form action="editResourceMetadata" method="get">
  <@s.hidden key="resource_id"/>
  <fieldset>
    <legend><@s.text name="resourceOverview.metadata"/></legend>
	<img class="right" src="${cfg.getResourceLogoUrl(resource_id)}" />
	<@s.label key="resource.description"/>
	<table class="lefthead">
		<tr>
			<th>Contact</th>
			<td>${resource.contactName!} <#if resource.contactEmail??>&lt;${resource.contactEmail}&gt;</#if></td>
		</tr>
		<tr>
			<th>Homepage</th>
			<td><#if resource.link??><a href="${resource.link}">Home</a><#else>No homepage configured</#if></td>
		</tr>
		<tr>
			<th>EML</th>
			<td><a href="${cfg.getEmlUrl(resource.guid)}">${cfg.getEmlUrl(resource.guid)}</a></td>
		</tr>
	</table>
    <@s.submit cssClass="button" key="button.edit"/>
  </fieldset>
</@s.form>

<#if (resource.id)??>
  <@s.form action="saveResource" method="get">
    <@s.hidden key="resource_id"/>
    <@s.submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('resource')" theme="simple"/>
  </@s.form>
</#if>

<br/>

