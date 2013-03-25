<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='propMappings.heading'/>"/>    
</head>

<!--<h1>Property Mappings</h1>
<div class="horizontal_dotted_line_large_foo"></div>-->
<div class="break10"></div>
<#-- See if sources exist -->
<#if (sources?size<1)>
	<p class="reminder">There are no data sources configured.<br/>
		Please <a href="<@s.url action="sources"/>">define at least one source</a> first.
	</p>
<#else>
	<p class="explMt">
		Data in the IPT is organised along the <a href="#star">star schema</a>. 
		There is a core, fixed table based on Darwin Core terms that you need to map your data to.
		And there are a number of extension tables that you can optionally configure and the IPT administrator can define.
		Each extension can hold multiple records for a single core record, e.g. you can publish many images for a species or occurrence. 
	</p>
</#if>
			

<h2>Your Existing Mappings</h2>
<div class="horizontal_dotted_line_large_soft_nm"></div>
<fieldset>
	<div>
	  <@s.form action="propMapping" method="get">
	   <@s.hidden key="resource_id"/>
	   <@s.hidden key="mid" value="${coreMapping.id}"/>
		<div class="left">
			<strong>${coreMapping.extension.name}</strong>
	<#if coreMapping.source??> 
			<span>${coreMapping.propertyMappings?size} properties mapped to source <i>${coreMapping.source.name}</i></span>
		</div>
		<div class="right">
			<@s.submit cssClass="button right" key="button.edit" />
		</div>
	<#else>
			<span>&nbsp;&nbsp;&nbsp;Assign source: </span>
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
	   <@s.hidden key="resource_id"/>
	   <@s.hidden key="mid" value="${id}"/>
		<div class="left">
			<strong>${extension.name}</strong>
			<span>${propertyMappings?size} properties mapped to source <i>${source.name}</i></span>
		</div>
		<div class="right">
			<@s.submit cssClass="button right" key="button.edit" />
		</div>
	  </@s.form>
	</div>
	</@s.iterator>
</fieldset>

<#if (extensions?size>0)>
<h2>Add New Mapping</h2>
<div class="horizontal_dotted_line_large_soft_nm"></div>
<fieldset>
	<div>
	  <@s.form action="propMapping" method="post">
	   <@s.hidden key="resource_id"/>
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

<div class="break">
<@s.form action="cache" method="get">
	<@s.hidden key="resource_id"/>
	<div class="breakRight">
		<@s.submit cssClass="button" key="button.next" theme="simple"/>
	</div>
</@s.form>
</div>

<a name="star" />
<img src="<@s.url value='/images/star_scheme.png'/>"/>
