<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<script type="text/javascript">
$(document).ready(function(){
	$('#organisation\\.key').click(function() {
	$('#organisation\\.name').val($('#organisation\\.key :selected').text());	
})
});
</script>	
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.registration.title"/></h1>

<#include "/WEB-INF/pages/macros/forms.ftl"> 
<@s.form id="organisationsForm" cssClass="ftlTopForm" action="organisation.do" method="post">
	<@s.hidden id="organisation.name" name="organisation.name" value="b" required="true"/>
	<@selectList name="organisation.key" options="organisations" objValue="key" objTitle="name" keyBase="admin.registration." value="" size=15/>  
	<@input name="organisation.password" keyBase="admin.registration." type="text"/>
   <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
  </div>	  
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
