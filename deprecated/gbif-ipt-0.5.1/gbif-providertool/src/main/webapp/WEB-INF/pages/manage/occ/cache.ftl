<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="submenu" content="manage_resource"/>
    
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


<fieldset>
  <legend><@s.text name="dataResource.cache"/></legend>
  <p>The IPT caches all data being served. You can update the cache at any time and start a data import from your sources
   based on the transformations and mappings you have configured. 
   Depending upon the amount of data this process may take a long time during which this resource is blocked. 
  </p>

<div>
  <@s.form>
	<table>
		<#if resource.lastUpload??>
			<@s.url id="logsUrl" action="logEvents" namespace="/admin" includeParams="get">
				<@s.param name="sourceId" value="resource.lastUpload.jobSourceId" />
				<@s.param name="sourceType" value="resource.lastUpload.jobSourceType" />
			</@s.url>
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
	</table>
  </@s.form>
</div>

<div class="newline">
<#if resource.hasMinimalMapping()>
	<@s.form action="upload" method="post" >
	  <@s.hidden key="resource_id" />
	  <@s.submit cssClass="button" key="button.upload" />
	</@s.form>
<#else>
	<p class="reminder">Please finalize at least the core mapping before uploading data</p>
</#if>
</div>
    
</fieldset>

<!--
<div class="break">
	<a href="<@s.url action="history"><@s.param name="resource_id" value="resource_id"/></@s.url>">
		<img src="${gChartData}" width="400" height="200"/>
	</a>
</div>	


<div class="break">
<@s.form action="validation" method="get">
  <@s.hidden key="resource_id"/>
  <@s.submit cssClass="button" key="button.next" theme="simple"/>
</@s.form>
</div>
-->