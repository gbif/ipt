<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="submenu" content="manage_resource"/>    
</head>

<@s.form id="sqlSourceForm" action="saveSource" method="post">
  <@s.hidden key="resource_id"/>
  <@s.hidden key="sid"/>
  <@s.textfield key="source.name" required="true" cssClass="text large"/>
  <@s.textarea key="source.sql" required="true" cssClass="text xlarge"/>
  
	<@s.submit cssClass="button" key="button.save" theme="simple"/>
	<@s.submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
	<@s.submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('source')" theme="simple"/>
</@s.form>


