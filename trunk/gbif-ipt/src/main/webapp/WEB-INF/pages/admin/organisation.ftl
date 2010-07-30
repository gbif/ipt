<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<script type="text/javascript">

$(document).ready(function(){
	$('#organisation\\.key').click(function() {
	$('#organisation\\.name').val($('#organisation\\.key :selected').text());	
	$('#organisation\\.alias').val($('#organisation\\.key :selected').text());	
})
});
</script>	
<#include "/WEB-INF/pages/inc/menu.ftl">
<h1><@s.text name="admin.organisation.title"/></h1>
<#include "/WEB-INF/pages/macros/forms.ftl"> 



<@s.form id="organisationsForm" cssClass="ftlTopForm" action="organisation.do" method="post">
	
	<#if id?has_content>
		<@input name="organisation.name" keyBase="admin." type="text" disabled=true/>
		<@s.hidden name="organisation.key" id="organisation.key" required="true" />
		<@s.hidden name="id" id="id" required="true" />
	<#else>
		<@s.hidden id="organisation.name" name="organisation.name" required="true" />
		<@selectList name="organisation.key" options="organisations" objValue="key" objTitle="name" keyBase="admin." size=15 />
	</#if>		  
	<@input name="organisation.password" keyBase="admin." type="text"/>
	<@input name="organisation.alias" keyBase="admin." type="text"/>
	<@checkbox name="organisation.canHost" keyBase="admin."/>
   <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
  </div>
</@s.form>
<#include "/WEB-INF/pages/inc/footer.ftl">
