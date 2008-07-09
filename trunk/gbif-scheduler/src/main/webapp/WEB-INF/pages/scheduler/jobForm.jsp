<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="jobDetail.title"/></title>
    <meta name="heading" content="<s:text name='jobDetail.heading'/>"/>
</head>

<s:form id="jobForm" action="saveJob" method="post" validate="true">
    <li style="display: none">
        <s:hidden key="job.id"/>
    </li>
    <s:textfield key="job.name" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="job.description" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="job.jobGroup" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="job.runningGroup" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="job.jobClassName" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="job.dataAsJSON" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="job.created" required="false" maxlength="255" cssClass="text" size="11" title="date"/>
    <s:textfield key="job.nextFireTime" required="false" maxlength="255" cssClass="text" size="11" title="date"/>
    <s:textfield key="job.started" required="false" maxlength="255" cssClass="text" size="11" title="date"/>
    <s:textfield key="job.instanceId" required="false" maxlength="255" cssClass="text medium"/>

    <li class="buttonBar bottom">
        <s:submit cssClass="button" method="save" key="button.save" theme="simple"/>
        <c:if test="${not empty job.id}">
            <s:submit cssClass="button" method="delete" key="button.delete"
                onclick="return confirmDelete('Job')" theme="simple"/>
        </c:if>
        <s:submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</s:form>

<script type="text/javascript" src="<c:url value='/scripts/calendar/calendar.js'/>"></script>
<script type="text/javascript" src="<c:url value='/scripts/calendar/calendar-setup.js'/>"></script>
<script type="text/javascript" src="<c:url value='/scripts/calendar/lang/calendar-en.js'/>"></script>
<script type="text/javascript">
    Form.focusFirstElement($("jobForm"));
    Calendar.setup({inputField: "jobForm_job_created", ifFormat: "%m/%d/%Y", button: "job.createdDatePicker"});
    Calendar.setup({inputField: "jobForm_job_nextFireTime", ifFormat: "%m/%d/%Y", button: "job.nextFireTimeDatePicker"});
    Calendar.setup({inputField: "jobForm_job_started", ifFormat: "%m/%d/%Y", button: "job.startedDatePicker"});
</script>
