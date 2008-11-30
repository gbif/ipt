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
		<#if resource.lastUpload??>
			<@s.url id="logsUrl" action="logEvents" namespace="/admin" includeParams="get">
				<@s.param name="sourceId" value="resource.lastUpload.jobSourceId" />
				<@s.param name="sourceType" value="resource.lastUpload.jobSourceType" />
			</@s.url>
			<tr>
				<th colspan="2"><@s.text name="resource.lastUpload"/></th>
			</tr>
			<tr>
				<td colspan="2">${resource.lastUpload.executionDate}</td>
			</tr>
		</#if>
		<tr>
			<th><@s.text name="resource.recordCount"/></th>
			<td>${resource.recTotal}</td>
		</tr>

		<tr>
			<th colspan="2">&nbsp;</th>
		</tr>		
		<tr>
			<th><@s.text name="occResource.numTerminalTaxa"/></th>
			<td>${resource.numTerminalTaxa}</td>
		</tr>
		<tr>
			<th><@s.text name="occResource.numRegions"/></th>
			<td>${resource.numRegions}</td>
		</tr>
 	  	<#list resource.getExtensionMappings() as v>
			<tr>
				<th>${v.extension.name}</th>
				<td>${v.recTotal}</td>
			</tr>
	  	</#list>
	</table>
  </@s.form>

    <#if resource.hasMinimalMapping()>
		<@s.form action="upload" method="post" >
		  <@s.hidden key="resource_id" />
		  <@s.submit cssClass="button" key="button.upload" theme="simple"/>
		</@s.form>
		<#--
		<@s.form action="clear" method="post">
		  <@s.hidden key="resource_id" />
	      <@s.submit cssClass="button" key="button.clear" onclick="return confirmReset()" theme="simple"/>
		</@s.form>
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


<!--
<div class="break">
<@s.form action="validation" method="get">
  <@s.hidden key="resource_id"/>
  <@s.submit cssClass="button" key="button.next" theme="simple"/>
</@s.form>
</div>
-->