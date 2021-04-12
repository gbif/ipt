<#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
 <title><@s.text name="title"/></title>
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();
	});
</script>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
<#include "/WEB-INF/pages/macros/popover-bootstrap.ftl"/>

<main class="container">
	<div class="my-3 p-3 bg-body rounded shadow-sm">
		<@s.form cssClass="topForm" action="publishAll.do" method="post" namespace="" includeContext="false">
			<table id="admintable">
				<tr>
					<td>
						<a href="${baseURL}/admin/config.do">
							<span class="fa-stack fa-2x">
								<i class="fa fa-cogs fa-stack-1x" style="color: #008959"></i>
							</span>
						</a>
					</td>
					<td>
						<h6 class="text-success">
							<@s.text name="admin.home.editConfig"/>
							<@popoverPropertyInfo "admin.home.publishResources.help"/>
						</h6>
						<@s.submit cssClass="btn btn-sm btn-outline-success" name="publishAll" key="admin.home.publishResources"/>
					</td>
				</tr>
			</table>
		</@s.form>
	</div>

	<div class="my-3 p-3 bg-body rounded shadow-sm">
		<table id="admintable">
			<tr>
				<td>
					<a href="${baseURL}/admin/users.do">
						<span class="fa-stack fa-2x">
							<i class="fa fa-user fa-stack-1x" style="color: #008959"></i>
						</span>
					</a>
				</td>
				<td colspan="2">
					<h6 class="text-success"><@s.text name="admin.home.manageUsers"/></h6>
				</td>
			</tr>
		</table>
	</div>

	<div class="my-3 p-3 bg-body rounded shadow-sm">
		<table id="admintable">
			<tr>
				<td>
					<a href="${baseURL}/admin/registration.do">
						<span class="fa-stack fa-2x">
							<i class="fa fa-cloud-upload fa-stack-1x" style="color: #008959"></i>
						</span>
					</a>
				</td>
				<td colspan="2">
					<h6 class="text-success">
						<@s.text name="admin.home.editRegistration"/>
					</h6>
				</td>
			</tr>
		</table>
	</div>

	<div class="my-3 p-3 bg-body rounded shadow-sm">
		<table id="admintable">
			<tr>
				<td>
					<#if registeredIpt?has_content>
						<a href="${baseURL}/admin/organisations.do">
							<span class="fa-stack fa-2x">
								<i class="fa fa-university fa-stack-1x" style="color: #008959"></i>
							</span>
						</a>
					<#else>
						<span class="fa-stack fa-2x">
							<i class="fa fa-university fa-stack-1x" style="color: #7f7f7f"></i>
						</span>
					</#if>
				</td>
				<td colspan="2">
					<h6 class="text-success">
						<@s.text name="admin.home.editOrganisations"/>
					</h6>
					<#if !registeredIpt?has_content>
						<small class="text-muted pt-0 mt-0">
							<@s.text name="admin.home.editOrganisations.disabled"/>
						</small>
					</#if>
				</td>
			</tr>
		</table>
	</div>

	<div class="my-3 p-3 bg-body rounded shadow-sm">
		<table id="admintable">
			<tr>
				<td>
					<a href="${baseURL}/admin/extensions.do">
						<span class="fa-stack fa-2x">
							<i class="fa fa-cubes fa-stack-1x" style="color: #008959"></i>
						</span>
					</a>
				</td>
				<td colspan="2">
					<h6 class="text-success">
						<@s.text name="admin.home.manageExtensions"/>
					</h6>
				</td>
			</tr>
		</table>
	</div>

	<div class="my-3 p-3 bg-body rounded shadow-sm">
		<table id="admintable">
			<tr>
				<td>
					<a href="${baseURL}/admin/logs.do">
						<span class="fa-stack fa-2x">
							<i class="fa fa-search fa-stack-1x" style="color: #008959"></i>
						</span>
					</a>
				</td>
				<td colspan="2">
					<h6 class="text-success">
						<@s.text name="admin.home.manageLogs"/>
					</h6>
				</td>
			</tr>
		</table>
	</div>
</main>


<#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
