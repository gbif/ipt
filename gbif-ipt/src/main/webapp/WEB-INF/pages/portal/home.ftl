<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>	
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="portal.home.title"/></h1>
<p><@s.text name="portal.home.intro"/></p> 

<#if (resources?size>0)>
<table id="resourcestable" class="sortable">
	<thead>
	<tr>
		<th id="resourceLogo" class="sorttable_nosort"><@s.text name="portal.home.logo"/></th>
		<th id="resourceName"><@s.text name="portal.home.name"/></th>
		<th id="resourceOrganisation"><@s.text name="portal.home.organisation"/></th>
		<th id="resourceType"><@s.text name="portal.home.type"/></th>
		<th id="resourceSubType"><@s.text name="manage.home.subtype"/></th>
		<th id="resourceRecords"><@s.text name="portal.home.records"/></th>
		<th id="resourceLastModified"><@s.text name="portal.home.modified"/></th>
		<th id="resourceLastPublication"><@s.text name="portal.home.last.publication" /></th>
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

    <#list resources?sort_by("title") as r>
    
      <tr class="${zebra(r_index)}">
      	<td id="resourceLogo"><#if r.eml.logoUrl?has_content><img class="resourceminilogo" src="${r.eml.logoUrl}" /></#if></td>
    	<td id="resourceName"><a href="resource.do?r=${r.shortname}"><#if r.title?has_content>${r.title}<#else>${r.shortname}</#if></a></td>
    	<#-- if registrationAllowed -->
    	<td id="resourceOrganisation">
    		<#if r.status=='REGISTERED'>
    			${r.organisation.name}
    		<#else>
    			<@s.text name="manage.home.not.registered"/>
    		</#if>
    	</td>
    	<#-- >/#if -->
      <td id="resourceType">
        <#if r.coreType?has_content >
          ${r.coreType?lower_case}
        <#else>
          ${emptyString}
        </#if>
      </td>
      <td id="resourceSubType">
        <#if (r.subtype?has_content) && (r.subtype?length >= 8) >
          ${r.subtype?lower_case?substring(0,7)}${dotDot}
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
      </tr>
    </#list>
    </tbody>
    </table>

<p><@s.text name="portal.home.feed"><@s.param>${baseURL}/rss.do</@s.param></@s.text> <img id="rssImage" src="${baseURL}/images/rss.png"/>.</p>

<#else>
	<p><@s.text name="portal.home.no.public"/></p>
</#if>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>