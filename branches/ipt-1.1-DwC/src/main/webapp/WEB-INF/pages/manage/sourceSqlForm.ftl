<head>
    <title><@s.text name="sources.sqlsources"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='sources.sqlsources'/>"/>              
</head>

<!--<h1>SQL Settings</h1>
<div class="horizontal_dotted_line_large_foo"></div>-->
<div class="break10"></div>

<@s.form id="sqlSourceForm" action="saveSource" method="post">
  <@s.hidden key="resourceId"/>
  <@s.hidden key="sid"/>
  <@s.textfield key="source.name" required="true" cssClass="text large"/>
  <@s.textarea key="source.sql" required="true" cssClass="text xlarge"/>
  
	<@s.submit cssClass="button" key="button.save" theme="simple"/>
	<@s.submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
	<@s.submit cssClass="button" method="delete" key="button.delete" onclick="return confirmDelete('source')" theme="simple"/>
</@s.form>


