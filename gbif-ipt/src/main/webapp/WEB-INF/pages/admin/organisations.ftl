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

<h1><@s.text name="admin.organisation.title"/></h1>

<#include "/WEB-INF/pages/macros/forms.ftl"> 

<table class="simple" width="100%">
	<tr>
		<th>Title</th>
		<th>Description</th>
		<th>Alias</th>
		<th>Can host?</th>
		<th></th>		
	</tr>

	<#list linkedOrganisations as o>	
	<tr>
		<td>${o.name!}</td>
		<td>${o.description!}</td>
		<td>${o.alias!}</td>
		<td>
		<#if o.canHost>
			<@s.checkbox name="organisation.canHost" key="organisation.canHost" disabled="true" value="true" />
		<#else>
			<@s.checkbox name="organisation.canHost" key="organisation.canHost" disabled="true" value="false" />
		</#if>
		</td>
		<#-- <@checkbox name="organisation.canHost" disabled=true checked=o.canHost/></td> -->
		<td><a href="organisation?id=${o.key}">Edit</a></td>
	</tr>
	</#list>
</table>

<p>
	<a class="button" href="organisation.do">Associate new organisation</a>
</p>

<#include "/WEB-INF/pages/inc/footer.ftl">
