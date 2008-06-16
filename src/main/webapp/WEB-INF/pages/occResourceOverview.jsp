<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
</head>

<s:form action="editOccResource">
  <fieldset>
    <legend>Metadata</legend>
	<s:label key="occResource.serviceName"/>
	<s:label key="occResource.description"/>
    <s:submit cssClass="button" key="button.edit"/>
  </fieldset>
</s:form>

<s:form action="editOccResource">
  <fieldset>
    <legend>Datasource</legend>
	<s:label key="occResource.isValidConnection" value="%{occResource.isValidConnection()}"/>
    <s:submit cssClass="button" key="button.edit"/>
  </fieldset>
</s:form>

<s:form action="editMapping">
  <fieldset>
    <legend>Mapping</legend>
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
	</fieldset>
</s:form>

<s:form action="uploadOccResource">
  <fieldset>
    <legend>Data Upload</legend>
	<s:label key="occResource.recordCount"/>
	<s:label key="occResource.lastImport"/>
    <s:submit cssClass="button" key="button.upload"/>
  </fieldset>
</s:form>

<s:form action="validateOccResource">
  <fieldset>
    <legend>Validation</legend>
	<s:label key="occResourceOverview.validation" value="Some validation summary..."/>
    <s:submit cssClass="button" key="button.validate"/>
  </fieldset>
</s:form>

<s:label key="occResourceOverview.manager" value="%{occResource.creator.getFullName()}"/>
<c:set var="tach">
	<s:property value="occResource.modified"/> by <s:property value="occResource.modifier.getFullName()"/>
</c:set>
<s:label key="occResourceOverview.lastModified" value="${tach}"/>

<br/>

