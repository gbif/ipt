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
  <div>
  	<@input name="resource.shortname" size=40/>
  	<@select name="type" options={"blank":"resource.source.blank", "dwca":"resource.source.dwca", "metadata":"resource.source.metadata"} value="blank" />
  </div>
  <#-- different divs to select between -->
  <div id="blank">
  	<h2>New Blank Resource</h2>
  	<@input name="resource.title" size=80/>
  	<@select name="resource.type" options={"occ":"resource.type.occ", "tax":"resource.type.tax"} value="occ" />
  	<@text name="resource.description" size=80/>
  </div>
  <div id="metadata">
  	<h2>Import Existing Metadata Document</h2>
  	<@input name="url" keyBase="manage.resource.create." size=80/>
  </div>
  <div id="dwca">
  	<h2>Upload Existing Darwin Core Archive</h2>
  	<@input name="url2" keyBase="manage.resource.create." size=80/>
	<@checkbox name="lockMetadata" keyBase="manage.resource.create." />  
  </div>
  
  <div class="buttons">
 	<@s.submit cssClass="button" name="create" key="button.create"/>
  </div>	
</@s.form>


<#include "/WEB-INF/pages/inc/footer.ftl"/>
