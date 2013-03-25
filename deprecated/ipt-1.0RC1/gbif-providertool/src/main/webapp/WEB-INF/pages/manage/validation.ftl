<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
    
    <script>
		function confirmReset() {   
		    var msg = "Are you sure you want to reset this resource? This will remove all uploaded data and files and clear the entire cache.";
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
  <@s.hidden key="resource_id"/>
  <fieldset>
    <legend><@s.text name="occResourceOverview.validation"/></legend>
    <#if resource.hasData()>
		<div><@s.a href="%{logsUrl}">Upload error logs</@s.a></div>
	    <@s.submit cssClass="button" key="button.validate"/>
    <#else>
    	<p class="reminder">Please upload data first</p>
    </#if>
  </fieldset>
</@s.form>


