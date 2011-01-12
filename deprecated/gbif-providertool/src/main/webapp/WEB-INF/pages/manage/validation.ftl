<head>
    <title><@s.text name="validation.title"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    
    <script>
		function confirmReset() {   
		    var msg = "<@s.text name="validation.confirm"/>";
		    ans = confirm(msg);
		    if (ans) {
		        return true;
		    } else {
		        return false;
		    }
		}
    </script>
</head>

<@s.form action="validation" method="get">
  <@s.hidden key="resourceId"/>
  <fieldset>
    <legend><@s.text name="validation.legend"/></legend>
    <#if resource.hasData()>
		<div><@s.a href="%{logsUrl}"><@s.text name="validation.errorlogs"/></@s.a></div>
	    <@s.submit cssClass="button" key="button.validate"/>
    <#else>
    	<p class="reminder"><@s.text name="validation.errorlogs"/></p>
    </#if>
  </fieldset>
</@s.form>