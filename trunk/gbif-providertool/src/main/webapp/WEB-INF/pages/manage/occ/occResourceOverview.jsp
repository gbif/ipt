<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
	<s:head theme="ajax" debug="true"/>
</head>

<c:set var="placeholder">
	<br/><br/><br/><br/>
</c:set>

<s:form action="editResourceMetadata" method="get">
  <s:hidden key="resource_id"/>
  <fieldset>
    <legend><s:text name="occResourceOverview.metadata"/></legend>
	<s:label key="occResource.serviceName"/>
	<s:label key="occResource.description"/>
    <s:submit cssClass="button" key="button.edit"/>
  </fieldset>
</s:form>

<s:form action="editResourceConnection" method="get">
  <s:hidden name="resource_id" value="%{occResource.id}"/>
  <fieldset>
    <legend><s:text name="occResourceOverview.datasource"/></legend>
    <s:if test="%{occResource.hasMetadata()}">
		<s:label key="occResource.isValidConnection" value="%{occResource.isValidConnection()}"/>
	    <s:submit cssClass="button" key="button.edit"/>
    </s:if>
    <s:else>
    	<s:property value="placeholder"/>
    </s:else>
  </fieldset>
</s:form>

<s:form action="editMappingSource" method="get">
  <s:hidden key="resource_id"/>
  <fieldset>
    <legend><s:text name="occResourceOverview.mapping"/></legend>
    <s:if test="%{occResource.isValidConnection()}">
    
  	  <s:label key="occResourceOverview.coreMapping" />
  	  <s:push value="occResource.getCoreMapping()">
		<li>
			<s:property value="extension.name"/>
			<s:url id="mappingUrl" action="editMappingSource">
				<s:param name="mapping_id" value="id"/>
			</s:url>
			<s:a href="%{mappingUrl}"><s:property value="propertyMappings.size"/> concepts</s:a>
		</li>
  	  </s:push>
  	  
  	  <s:label key="occResourceOverview.extensionMappings"/>
      <c:if test="${not empty extensions}">
		<s:select id="extension_id" name="extension_id" list="extensions" listKey="id" listValue="name"/>
        <s:submit action="editMappingSource" method="" cssClass="button" key="button.add" theme="simple"/>
  	  </c:if>
 	  <s:iterator value="occResource.getExtensionMappings()" status="mappingStatus">
		<s:url id="mappingUrl" action="editMappingSource">
			<s:param name="mapping_id" value="id"/>
		</s:url>
		<ul class="subform">
			<li>
				<s:property value="extension.name"/>
				<s:a href="%{mappingUrl}"><s:property value="propertyMappings.size"/> concepts</s:a>
			</li>
		</ul>
	  </s:iterator>
    </s:if>
    <s:else>
    	<c:out value="${placeholder}" escapeXml="false"/>
    </s:else>
	</fieldset>
</s:form>

<s:form action="upload" method="get">
  <s:hidden key="resource_id"/>
  <fieldset>
    <legend><s:text name="occResourceOverview.cache"/></legend>
    <s:if test="%{occResource.hasMinimalMapping()}">
    	<div class="left">
    		<s:if test="%{currentJob}">
				<s:url id="recordCountUrl" action="uploadStatus">
					<s:param name="resource_id" value="resource_id" />
					<s:param name="ajax" value="true"/>
				</s:url>
	    		<s:div id="recordCount" theme="ajax" href="%{recordCountUrl}" updateFreq="5000" >
		    		<s:property value="occResource.recordCount"/>
	    		</s:div>
    		</s:if>
    		<s:else>
				<s:label key="resource.recordCount" value="%{occResource.recordCount}"/>
    		</s:else>
			<s:label key="occResource.lastImport"/>
			<s:label key="occResourceOverview.nextUpload" value="%{nextUpload.nextFireTime}"/>
    		<s:if test="%{currentJob}">
				<s:label key="occResourceOverview.currentJob" value="%{currentJob.description}"/>
    		</s:if>
	    </div>
		<div class="right">
			<s:url id="uploadHistoryUrl" action="uploadHistory">
				<s:param name="resource_id" value="resource_id"/>
			</s:url>
			<s:a href="%{uploadHistoryUrl}">
				<img src="<s:property value="gChartData"/>" width="450" height="200"/>
			</s:a>
		</div>
		<div class="break">
		</div>
	    <s:submit cssClass="button" key="button.upload"/>
    </s:if>
    <s:else>
    	<c:out value="${placeholder}" escapeXml="false"/>
    </s:else>
  </fieldset>
</s:form>

<s:form action="validate" method="get">
  <s:hidden key="resource_id"/>
  <fieldset>
    <legend><s:text name="occResourceOverview.validation"/></legend>
    <s:if test="%{occResource.hasData()}">
		<s:label key="Problematic records"/>
		<li>
 			<a href="/logging/logEvents.html?groupId=<s:property value="occResource.id"/>"><s:property value="occResource.recordCount"/></a>
 		</li>		
	    <s:submit cssClass="button" key="button.validate"/>
    </s:if>
    <s:else>
    	<c:out value="${placeholder}" escapeXml="false"/>
    </s:else>
  </fieldset>
</s:form>

<s:label key="occResourceOverview.manager" value="%{occResource.creator.getFullName()}"/>
<s:label key="occResourceOverview.lastModified" value="%{occResource.modified} by %{occResource.modifier.getFullName()}"/>

<c:if test="${not empty occResource.id}">
  <s:form action="saveResource" method="get">
    <s:hidden key="resource_id"/>
    <s:submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('saveResource')" theme="simple"/>
  </s:form>
</c:if>

<br/>

