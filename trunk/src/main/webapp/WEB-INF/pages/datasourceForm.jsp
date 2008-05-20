<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="datasourceDetail.title"/></title>
    <meta name="heading" content="<fmt:message key='datasourceDetail.heading'/>"/>
</head>

<s:form id="datasourceForm" action="saveDatasource" method="post" validate="true">
    <li style="display: none">
        <s:hidden key="datasource.id"/>
    </li>
    <!-- todo: change this to read the identifier field from the other pojo -->
    <s:select name="datasource.metadata.id" list="metadataList" listKey="id" listValue="id"></s:select>
    <s:textfield key="datasource.modified" required="false" maxlength="255" cssClass="text" size="11" title="date"/>
    <s:textfield key="datasource.serviceName" required="false" maxlength="16" cssClass="text medium"/>
    <s:textfield key="datasource.sourceJdbcConnection" required="false" maxlength="255" cssClass="text medium"/>

    <li class="buttonBar bottom">
        <s:submit cssClass="button" method="save" key="button.save" theme="simple"/>
        <c:if test="${not empty datasource.id}">
            <s:submit cssClass="button" method="delete" key="button.delete"
                onclick="return confirmDelete('Datasource')" theme="simple"/>
        </c:if>
        <s:submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</s:form>

<script type="text/javascript" src="<c:url value='/scripts/calendar/calendar.js'/>"></script>
<script type="text/javascript" src="<c:url value='/scripts/calendar/calendar-setup.js'/>"></script>
<script type="text/javascript" src="<c:url value='/scripts/calendar/lang/calendar-en.js'/>"></script>
<script type="text/javascript">
    Form.focusFirstElement($("datasourceForm"));
    Calendar.setup({inputField: "datasourceForm_datasource_modified", ifFormat: "%m/%d/%Y", button: "datasource.modifiedDatePicker"});
</script>
