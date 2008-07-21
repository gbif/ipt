<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
	<s:head theme="xhtml" debug="true"/>
</head>

<s:form id="occResourceForm" action="saveResource" method="post" validate="true">
    <li style="display: none">
        <s:hidden name="resource_id" value="%{occResource.id}"/>
        <s:hidden name="neu" value="%{isNew()}"/>
    </li>

    <s:textfield key="occResource.title" required="true" cssClass="text medium"/>
    <!--s:textfield key="occResource.serviceName" maxlength="16" cssClass="text medium"/-->
    <s:textarea key="occResource.description" cssClass="text large"/>

    <li class="buttonBar bottom">
        <s:submit cssClass="button" method="save" key="button.save" theme="simple"/>
        <s:submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</s:form>

<script type="text/javascript">
    Form.focusFirstElement($("occResourceForm"));
</script>
