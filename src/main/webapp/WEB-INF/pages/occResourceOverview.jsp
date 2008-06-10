<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
</head>

<s:url id="editUrl" action="editOccResource"><s:param name="resource_id" value="occResource.id"/></s:url>
<s:url id="uploadUrl" action="occUpload"><s:param name="resource_id" value="occResource.id"/></s:url>
<s:url id="validateUrl" action="occValidation"><s:param name="resource_id" value="occResource.id"/></s:url>
<s:url id="exploreUrl" action="occResource" namespace="/"><s:param name="id" value="occResource.id"/></s:url>

<table>
	<tr>
		<th><fmt:message key='resource.description'/></th>
		<td><s:property value="occResource.description"/></td>
	</tr>
	<tr>
		<th><fmt:message key='occResource.serviceName'/></th>
		<td><s:property value="occResource.serviceName"/></td>
	</tr>
	<tr>
		<th><fmt:message key='occResource.isValidConnection'/></th>
		<td><s:property value="occResource.isValidConnection()"/></td>
	</tr>
	<tr>
		<th><fmt:message key='occResource.recordCount'/></th>
		<td><s:property value="occResource.recordCount"/></td>
	</tr>
	<tr>
		<th><fmt:message key='occResource.lastImport'/></th>
		<td><s:property value="occResource.lastImport"/></td>
	</tr>
	<tr>
		<th><fmt:message key='occResourceOverview.validation'/></th>
		<td>Some validation summary...</td>
	</tr>
	<tr>			
		<th><fmt:message key='occResourceOverview.mapping'/></th>
		<td>
        <c:if test="${not empty extensions}">
			<s:form id="addMapping" action="editMapping" namespace="/manage/occ" method="post" validate="true">
		        <s:hidden name="resource_id" value="%{occResource.id}"/>
				<s:select id="extension_id" name="extension_id" list="extensions" listKey="id" listValue="name"/>
		        <s:submit cssClass="button" key="button.add" theme="simple"/>
			</s:form>
		</c:if>
		</td>
	</tr>
	<s:iterator value="occResource.mappings" status="mappingStatus">
		<s:url id="mappingUrl" action="editMapping">
			<s:param name="mapping_id" value="id"/>
		</s:url>
		<tr>
			<td class="subrow"><s:property value="extension.name"/></td>
			<td>
				<s:a href="%{mappingUrl}"><s:property value="properties.size"/> mapped concepts</s:a>
			</td>
		</tr>
	</s:iterator>
	<tr>
		<th><fmt:message key='occResourceOverview.manager'/></th>
		<td><s:property value="occResource.creator.getFullName()"/></td>
	</tr>
	<tr>
		<th><fmt:message key='occResourceOverview.lastModified'/></th>
		<td><s:property value="occResource.modified"/>, <s:property value="occResource.modifier.getFullName()"/></td>
	</tr>
</table>


<div class="actionlinks">
	<s:a href="%{editUrl}">edit resource</s:a>
	| <s:a href="%{uploadUrl}">upload data</s:a>
	| <s:a href="%{validateUrl}">validate data</s:a>
	| <s:a href="%{exploreUrl}">explore data</s:a>
</div>

