<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
</head>

<c:set var="placeholder">
	<br/><br/><br/><br/>
</c:set>

<s:form action="editResourceMetadata">
  <fieldset>
    <legend>Metadata</legend>
	<s:label key="occResource.serviceName"/>
	<s:label key="occResource.description"/>
    <s:submit cssClass="button" key="button.edit"/>
  </fieldset>
</s:form>

<s:form action="editResourceConnection">
  <fieldset>
    <legend>Datasource</legend>
    <s:if test="%{occResource.hasMetadata()}">
		<s:label key="occResource.isValidConnection" value="%{occResource.isValidConnection()}"/>
	    <s:submit cssClass="button" key="button.edit"/>
    </s:if>
    <s:else>
    	<s:property value="placeholder"/>
    </s:else>
  </fieldset>
</s:form>

<s:form action="editMapping">
  <fieldset>
    <legend>Mapping</legend>
    <s:if test="%{occResource.isValidConnection()}">
  	  <s:label key="DarwinCore"/>
		<s:url id="mappingUrl" action="editMapping">
			<s:param name="mapping_id" value="occResource.getCoreMapping().id"/>
		</s:url>
		<ul class="subform">
			<li>
				<s:property value="occResource.getCoreMapping().extension.name"/>
				<s:a href="%{mappingUrl}"><s:property value="propertyMappings.size"/> concepts</s:a>
			</li>
		</ul>
  	  
  	  <s:label key="occResourceOverview.mapping"/>
      <c:if test="${not empty extensions}">
		<s:select id="extension_id" name="extension_id" list="extensions" listKey="id" listValue="name"/>
        <s:submit action="editMapping" method="" cssClass="button" key="button.add" theme="simple"/>
  	  </c:if>
 	  <s:iterator value="occResource.mappings" status="mappingStatus">
		<s:url id="mappingUrl" action="editMapping">
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

<s:form action="upload">
  <fieldset>
    <legend>Data Upload</legend>
    <s:if test="%{occResource.hasMapping()}">
		<s:label key="occResource.recordCount"/>
		<s:label key="occResource.lastImport"/>
	    <s:submit cssClass="button" key="button.upload"/>
    </s:if>
    <s:else>
    	<c:out value="${placeholder}" escapeXml="false"/>
    </s:else>
  </fieldset>
</s:form>

<s:form action="validate">
  <fieldset>
    <legend>Validation</legend>
    <s:if test="%{occResource.hasData()}">
		<s:label key="occResourceOverview.validation" value="Some validation summary..."/>
	    <s:submit cssClass="button" key="button.validate"/>
    </s:if>
    <s:else>
    	<c:out value="${placeholder}" escapeXml="false"/>
    </s:else>
  </fieldset>
</s:form>

<s:label key="occResourceOverview.manager" value="%{occResource.creator.getFullName()}"/>
<c:set var="tach">
	<s:property value="occResource.modified"/> by <s:property value="occResource.modifier.getFullName()"/>
</c:set>
<s:label key="occResourceOverview.lastModified" value="${tach}"/>

<c:if test="${not empty occResource.id}">
  <s:form action="saveResource">
    <s:hidden name="resource_id" value="%{occResource.id}"/>
    <s:submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('saveResource')" theme="simple"/>
  </s:form>
</c:if>

<br/>

