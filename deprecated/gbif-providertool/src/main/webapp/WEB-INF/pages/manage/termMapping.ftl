<head>
    <title><@s.text name="termmapping.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    <meta name="heading" content="<@s.text name='termmapping.heading'/> ${voc.title}"/>
</head>

<h2><@s.text name='termmapping.sourcefrom'/> <i>${column}</i> <@s.text name='termmapping.in'/> <i>${source.name}</i></h2>

<@s.form action="terMappingScan" method="get">
	<#if (termMappings?size<1)>
		<p class="reminder"><@s.text name='termmapping.noterms'/></p>
	<#else>
		<p><@s.text name='termmapping.rescan'/></p>
	</#if>
    <@s.hidden key="tid"/>
    <@s.hidden key="mid"/>
    <@s.hidden key="origin"/>
    <@s.hidden key="resourceId"/>
    <@s.submit cssClass="button" key="button.scan" theme="simple"/>
</@s.form>


<@s.form action="saveTerMapping" method="post">
    <@s.hidden key="tid"/>
    <@s.hidden key="mid"/>
    <@s.hidden key="origin"/>
    <@s.hidden key="resourceId"/>

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