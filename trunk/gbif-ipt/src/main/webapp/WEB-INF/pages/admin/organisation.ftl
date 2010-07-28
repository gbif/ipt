<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<script type="text/javascript">
// Read a page's GET URL variables and return them as an associative array.
// taken from http://snipplr.com/users/Roshambo/
function getUrlVars()
{
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

$(document).ready(function(){
	var idVar = getUrlVars()["id"];
	if(idVar!=null) {
		$('#organisation\\.key').val(idVar);
	}
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
	<@s.hidden id="organisation.name" name="organisation.name" required="true" />
	<@selectList name="organisation.key" options="organisations" objValue="key" objTitle="name" keyBase="admin." size=15 disabled=id?has_content />  
	<@input name="organisation.password" keyBase="admin." type="text"/>
	<@input name="organisation.alias" keyBase="admin." type="text"/>
	<@checkbox name="organisation.canHost" keyBase="admin."/>
   <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
  </div>
</@s.form>
<#include "/WEB-INF/pages/inc/footer.ftl">
