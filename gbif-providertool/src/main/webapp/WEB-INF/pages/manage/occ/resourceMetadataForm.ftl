<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<@s.property value="occResource.title"/>"/>
    <meta name="submenu" content="manage"/>
</head>

<@s.form id="occResourceForm" action="saveResource" method="post" validate="true">
    <@s.hidden name="resource_id" value="${occResource.id}"/>
    <@s.textfield key="occResource.title" required="true" cssClass="text xlarge"/>
    <@s.textarea key="occResource.description" cssClass="text xlarge"/>

    <li class="buttonBar bottom">
        <@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
        <@s.submit cssClass="button" name="cancel" key="button.cancel" theme="simple"/>
    </li>
</@s.form>

<script type="text/javascript">
    Form.focusFirstElement($("occResourceForm"));
</script>
