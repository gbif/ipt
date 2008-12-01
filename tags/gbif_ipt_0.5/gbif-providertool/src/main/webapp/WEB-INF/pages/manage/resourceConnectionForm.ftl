<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="submenu" content="manage_resource"/>
</head>

<@s.form id="occResourceForm" action="saveConnection" method="post" validate="true">
    <li style="display: none">
        <@s.hidden name="resource_id" value="%{resource.id}"/>
    </li>

 	<@s.select key="resource.jdbcDriverClass" required="true" headerKey="Select database system" emptyOption="false" 
		list="jdbcDriverClasses" />
    <@s.textfield key="resource.jdbcUrl" required="true" maxlength="120" cssClass="text large"/>
    <@s.textfield key="resource.jdbcUser" required="true" maxlength="120" cssClass="text large"/>
    <@s.textfield key="resource.jdbcPassword" required="true" maxlength="120" cssClass="text large"/>

    <li class="buttonBar bottom">
        <@s.submit cssClass="button" key="button.save" theme="simple"/>
        <@s.submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</@s.form>

<script type="text/javascript">
    Form.focusFirstElement($("occResourceForm"));
</script>
