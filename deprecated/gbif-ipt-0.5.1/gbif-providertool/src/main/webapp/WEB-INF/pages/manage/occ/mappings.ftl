<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="submenu" content="manage_resource"/>
    <script>
    	function copySQL(sql){
    		$('mappingSourceSql').value=sql;
    	}
    </script>
</head>

<#-- See if sources exist -->
<#if (resource.sources?size<1)>
	<p class="reminder">There are no source views configured.<br/>
		Please <a href="<@s.url action="sources"/>">define at least one source</a> first.
	</p>
<#else>
	<p>
		Data in the IPT is organised along the <a href="">star schema</a>. 
		There is a core, fixed table that you need to map your data to.
		And there are a number of extension tables that you can optionally configure and the IPT administrator can define. 
	</p>
</#if>
			

<fieldset>
<legend>Your Existing Mappings</legend>
	<i>(could also be visualised with graphviz)</i>
	<div>
	  <@s.form action="propertyMapping" method="post">
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
			<@s.submit cssClass="button right" key="button.save" />
		</div>
	</#if>
	  </@s.form>
	</div>
	<div class="break"></div>
	
	<@s.iterator value="extMappings" status="stat">
	<div class="newline">
	  <@s.form action="propertyMapping" method="get">
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
<fieldset>
<legend>Add New Mapping</legend>
	<div>
	  <@s.form action="propertyMapping" method="post">
	   <@s.hidden key="resource_id"/>
	   <@s.hidden key="mid" value=""/>
		<div class="left">
		 	<@s.select key="mappings.extensions" name="eid" required="false" emptyOption="false" list="extensions" listKey="id" listValue="name"/>
		</div>
		<div class="left">
		 	<@s.select key="mappings.sources" name="sid" required="false" emptyOption="false" list="sources" listKey="id" listValue="name"/>
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
  <@s.submit cssClass="button" key="button.next" theme="simple"/>
</@s.form>
</div>
