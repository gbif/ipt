<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    <meta name="heading" content="Term Mappings for ${voc.title}"/>
</head>

<h2>Source terms from <i>${column}</i> in <i>${source.name}</i></h2>

<@s.form action="terMappingScan" method="get">
	<#if (termMappings?size<1)>
		<p class="reminder">No terms found. Please lookup terms from source first.</p>
	<#else>
		<p>Rescan source for new terms:</p>
	</#if>
    <@s.hidden key="tid"/>
    <@s.hidden key="mid"/>
    <@s.hidden key="origin"/>
    <@s.hidden key="resource_id"/>
    <@s.submit cssClass="button" key="button.scan" theme="simple"/>
</@s.form>


<@s.form action="saveTerMapping" method="post">
    <@s.hidden key="tid"/>
    <@s.hidden key="mid"/>
    <@s.hidden key="origin"/>
    <@s.hidden key="resource_id"/>

	<table>
    <#list termMappings as m> 
	  <tr>
	  	<td>${m.term!}</td>
	  	<td>
			<@s.select key="termMappings[${m_index}].targetTerm" list="concepts" headerKey="" emptyOption="true" style="display: inline" theme="simple"/>
		</td>
	  </tr>
    </#list>
 	</table>
	
	<div class="break"></div>
	<#if (termMappings?size>0)>
    <@s.submit cssClass="button" key="button.save" theme="simple"/>
    </#if>
    <@s.submit cssClass="button" name="cancel" key="button.done" theme="simple"/>
 
</@s.form> 
