<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="logEventDetail.title"/></title>
    <meta name="heading" content="<s:text name='logEventDetail.heading'/>"/>
</head>

<s:form id="logEventForm" action="saveLogEvent" method="post" validate="true">
    <li style="display: none">
        <s:hidden key="logEvent.id"/>
    </li>
    <s:textfield key="logEvent.message" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="logEvent.timestamp" required="false" maxlength="255" cssClass="text" size="11" title="date"/>
    <s:textfield key="logEvent.groupId" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="logEvent.level" required="true" maxlength="255" cssClass="text medium"/>
    <s:textfield key="logEvent.instanceId" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="logEvent.bioDatasource" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="logEvent.user" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="logEvent.infoAsJSON" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="logEvent.messageParams" required="false" maxlength="255" cssClass="text medium"/>

    <li class="buttonBar bottom">
        <s:submit cssClass="button" method="save" key="button.save" theme="simple"/>
        <c:if test="${not empty logEvent.id}">
            <s:submit cssClass="button" method="delete" key="button.delete"
                onclick="return confirmDelete('LogEvent')" theme="simple"/>
        </c:if>
        <s:submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</s:form>

<script type="text/javascript" src="<c:url value='/scripts/calendar/calendar.js'/>"></script>
<script type="text/javascript" src="<c:url value='/scripts/calendar/calendar-setup.js'/>"></script>
<script type="text/javascript" src="<c:url value='/scripts/calendar/lang/calendar-en.js'/>"></script>
<script type="text/javascript">
    Form.focusFirstElement($("logEventForm"));
    Calendar.setup({inputField: "logEventForm_logEvent_timestamp", ifFormat: "%m/%d/%Y", button: "logEvent.timestampDatePicker"});
</script>
