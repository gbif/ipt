<head>
    <title><@s.text name="sqlSettings.heading"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='sqlSettings.heading'/>"/>
	<script>
	$(document).ready(function(){
      	$("#advancedDriver").hide();
      	// check if Other was selected
	    $("select#driverSelect").change( function () { 
	      if ($(this).val()=="${other}"){
	      	$("#advancedDriver").slideDown("normal");
	      }else{
	      	$("#advancedDriver input").val("");
	      	$("#advancedDriver").slideUp("normal");
	      }; 
	    });
	<#if jdbcDriverClass??>
      	$("#advancedDriver").show();
	</#if>
    });
	</script>    
</head>

<div class="break10"></div>

<@s.form id="occResourceForm" action="saveConnection" method="post" validate="true">
    <li style="display: none">
        <@s.hidden name="resourceId" value="%{resource.id}"/>
    </li>

 	<@s.select id="driverSelect" key="resource.jdbcDriverClass" required="true" headerKey="Select database system" emptyOption="false" list="jdbcDriverClasses" />
	<div id="advancedDriver">
    	<@s.textfield key="jdbcDriverClass" required="false" maxlength="120" cssClass="text large"/>
    </div>
    <@s.textfield key="resource.jdbcUrl" required="true" maxlength="120" cssClass="text large"/>
    <@s.textfield key="resource.jdbcUser" required="true" maxlength="120" cssClass="text large"/>
    <@s.textfield key="resource.jdbcPassword" required="true" maxlength="120" cssClass="text large"/>

    <li class="buttonBar bottom">
        <@s.submit cssClass="button" key="button.save" theme="simple"/>
        <@s.submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</@s.form>


<h2><@s.text name='connectionform.jdbcresources'/></h2>
<div class="horizontal_dotted_line_large_soft"></div>
<p>
	<@s.text name='connectionform.instructions'/> 
</p>
<dl>
		<dt>MySQL</dt>
		<dd>included v5.1.6</dd>
		<dd>default port: 3306</dd>
		<dd>com.mysql.jdbc.Driver</dd>
		<dd>jdbc:mysql://host:port/database</dd>
		
		<dt>PostgreSQL</dt>
		<dd>included v8.2-504.jdbc3</dd>
		<dd>default port: 5432</dd>
		<dd>org.postgresql.Driver</dd>
		<dd>jdbc:postgresql://host:port/database?charSet=LATIN1&compatible=7.2</dd>

		<dt>Sybase</dt>
		<dd>included v1.2.2</dd>
		<dd>default port: 1433</dd>
		<dd>net.sourceforge.jtds.jdbc.Driver</dd>
		<dd>jdbc:jtds:sybase://server:port/database</dd>

		<dt>Microsoft SQL Server</dt>
		<dd>included v1.2.2</dd>
		<dd>default port: 1433</dd>
		<dd>net.sourceforge.jtds.jdbc.Driver</dd>
		<dd>jdbc:jtds:sqlserver://server/database</dd>

		<dt>Microsoft SQL Server</dt>
		<dd><a href="http://www.microsoft.com/downloads/details.aspx?FamilyId=F914793A-6FB4-475F-9537-B8FCB776BEFD&displaylang=en">download</a></dd>
		<dd>default port: 1433</dd>
		<dd>com.microsoft.sqlserver.jdbc.SQLServerDriver</dd>
		<dd>jdbc:microsoft:sqlserver://host:port;databasename=database</dd>

		<dt>Oracle</dt>
		<dd>included v14</dd>
		<dd>default port: 1521</dd>
		<dd>oracle.jdbc.OracleDriver</dd>
		<dd>jdbc:oracle:thin:@//host:port/database</dd>

		<dt>H2</dt>
		<dd>included v1.1.104</dd>
		<dd>org.h2.Driver</dd>
		<dd>jdbc:h2:/ABSOLUTE/PATH/TO/DATABASE/database;auto_server=true</dd>

		<dt>Sun ODBC</dt>
		<dd>included Java5</dd>
		<dd>sun.jdbc.odbc.JdbcOdbcDriver</dd>
		<dd>jdbc:odbc:data-source-name</dd>
</dl>