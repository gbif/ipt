<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="title"/></title>
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();
	});
</script>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<div class="grid_18 suffix_6">
<table id="admintable">
	<@s.form cssClass="topForm" action="publishAll.do" method="post" namespace="" includeContext="false">
	<tr>
	  <td>
		<a href="${baseURL}/admin/config.do"><span class="fa-stack fa-2x"><i class="fa fa-circle fa-stack-2x icon-background"></i><i class="fa fa-cogs fa-stack-1x icon-foreground"></i></span></a>
	  </td>
	  <td>
	  <@s.text name="admin.home.editConfig"/>
	  <img class="infoImg" src="${baseURL}/images/info.gif" />
	  <div class="info"><@s.text name="admin.home.publishResources.help"/></div>
	  <@s.submit cssClass="button" name="publishAll" key="admin.home.publishResources"/>
	  </td>
	</tr>
	  </@s.form>
	<tr>
	  <td>
		<a href="${baseURL}/admin/users.do"><span class="fa-stack fa-2x"><i class="fa fa-circle fa-stack-2x icon-background"></i><i class="fa fa-user fa-stack-1x icon-foreground"></i></span></a>
	  </td>
	  <td colspan="2">
		<@s.text name="admin.home.manageUsers"/>
	  </td>
	</tr>
	<tr>
	  <td>
		<a href="${baseURL}/admin/registration.do"><span class="fa-stack fa-2x"><i class="fa fa-circle fa-stack-2x icon-background"></i><i class="fa fa-cloud-upload fa-stack-1x icon-foreground"></i></span></a>
	  </td>
	  <td colspan="2">
		<@s.text name="admin.home.editRegistration"/>
	  </td>
	</tr>
	<tr>
	  <td>
		  <#if registeredIpt?has_content>
				<a href="${baseURL}/admin/organisations.do"><span class="fa-stack fa-2x"><i class="fa fa-circle fa-stack-2x icon-background"></i><i class="fa fa-university fa-stack-1x icon-foreground"></i></span>></a>
		  <#else>
				<span class="fa-stack fa-2x"><i class="fa fa-circle fa-stack-2x icon-background-grey"></i><i class="fa fa-university fa-stack-1x icon-foreground-grey"></i></span>
		  </#if>
	  </td>
	  <td colspan="2">
		<@s.text name="admin.home.editOrganisations"/>
		<#if !registeredIpt?has_content><div id="un-registered"><@s.text name="admin.home.editOrganisations.disabled"/></div></#if>
	  </td>
	</tr>

	<tr>
	  <td>
		<a href="${baseURL}/admin/extensions.do"><span class="fa-stack fa-2x"><i class="fa fa-circle fa-stack-2x icon-background"></i><i class="fa fa-cubes fa-stack-1x icon-foreground"></i></span></a>
	  </td>
	  <td colspan="2">
		<@s.text name="admin.home.manageExtensions"/>
	  </td>
	</tr>
	<tr>
	  <td>
		<a href="${baseURL}/admin/logs.do"><span class="fa-stack fa-2x"><i class="fa fa-circle fa-stack-2x icon-background"></i><i class="fa fa-search fa-stack-1x icon-foreground"></i></span></a>
	  </td>
	  <td colspan="2">
		<@s.text name="admin.home.manageLogs"/>
	  </td>
	</tr>
</table>
</div>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">
