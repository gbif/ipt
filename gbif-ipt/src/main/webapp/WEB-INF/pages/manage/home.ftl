<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="title"/></title>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><@s.text name="manage.home.title"/></h1>
<p></p>

<#if (resources?size>0)>
<table id="resourcestable" class="sortable">
	<thead>
	<tr>
		<th id="resourceName"><@s.text name="manage.home.name"/></th>
		<th id="resourceOrganisation"><@s.text name="manage.home.organisation"/></th>
		<th id="resourceType"><@s.text name="manage.home.type"/></th>
		<th id="resourceSubType"><@s.text name="manage.home.subtype"/></th>
		<th id="resourceRecords"><@s.text name="manage.home.records"/></th>
		<th id="resourceLastModified"><@s.text name="manage.home.last.modified"/></th>
		<th id="resourceLastPublication"><@s.text name="manage.home.last.publication" /></th>
		<th id="resourceVisible"><@s.text name="manage.home.visible"/></th>
		<#-- see if the ADMIN has enabled registrations -->
		<#-- if registrationAllowed -->
		<th id="resourceAuthor"><@s.text name="portal.home.author"/></th>
		<#-- >/#if -->
	</tr>
	</thead>
	<tbody>

    <#-- for counting even or odd rows -->
    <#function zebra index>
      <#if (index % 2) == 0>
        <#return "even" />
      <#else>
        <#return "odd" />
      </#if>
    </#function>

    <#assign emptyString="---">
    <#assign dotDot="..">

      <#list resources as r>

      <tr class="${zebra(r_index)}">
    	<td id="resourceName"><a href="resource.do?r=${r.shortname}"><if><#if r.title?has_content>${r.title}<#else>${r.shortname}</#if></a></td>
    	<#-- if registrationAllowed -->
    	<td id="resourceOrganisation">
    		<#if r.status=='REGISTERED'>
    			${r.organisation.name}
    		<#else>
    			<@s.text name="manage.home.not.registered"/>
    		</#if>
    	</td>
      <td id="resourceType">
        <#if r.coreType?has_content >
          ${r.coreType?upper_case}
        <#else>
          ${emptyString}
        </#if>
      </td>
    	<td id="resourceSubType">
        <#if (r.subtype?has_content) && (r.subtype?length >= 8) >
          ${r.subtype?upper_case?substring(0,7)}${dotDot}
        <#else>
          ${emptyString}
        </#if>
      </td>
    	<td id="resourceRecords">${r.recordsPublished!0}</td>
    	<td id="resourceLastModified">${r.modified?date}</td>
    	<td id="resourceLastPublication">
    		<#if r.published>
    			${(r.lastPublished?date)!}
    		<#else>			
    			<@s.text name="portal.home.not.published"/>
    		</#if>	
    	</td>
    	<td id="resourceVisible">
    		<#if r.status=='PRIVATE'>
    			<@s.text name="manage.home.visible.private"/>
    		<#else>
    			<@s.text name="manage.home.visible.public"/>
    		</#if>
    	</td>
    	<td id="resourceAuthor">${r.creator.firstname!} ${r.creator.lastname!}</td>
    	<#-- >/#if -->
      </tr>
    </#list>
    </tbody>
    </table>

<#else>
	<p><@s.text name="manage.home.resources.none"/></p>
</#if>

    <div id="new-resource" class="grid_18">
    
    <h2><@s.text name="manage.resource.create.title"/></h2>
    <#include "inc/create_new_resource.ftl"/>

    </div>
    </div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
