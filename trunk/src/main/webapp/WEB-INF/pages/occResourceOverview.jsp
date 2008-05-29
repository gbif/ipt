<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<fmt:message key='occResourceOverview.heading'/>"/>
	<s:head theme="ajax" debug="true"/>
</head>

<p><strong><fmt:message key='resource.title'/></strong>: <s:property value="occResource.title"/></p>
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

<s:form id="occResourceForm" action="saveOccResource" method="post" validate="true">
    <s:hidden key="occResource.id"/>
    <li class="buttonBar bottom">
        <c:if test="${not empty occResource.id}">
			<s:submit cssClass="button" method="edit" key="button.edit" theme="simple"/>
			<s:submit cssClass="button" method="map" key="button.map" theme="simple"/>
			<s:submit cssClass="button" method="validateRecords" key="button.validate" theme="simple"/>
			<s:submit cssClass="button" method="upload" key="button.upload" theme="simple"/>
            <s:submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('OccResource')" theme="simple"/>
			<s:submit cssClass="button" method="explore" key="button.explore" theme="simple"/>
        </c:if>
    </li>
</s:form>

<script type="text/javascript">
    Form.focusFirstElement($("occResourceForm"));
</script>
