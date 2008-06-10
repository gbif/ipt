<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="occResourceOverview.title"/></title>
    <meta name="heading" content="<fmt:message key='occResourceOverview.heading'/>"/>
	<s:head theme="xhtml" debug="true"/>
</head>

<s:form id="occResourceForm" action="saveOccResource" method="post" validate="true">
    <li style="display: none">
        <s:hidden name="resource_id" value="%{occResource.id}"/>
    </li>

    <s:textfield key="occResource.title" required="true" cssClass="text medium"/>
    <s:textfield key="occResource.serviceName" maxlength="16" cssClass="text medium"/>
    <s:textfield key="occResource.jdbcUrl" required="true" maxlength="120" cssClass="text large"/>
    <s:textfield key="occResource.jdbcUser" required="true" maxlength="120" cssClass="text large"/>
    <s:textfield key="occResource.jdbcPassword" required="true" maxlength="120" cssClass="text large"/>
    <s:textarea key="occResource.description" cssClass="text large"/>

    <c:if test="${not empty occResource.id}">
	    <p>
	    	Resource last modified by <s:property value="occResource.modifier.getFullName()"/>: <s:property value="occResource.modified"/>
	    </p>
	</c:if>
    <li class="buttonBar bottom">
        <s:submit cssClass="button" method="save" key="button.save" theme="simple"/>
        <c:if test="${not empty occResource.id}">
            <s:submit cssClass="button" method="delete" key="button.delete"
                onclick="return confirmDelete('OccResource')" theme="simple"/>
        </c:if>
        <s:submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</s:form>

<script type="text/javascript">
    Form.focusFirstElement($("occResourceForm"));
</script>
