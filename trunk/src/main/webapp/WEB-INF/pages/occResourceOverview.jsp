<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
</head>

<p><strong><fmt:message key='resource.description'/></strong>: <s:property value="occResource.description"/></p>
<p><strong><fmt:message key='occResource.serviceName'/></strong>: <s:property value="occResource.serviceName"/></p>
<p><strong><fmt:message key='occResource.sourceJdbcConnection'/></strong>: <s:property value="occResource.sourceJdbcConnection"/></p>
<p><strong><fmt:message key='occResource.recordCount'/></strong>: <s:property value="occResource.recordCount"/></p>
<p><strong><fmt:message key='occResource.lastImport'/></strong>: <s:property value="occResource.lastImport"/></p>
<p><strong><fmt:message key='occResourceOverview.validation'/></strong>: Some validation summary</p>
<p><strong><fmt:message key='occResourceOverview.mapping'/></strong>: Some mapping summary</p>
<p><strong><fmt:message key='occResourceOverview.manager'/></strong>: <s:property value="occResource.creator.getFullName()"/></p>
<p>
	Resource last modified by <s:property value="occResource.modifier.getFullName()"/>: <s:property value="occResource.modified"/>
</p>

<div class="actionlinks">
	<a href="<s:url action="editOccResource"><s:param name="id" value="occResource.id"/></s:url>">edit resource</a>
	| <a href="<s:url action="occResourceMapping"><s:param name="id" value="occResource.id"/></s:url>">map datasource</a>
	| <a href="<s:url action="occUpload" namespace="manage"><s:param name="id" value="occResource.id"/></s:url>">upload data</a>
	| <a href="<s:url action="occValidation" namespace="manage"><s:param name="id" value="occResource.id"/></s:url>">validate data</a>
	| <a href="<s:url action="occResource" namespace="manage"><s:param name="id" value="occResource.id"/></s:url>">explore data</a>
</div>

<script type="text/javascript">
    Form.focusFirstElement($("occResourceForm"));
</script>
