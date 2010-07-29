<#include "/WEB-INF/pages/inc/header.ftl"/>
 <title>Create New Resource</title>
<script type="text/javascript">
$(document).ready(function(){
   	$("#dwca").hide();
   	$("#blank").show();
   	$("#metadata").hide();
	$("#type").change(function(){
   		if ($(this).val()=="dwca") {
		   	$("#dwca").show();
		   	$("#blank").hide();
		   	$("#metadata").hide();
   		}else if ($(this).val()=="metadata") {
		   	$("#dwca").hide();
		   	$("#blank").hide();
		   	$("#metadata").show();
   		}else{
		   	$("#dwca").hide();
		   	$("#blank").show();
		   	$("#metadata").hide();
   		}
   	});
});
</script>
<#include "/WEB-INF/pages/inc/menu.ftl"/>

<h1>Create New Resource</h1>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<@s.form cssClass="ftlTopForm" action="resource.do" method="post">
  	<@input name="resource.shortname" size=40/>
  	<@input name="url" keyBase="manage.resource.create." size=80/>
  
  <div class="buttons">
 	<@s.submit cssClass="button" name="create" key="button.create"/>
  </div>	
</@s.form>


<#include "/WEB-INF/pages/inc/footer.ftl"/>
