<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<@s.property value="occResource.title"/>"/>
    <meta name="submenu" content="manage"/>
    
    <script>
		function confirmReset() {   
		    var msg = "Are you sure you want to reset this resource? This will remove all uploaded data and files and clear the entire cache.";
		    ans = confirm(msg);
		    if (ans) {
		        return true;
		    } else {
		        return false;
		    }
		}
    </script>

</head>

<#assign placeholder="<br/><br/><br/><br/>">

<@s.form action="editResourceMetadata" method="get">
  <@s.hidden key="resource_id"/>
  <fieldset>
    <legend><@s.text name="occResourceOverview.metadata"/></legend>
	<img class="right" src="${cfg.getResourceLogoUrl(resource_id)}" />
	<@s.label key="occResource.description"/>
	<table class="lefthead">
		<tr>
			<th>Contact</th>
			<td>${occResource.contactName!} <#if occResource.contactEmail??>&lt;${occResource.contactEmail}&gt;</#if></td>
		</tr>
		<tr>
			<th>Homepage</th>
			<td><#if occResource.link??><a href="${occResource.link}">Home</a><#else>No homepage configured</#if></td>
		</tr>
		<tr>
			<th>EML</th>
			<td><a href="${cfg.getEmlUrl(occResource.guid)}">${cfg.getEmlUrl(occResource.guid)}</a></td>
		</tr>
	</table>
    <@s.submit cssClass="button" key="button.edit"/>
  </fieldset>
</@s.form>


<@s.form action="editResourceConnection" method="get">
  <@s.hidden name="resource_id" value="${occResource.id}"/>
  <fieldset>
    <legend><@s.text name="occResourceOverview.datasource"/></legend>
    <#if occResource.hasMetadata()>
	    <#if occResource.hasDbConnection()>
			<@s.label key="occResource.hasDbConnection" value="${occResource.jdbcUrl}"/>
	    <#else>
	    	<p class="reminder"><@s.text name="occResource.noDbConnection" /></p>
	    </#if>
	    <@s.submit cssClass="button" key="button.edit"/>
    <#else>
    	${placeholder}
    </#if>
  </fieldset>
</@s.form>

<@s.form action="editMappingSource" method="get">
  <@s.hidden key="resource_id"/>
  <fieldset>
    <legend><@s.text name="occResourceOverview.mapping"/></legend>
		<li>
	  	    <#assign coreView=occResource.getCoreMapping()/>
			<@s.url id="sourceUrl" action="editMappingSource" includeParams="get">
				<@s.param name="mapping_id" value="${coreView.id}"/>
			</@s.url>
			<@s.url id="mappingUrl" action="editMappingProperties" includeParams="get">
				<@s.param name="mapping_id" value="${coreView.id}"/>
			</@s.url>
	  		<label class="desc"><@s.text name="occResourceOverview.coreMapping"/></label>
			<div>${coreView.extension.name}
		        <#if coreView.isMappedToFile()>
					(<@s.a href="${sourceUrl}">file source</@s.a>)
		  	    <#else>
					(<@s.a href="${sourceUrl}">sql source</@s.a>)
		  	    </#if>
				<@s.a href="%{mappingUrl}">${coreView.propertyMappings?size} properties</@s.a>
			</div>
		</li>
  	  
		<li>
	  		<label class="desc"><@s.text name="occResourceOverview.extensionMappings"/></label>
			<#list occResource.getExtensionMappings() as v>
				<@s.url id="sourceUrl" action="editMappingSource" includeParams="get">
					<@s.param name="mapping_id" value="${v.id?c}"/>
				</@s.url>
				<@s.url id="mappingUrl" action="editMappingProperties" includeParams="get">
					<@s.param name="mapping_id" value="${v.id?c}"/>
				</@s.url>
				<li>${v.extension.name}			
			        <#if coreView.isMappedToFile()>
						(<@s.a href="${sourceUrl}">file source</@s.a>)
			  	    <#else>
						(<@s.a href="${sourceUrl}">sql source</@s.a>)
			  	    </#if>
					<@s.a href="%{mappingUrl}">${v.propertyMappings?size} properties</@s.a>
				</li>
		    </#list>
		</li>
	  
	  <#-- add new extension-->
      <#if !(extensions?size==0)>
      	<li>
		<@s.select id="extension_id" name="extension_id" list="extensions" listKey="id" listValue="name" theme="simple"/>
		
        <@s.submit cssClass="button" key="button.add" theme="simple"/>
        </li>
  	  </#if>
	</fieldset>
</@s.form>

<fieldset>
  <legend><@s.text name="occResourceOverview.cache"/></legend>
  <@s.form>
	<div class="right">
		<@s.url id="uploadHistoryUrl" action="history">
			<@s.param name="resource_id" value="resource_id"/>
		</@s.url>
		<@s.a href="%{uploadHistoryUrl}">
			<img src="<@s.property value="gChartData"/>" width="400" height="200"/>
		</@s.a>
	</div>
	<table>
		<#if occResource.lastUpload??>
			<@s.url id="logsUrl" action="logEvents" namespace="/admin" includeParams="get">
				<@s.param name="sourceId" value="occResource.lastUpload.jobSourceId" />
				<@s.param name="sourceType" value="occResource.lastUpload.jobSourceType" />
			</@s.url>
			<tr>
				<th colspan="2"><@s.text name="resource.lastUpload"/></th>
			</tr>
			<tr>
				<td colspan="2">${occResource.lastUpload.executionDate}</td>
			</tr>
		</#if>
		<tr>
			<th><@s.text name="resource.recordCount"/></th>
			<td>${occResource.recTotal}</td>
		</tr>

		<tr>
			<th colspan="2">&nbsp;</th>
		</tr>		
		<tr>
			<th><@s.text name="occResource.numTerminalTaxa"/></th>
			<td>${occResource.numTerminalTaxa}</td>
		</tr>
		<tr>
			<th><@s.text name="occResource.numRegions"/></th>
			<td>${occResource.numRegions}</td>
		</tr>
 	  	<#list occResource.getExtensionMappings() as v>
			<tr>
				<th>${v.extension.name}</th>
				<td>${v.recTotal}</td>
			</tr>
	  	</#list>
	</table>
  </@s.form>

    <#if occResource.hasMinimalMapping()>
		<@s.form action="upload" method="post" >
		  <@s.hidden key="resource_id" />
		  <@s.submit cssClass="button" key="button.upload" theme="simple"/>
		</@s.form>
		<@s.form action="clear" method="post">
		  <@s.hidden key="resource_id" />
	      <@s.submit cssClass="button" key="button.clear" onclick="return confirmReset()" theme="simple"/>
		</@s.form>
		<#-- 
		<@s.form action="process" method="post">
		  <@s.hidden key="resource_id"/>
	      <@s.submit cssClass="button" key="button.process" />
		</@s.form>
		 -->
    <#else>
    	<p class="reminder">Please finalize the core mapping before uploading data</p>
    </#if>
    
    <div class="clearfix" ></div>
    
</fieldset>
	
<@s.form action="validate" method="get">
  <@s.hidden key="resource_id"/>
  <fieldset>
    <legend><@s.text name="occResourceOverview.validation"/></legend>
    <#if occResource.hasData()>
		<div><@s.a href="%{logsUrl}">Upload error logs</@s.a></div>
	    <@s.submit cssClass="button" key="button.validate"/>
    <#else>
    	<p class="reminder">Please upload data first</p>
    </#if>
  </fieldset>

  <@s.label key="occResourceOverview.manager" value="%{occResource.creator.getFullName()}"/>
  <@s.label key="occResourceOverview.lastModified" value="%{occResource.modified} by %{occResource.modifier.getFullName()}"/>
</@s.form>


<#if (occResource.id)??>
  <@s.form action="saveResource" method="get">
    <@s.hidden key="resource_id"/>
    <@s.submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('resource')" theme="simple"/>
  </@s.form>
</#if>

<br/>

