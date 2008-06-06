<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
</head>

<s:url id="editUrl" action="editOccResource"><s:param name="id" value="occResource.id"/></s:url>
<s:url id="uploadUrl" action="occUpload"><s:param name="id" value="occResource.id"/></s:url>
<s:url id="validateUrl" action="occValidation"><s:param name="id" value="occResource.id"/></s:url>
<s:url id="exploreUrl" action="occResource" namespace="/"><s:param name="id" value="occResource.id"/></s:url>

<p><strong><fmt:message key='resource.description'/></strong>: <s:property value="occResource.description"/></p>
<p><strong><fmt:message key='occResource.serviceName'/></strong>: <s:property value="occResource.serviceName"/></p>
<p><strong><fmt:message key='occResource.isValidConnection'/></strong>: <s:property value="occResource.isValidConnection()"/></p>
<p><strong><fmt:message key='occResource.recordCount'/></strong>: <s:property value="occResource.recordCount"/></p>
<p><strong><fmt:message key='occResource.lastImport'/></strong>: <s:property value="occResource.lastImport"/></p>
<p><strong><fmt:message key='occResourceOverview.validation'/></strong>: Some validation summary</p>
<p><strong><fmt:message key='occResourceOverview.mapping'/></strong>: Some mapping summary</p>
<p><strong><fmt:message key='occResourceOverview.manager'/></strong>: <s:property value="occResource.creator.getFullName()"/></p>
<ul>
<s:iterator value="occResource.mappings" status="mappingStatus">
	<s:url id="mappingUrl" action="viewMapping"><s:param name="id" value="id"/></s:url>
	<li>
		<s:property value="#mappingStatus.count"/>
		<s:a href="mappingUrl"> <s:property value="extension.name"/> </s:a>
		<s:property value="properties.size()"/>
	</li>
</s:iterator>
	<s:url id="newMappingUrl" action="viewMapping"><s:param name="id" value="id"/></s:url>
	<li>
		<s:a href="newMappingUrl"> <s:property value="extension.name"/> </s:a>	
	</li>
</ul>
<p>
	Resource last modified by <s:property value="occResource.modifier.getFullName()"/>: <s:property value="occResource.modified"/>
</p>

<div class="actionlinks">
	<s:a href="%{editUrl}">edit resource</s:a>
	| <s:a href="%{uploadUrl}">upload data</s:a>
	| <s:a href="%{validateUrl}">validate data</s:a>
	| <s:a href="%{exploreUrl}">explore data</s:a>
</div>

<script type="text/javascript">
    Form.focusFirstElement($("occResourceForm"));
</script>
