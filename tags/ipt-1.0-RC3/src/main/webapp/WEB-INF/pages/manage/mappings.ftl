<head>
    <title><@s.text name="mappings.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='mappings.heading'/>"/>    
</head>

<div class="break10"></div>
<#-- See if sources exist -->
<#if (sources?size<1)>
	<p class="reminder"><@s.text name='mappings.nosources'/> <a href="<@s.url action="sources"/>"><@s.text name='mappings.define'/></a> <@s.text name='mappings.atleastone'/>
	</p>
<#else>
	<p class="explMt">
		<@s.text name='mappings.explanation.1'/> <a href="#star"><@s.text name='mappings.starschema'/></a>. <@s.text name='mappings.explanation.2'/>
	</p>
</#if>
			
<h2><@s.text name='mappings.existing'/></h2>
<div class="horizontal_dotted_line_large_soft_nm"></div>
<fieldset>
	<div>
	  <@s.form action="propMapping" method="get">
	   <@s.hidden key="resourceId"/>
	   <@s.hidden key="mid" value="${coreMapping.id}"/>
		<div class="left">
			<strong>${coreMapping.extension.name}</strong>
	<#if coreMapping.source??> 
			<span>${coreMapping.propertyMappings?size} <@s.text name='mappings.mapped'/> <i>${coreMapping.source.name}</i></span>
		</div>
		<div class="right">
			<@s.submit cssClass="button right" key="button.edit" />
		</div>
	<#else>
			<span>&nbsp;&nbsp;&nbsp;<@s.text name='mappings.assign'/></span>
		</div>
		<div class="left">
		 	<@s.select key="mappings.sources" name="sid" emptyOption="false" list="sources" listKey="id" listValue="name" theme="simple"/>
		</div>
		<div class="right">
			<@s.submit cssClass="button right" key="button.configure" />
		</div>
	</#if>
	  </@s.form>
	</div>
	<!--<div class="break"></div>-->
	
	<@s.iterator value="extMappings" status="stat">
	<div class="newline">
	  <@s.form action="propMapping" method="post">
	   <@s.hidden key="resourceId"/>
	   <@s.hidden key="mid" value="${id}"/>
		<div class="left">
			<strong>${extension.name}</strong>
			<span>${propertyMappings?size} <@s.text name='mappings.mapped'/> <i>${source.name}</i></span>
		</div>
		<div class="right">
			<@s.submit cssClass="button right" key="button.edit" />
		</div>
	  </@s.form>
	</div>
	</@s.iterator>
</fieldset>

<#if (extensions?size>0)>
<h2><@s.text name='mappings.addnew'/></h2>
<div class="horizontal_dotted_line_large_soft_nm"></div>
<fieldset>
	<div>
	  <@s.form action="propMapping" method="post">
	   <@s.hidden key="resourceId"/>
	   <@s.hidden key="mid" value=""/>
		<div class="leftMedium">
		 	<@s.select key="mappings.extensions" name="eid" required="false" emptyOption="false" list="extensions" listKey="id" listValue="name" cssClass="text medium"/>
		</div>
		<div class="leftMedium">
		 	<@s.select key="mappings.sources" name="sid" required="false" emptyOption="false" list="sources" listKey="id" listValue="name" cssClass="text medium"/>
		</div>
		<div class="right">
			<li class="wwgrp">
				<div class="wwlbl">&nbsp;</div>
				<@s.submit cssClass="button right" key="button.add"/>
			</li>
		</div>
	  </@s.form>
	</div>
</fieldset>
</#if>


<h2><@s.text name="dataResource.cache"/></h2>
<div class="horizontal_dotted_line_large_soft_nm"></div>
<p class="explMt">
<@s.text name='mappings.explanation.cache'/> 
</p>
<fieldset style="padding-top:0px;">
<div>
  <@s.form id="banane">
	<table>
		<#if resource.lastUpload??>
			<@s.url id="logsUrl" action="annotations" namespace="/" includeParams="get"/>
			<tr>
				<th><@s.text name="resource.lastUpload"/></th>
				<td>${resource.lastUpload.executionDate} (<a href="${logsUrl}">logs</a>)</td>
			</tr>
		</#if>
		<tr>
			<th><@s.text name="resource.recordCount"/></th>
			<td>${resource.recTotal}</td>
		</tr>

		
		<tr>
			<th colspan="2">&nbsp;</th>
		</tr>
		
				
 	  	<#list resource.getExtensionMappings() as v>
			<tr>
				<th>${v.extension.name}</th>
				<td>${v.recTotal}</td>
			</tr>
	  	</#list>
	</table>#
  </@s.form>
</div>

<div>
<#if resource.hasMinimalMapping()>
	<@s.form action="runImport" method="post" >
	  <@s.hidden key="resourceId" />
	  <@s.submit cssClass="button" key="button.import" />
	</@s.form>
<#else>
	<p class="reminder"><@s.text name='mappings.reminder'/></p>
</#if>
</div>
    
</fieldset>



<h2><@s.text name='mappings.dwc'/></h2>
<div class="horizontal_dotted_line_large_soft_nm"></div>

<a name="star" />
<img src="<@s.url value='/images/star_scheme.png'/>"/>
