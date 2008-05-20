<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="resourceMetadataDetail.title"/></title>
    <meta name="heading" content="<fmt:message key='resourceMetadataDetail.heading'/>"/>
</head>

<s:form id="resourceMetadataForm" action="saveResourceMetadata" method="post" validate="true">
    <li style="display: none">
        <s:hidden key="resourceMetadata.id"/>
    </li>
    <s:textfield key="resourceMetadata.modified" required="false" maxlength="255" cssClass="text" size="11" title="date"/>
    <s:textfield key="resourceMetadata.uri" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="resourceMetadata.uuid" required="false" maxlength="36" cssClass="text medium"/>
    <s:textfield key="resourceMetadata.description" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="resourceMetadata.title" required="false" maxlength="255" cssClass="text medium"/>

    <li class="buttonBar bottom">
        <s:submit cssClass="button" method="save" key="button.save" theme="simple"/>
        <c:if test="${not empty resourceMetadata.id}">
            <s:submit cssClass="button" method="delete" key="button.delete"
                onclick="return confirmDelete('ResourceMetadata')" theme="simple"/>
        </c:if>
        <s:submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</s:form>

<script type="text/javascript" src="<c:url value='/scripts/calendar/calendar.js'/>"></script>
<script type="text/javascript" src="<c:url value='/scripts/calendar/calendar-setup.js'/>"></script>
<script type="text/javascript" src="<c:url value='/scripts/calendar/lang/calendar-en.js'/>"></script>
<script type="text/javascript">
    Form.focusFirstElement($("resourceMetadataForm"));
    Calendar.setup({inputField: "resourceMetadataForm_resourceMetadata_modified", ifFormat: "%m/%d/%Y", button: "resourceMetadata.modifiedDatePicker"});
</script>
