<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
</head>

<s:url id="editUrl" action="editOccResource"><s:param name="resource_id" value="occResource.id"/></s:url>
<s:url id="uploadUrl" action="occUpload"><s:param name="resource_id" value="occResource.id"/></s:url>
<s:url id="validateUrl" action="occValidation"><s:param name="resource_id" value="occResource.id"/></s:url>
<s:url id="exploreUrl" action="occResource" namespace="/"><s:param name="id" value="occResource.id"/></s:url>

<s:form action="occResources">
	<s:label key="occResource.description"/>
	<s:label key="occResource.serviceName"/>
	<s:label key="occResource.isValidConnection" value="%{occResource.isValidConnection()}"/>
	<s:label key="occResource.recordCount"/>
	<s:label key="occResource.lastImport"/>
	<s:label key="occResourceOverview.validation" value="Some validation summary..."/>
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
	<s:label key="occResourceOverview.manager" value="%{occResource.creator.getFullName()}"/>
	<c:set var="tach">
		<s:property value="occResource.modified"/> by <s:property value="occResource.modifier.getFullName()"/>
	</c:set>
	<s:label key="occResourceOverview.lastModified" value="${tach}"/>
</s:form>


<div class="actionlinks">
	<s:a href="%{editUrl}">edit resource</s:a>
	| <s:a href="%{uploadUrl}">upload data</s:a>
	| <s:a href="%{validateUrl}">validate data</s:a>
	| <s:a href="%{exploreUrl}">explore data</s:a>
</div>

<br/>

