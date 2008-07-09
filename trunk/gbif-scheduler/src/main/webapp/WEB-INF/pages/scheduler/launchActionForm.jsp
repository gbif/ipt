<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="launchActionDetail.title"/></title>
    <meta name="heading" content="<s:text name='launchActionDetail.heading'/>"/>
</head>

<s:form id="launchActionForm" action="saveLaunchAction" method="post" validate="true">
    <li style="display: none">
        <s:hidden key="launchAction.id"/>
    </li>
    <s:textfield key="launchAction.methodName" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="launchAction.i18nKey" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="launchAction.fullClassName" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="launchAction.methodParams" required="false" maxlength="255" cssClass="text medium"/>
    <s:textfield key="launchAction.instanceParam" required="false" maxlength="255" cssClass="text medium"/>

    <li class="buttonBar bottom">
        <s:submit cssClass="button" method="save" key="button.save" theme="simple"/>
        <c:if test="${not empty launchAction.id}">
            <s:submit cssClass="button" method="delete" key="button.delete"
                onclick="return confirmDelete('LaunchAction')" theme="simple"/>
        </c:if>
        <s:submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</s:form>

<script type="text/javascript">
    Form.focusFirstElement($("launchActionForm"));
</script>
