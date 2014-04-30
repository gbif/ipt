<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<script type="text/javascript">

$(document).ready(function(){
	$('#organisation\\.key').click(function() {
		$('#organisation\\.name').val($('#organisation\\.key :selected').text());	
	});
	//Hack needed for Internet Explorer X.*x
	$('#add').click(function() {
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
<#include "/WEB-INF/pages/macros/forms.ftl"> 

<div class="grid_24">
<h1><@s.text name="admin.home.editOrganisations"/></h1>

<table id="user-list" class="simple" width="100%">
    <thead>
	<tr>
		<th><@s.text name="admin.organisation.name"/></th>
		<th><@s.text name="admin.organisation.alias"/></th>
		<th><@s.text name="admin.organisation.canPublish"/></th>
		<th></th>		
	</tr>
	</thead>
	<tbody>
	<#-- for counting even or odd rows -->
    <#function zebra index>
      <#if (index % 2) == 0>
        <#return "even" />
      <#else>
        <#return "odd" />
      </#if>
    </#function>

	<#list linkedOrganisations as o>	
	<tr class="${zebra(o_index)}">
		<td><a id="editLink_${o.key}" href="organisation?id=${o.key}">${o.name!"???"}</a></td>
		<td>${o.alias!}</td>
		<td>
		<@s.checkbox name="organisation.canHost" key="organisation.canHost" disabled="true" value="${o.canHost?string}" />
		</td>
	</tr>
	</#list>
	</tbody>
</table>


<p>
	<button id="add"><@s.text name="button.add"/></button>
	<button id="cancel"><@s.text name="button.cancel"/></button>
</p>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>