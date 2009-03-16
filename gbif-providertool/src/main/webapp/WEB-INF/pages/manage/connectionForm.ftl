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
        <@s.hidden name="resource_id" value="%{resource.id}"/>
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


<h2>JDBC driver resources</h2>
<div class="horizontal_dotted_line_large_soft"></div>
<p>
	Some of the above JDBC drivers have licenses that do not allow us to bundle them with this software. 
	You will therefore have to download them yourselves, add the jar to the IPT lib folder and restart the application server.
	On Tomcat the IPT lib folder is at <i>&lt;%CATALINA_HOME%&gt;/webapps/ipt/WEB-INF/lib</i> 
</p>
<ul class="plain">
	<li><a href="http://www.microsoft.com/downloads/details.aspx?FamilyId=F914793A-6FB4-475F-9537-B8FCB776BEFD&displaylang=en">Microsoft SQL Server JDBC Driver 2.0</a>,com.microsoft.sqlserver.jdbc.SQLServerDriver, jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks;user=UserName;password=*****</li>
</ul>