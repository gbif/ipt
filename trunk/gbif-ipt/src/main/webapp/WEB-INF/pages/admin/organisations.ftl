<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<script type="text/javascript">

$(document).ready(function(){
	$('#organisation\\.key').click(function() {
		$('#organisation\\.name').val($('#organisation\\.key :selected').text());	
	});
	//Hack needed for Internet Explorer X.*x
	$('#create').click(function() {
		window.location='organisation.do';
	});
	$('#cancel').click(function() {
		window.location='organisations.do?cancel=true';
	});	
	$('.edit').each(function() {
		$(this).click(function() {
			window.location = $(this).parent('a').attr('href');
		});
	});		
});
</script>	
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.organisation.title"/></h1>

<#include "/WEB-INF/pages/macros/forms.ftl"> 

<table class="simple" width="100%">
	<tr>
		<th><@s.text name="admin.organisation.name"/></th>
		<th><@s.text name="admin.organisation.alias"/></th>
		<th><@s.text name="admin.organisation.canHost"/></th>
		<th></th>		
	</tr>
	
	<#list linkedOrganisations as o>	
	<tr>
		<td>${o.name!}</td>
		<td>${o.alias!}</td>
		<td>
		<#if o.canHost>
			<@s.checkbox name="organisation.canHost" key="organisation.canHost" disabled="true" value="true" />
		<#else>
			<@s.checkbox name="organisation.canHost" key="organisation.canHost" disabled="true" value="false" />
		</#if>
		</td>
		<td><a id="editLink_${o.key}" href="organisation?id=${o.key}"><button class="edit" id="${o.key}"><@s.text name="button.edit"/></button></a></td>
	</tr>
	</#list>
</table>


<p>
	<button id="create"><@s.text name="button.create"/></button>
	<button id="cancel"><@s.text name="button.cancel"/></button>
</p>

<#include "/WEB-INF/pages/inc/footer.ftl">
