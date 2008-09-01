<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<s:property value="occResource.title"/>"/>
    <meta name="submenu" content="manage"/>
</head>

<c:set var="placeholder">
	<br/><br/><br/><br/>
</c:set>

<s:form action="editResourceMetadata" method="get">
  <s:hidden key="resource_id"/>
  <fieldset>
    <legend><s:text name="occResourceOverview.metadata"/></legend>
	<s:label key="occResource.description"/>
    <s:submit cssClass="button" key="button.edit"/>
  </fieldset>
</s:form>


<s:form action="editResourceConnection" method="get">
  <s:hidden name="resource_id" value="%{occResource.id}"/>
  <fieldset>
    <legend><s:text name="occResourceOverview.datasource"/></legend>
    <s:if test="%{occResource.hasMetadata()}">
	    <s:if test="%{occResource.hasDbConnection()}">
			<s:label key="occResource.hasDbConnection" value="%{occResource.jdbcUrl}"/>
	    </s:if>
	    <s:else>
	    	<p class="reminder"><s:text name="occResource.noDbConnection" /></p>
	    </s:else>
	    <s:submit cssClass="button" key="button.edit"/>
    </s:if>
    <s:else>
    	<s:property value="placeholder"/>
    </s:else>
  </fieldset>
</s:form>

<s:form action="editMapping" method="get">
  <s:hidden key="resource_id"/>
  <fieldset>
    <legend><s:text name="occResourceOverview.mapping"/></legend>
    
  	  <s:label key="occResourceOverview.coreMapping" />
  	  <s:push value="occResource.getCoreMapping()">
		<li>
			<s:property value="extension.name"/>
			<s:url id="mappingUrl" action="editMapping" includeParams="get">
				<s:param name="mapping_id" value="id"/>
			</s:url>
			<s:a href="%{mappingUrl}"><s:property value="propertyMappings.size"/> concepts</s:a>
		</li>
  	  </s:push>
  	  
  	  <s:label key="occResourceOverview.extensionMappings"/>
      <c:if test="${not empty extensions}">
		<s:select id="extension_id" name="extension_id" list="extensions" listKey="id" listValue="name"/>
        <s:submit action="editMapping" method="" cssClass="button" key="button.add" theme="simple"/>
  	  </c:if>
 	  <s:iterator value="occResource.getExtensionMappings()" status="mappingStatus">
		<s:url id="mappingUrl" action="editMapping" includeParams="get">
			<s:param name="mapping_id" value="id"/>
		</s:url>
		<ul class="subform">
			<li>
				<s:property value="extension.name"/>
				<s:a href="%{mappingUrl}"><s:property value="propertyMappings.size"/> concepts</s:a>
			</li>
		</ul>
	  </s:iterator>
	</fieldset>
</s:form>

<fieldset>
  <legend><s:text name="occResourceOverview.cache"/></legend>
  <s:form>
	<div class="left">
		<s:label key="resource.recordCount" value="%{occResource.getRecordCount()}"/>
		<s:label key="occResource.numTerminalTaxa"/>
		<s:label key="occResource.numRegions"/>
	  	<s:label key="occResourceOverview.extensionCache"/>
	 	  <s:iterator value="occResource.getExtensionMappings()" status="mappingStatus">
			<ul class="subform">
				<li>
					<s:property value="extension.name"/>
					1000 records
				</li>
			</ul>
		  </s:iterator>
		<s:if test="%{occResource.lastUpload}">
			<s:label key="resource.lastUpload" value="%{occResource.lastUpload.executionDate}"/>
			<s:url id="logsUrl" action="logEvents" namespace="/admin" includeParams="get">
				<s:param name="sourceId" value="occResource.lastUpload.jobSourceId" />
				<s:param name="sourceType" value="occResource.lastUpload.jobSourceType" />
			</s:url>
			<s:a href="%{logsUrl}">log entries</s:a>
		</s:if>
    </div>
	<div class="right">
		<s:url id="uploadHistoryUrl" action="history">
			<s:param name="resource_id" value="resource_id"/>
		</s:url>
		<s:a href="%{uploadHistoryUrl}">
			<img src="<s:property value="gChartData"/>" width="400" height="200"/>
		</s:a>
	</div>
  </s:form>	
	<div class="break">
    <s:if test="%{occResource.hasMinimalMapping()}">
		<s:form action="upload" method="post">
		  <s:hidden key="resource_id"/>
		  <s:submit cssClass="button" key="button.upload"/>
		</s:form>
		<s:form action="clear" method="post">
		  <s:hidden key="resource_id"/>
	      <s:submit cssClass="button" key="button.clear"/>
		</s:form>
		<s:form action="process" method="post">
		  <s:hidden key="resource_id"/>
	      <s:submit cssClass="button" key="button.process" />
		</s:form>
    </s:if>
    <s:else>
    	<p class="reminder">Please finalize the core mapping before uploading data</p>
    </s:else>
	</div>
  </fieldset>

<s:form action="validate" method="get">
  <s:hidden key="resource_id"/>
  <fieldset>
    <legend><s:text name="occResourceOverview.validation"/></legend>
    <s:if test="%{occResource.hasData()}">
	    <s:submit cssClass="button" key="button.validate"/>
    </s:if>
    <s:else>
    	<p class="reminder">Please upload data first</p>
    </s:else>
  </fieldset>

  <s:label key="occResourceOverview.manager" value="%{occResource.creator.getFullName()}"/>
  <s:label key="occResourceOverview.lastModified" value="%{occResource.modified} by %{occResource.modifier.getFullName()}"/>
</s:form>


<c:if test="${not empty occResource.id}">
  <s:form action="saveResource" method="get">
    <s:hidden key="resource_id"/>
    <s:submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('resource')" theme="simple"/>
  </s:form>
</c:if>

<br/>

