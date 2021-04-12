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
<#include "/WEB-INF/pages/macros/organisationsTable.ftl"/>

<h1 class="rtableTitle"><@s.text name="admin.home.editOrganisations"/></h1>

<@organisationsTable numOrganisationsShown=20 sEmptyTable="dataTables.sEmptyTable.organisations" columnToSortOn=0 sortOrder="asc" />
<div id="tableContainer"></div>

<div class="grid_24">
<p>
	<button id="add"><@s.text name="button.add"/></button>
	<button id="cancel"><@s.text name="button.cancel"/></button>
</p>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">

<!-- jQuery v1.11.1 -->
<script type="text/javascript" src="${baseURL}/js/jquery/jquery-3.2.1.min.js"></script>
<!-- DataTables v1.9.4 -->
<script type="text/javascript" language="javascript" src="${baseURL}/js/jquery/jquery.dataTables.js"></script>

</#escape>