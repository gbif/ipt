<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
	<s:head theme="xhtml" debug="true"/>
</head>

<s:form id="occResourceForm" action="saveResource" method="post" validate="true">
    <li style="display: none">
        <s:hidden name="resource_id" value="%{occResource.id}"/>
    </li>

 	<s:select key="occResource.jdbcDriverClass" required="true" headerKey="Select database system" emptyOption="false" 
		list="jdbcDriverClasses" />
    <s:textfield key="occResource.jdbcUrl" required="true" maxlength="120" cssClass="text large"/>
    <s:textfield key="occResource.jdbcUser" required="true" maxlength="120" cssClass="text large"/>
    <s:textfield key="occResource.jdbcPassword" required="true" maxlength="120" cssClass="text large"/>

    <li class="buttonBar bottom">
        <s:submit cssClass="button" method="save" key="button.save" theme="simple"/>
        <s:submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</s:form>

<script type="text/javascript">
    Form.focusFirstElement($("occResourceForm"));
</script>
