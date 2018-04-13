<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="admin.home.manageUsers"/></title>
 <script type="text/javascript">

$(document).ready(function(){
	//Hack needed for Internet Explorer
	$('#create').click(function() {
		window.location='user.do';
	});	
	$('#cancel').click(function() {
		window.location='home.do';
	});	
});
</script>	
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/usersTable.ftl"/>

<h1 class="rtableTitle"><@s.text name="admin.home.manageUsers"/></h1>

<@usersTable numUsersShown=20 sEmptyTable="dataTables.sEmptyTable.users" columnToSortOn=0 sortOrder="asc" />
<div id="tableContainer"></div>

<div class="grid_24">
<p>
	<button id="create"><@s.text name="button.create"/></button>
	<button id="cancel"><@s.text name="button.cancel"/></button>
</p>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">

<!-- jQuery v1.11.1 -->
<script type="text/javascript" src="${baseURL}/js/jquery/jquery-3.2.1.min.js"></script>
<!-- DataTables v1.9.4 -->
<script type="text/javascript" language="javascript" src="${baseURL}/js/jquery/jquery.dataTables.js"></script>

</#escape>