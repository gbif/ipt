<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="title"/></title>
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();
	});
</script>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/popover.ftl"/>

<main class="container">
	<div class="my-3 p-3 bg-body rounded shadow-sm admin-item">
		<table id="admintable">
			<tr>
				<td>
					<a href="${baseURL}/admin/config.do">
						<span class="fa-stack fa-2x text-gbif-primary">
							<i class="fa fa-cogs fa-stack-1x"></i>
						</span>
					</a>
				</td>
				<td>
					<h6 class="text-gbif-header">
						<@s.text name="admin.home.editConfig"/>
					</h6>
				</td>
			</tr>
		</table>
	</div>

	<div class="my-3 p-3 bg-body rounded shadow-sm admin-item">
		<table id="admintable">
			<tr>
				<td>
					<a href="${baseURL}/admin/bulk-publication.do">
						<span class="fa-stack fa-2x text-gbif-primary">
							<i class="fa fa-share-square-o fa-stack-1x"></i>
						</span>
					</a>
				</td>
				<td colspan="2">
					<h6 class="text-gbif-header">
						<@s.text name="admin.home.bulkPublication"/>
					</h6>
				</td>
			</tr>
		</table>
	</div>

	<div class="my-3 p-3 bg-body rounded shadow-sm admin-item">
		<table id="admintable">
			<tr>
				<td>
					<a href="${baseURL}/admin/users.do">
						<span class="fa-stack fa-2x text-gbif-primary">
							<i class="fa fa-users fa-stack-1x"></i>
						</span>
					</a>
				</td>
				<td colspan="2">
					<h6 class="text-gbif-header"><@s.text name="admin.home.manageUsers"/></h6>
				</td>
			</tr>
		</table>
	</div>

	<div class="my-3 p-3 bg-body rounded shadow-sm admin-item">
		<table id="admintable">
			<tr>
				<td>
					<a href="${baseURL}/admin/registration.do">
						<span class="fa-stack fa-2x">
							<i class="fa fa-cloud-upload fa-stack-1x text-gbif-primary"></i>
						</span>
					</a>
				</td>
				<td colspan="2">
					<h6 class="text-gbif-header">
						<@s.text name="admin.home.editRegistration"/>
					</h6>
				</td>
			</tr>
		</table>
	</div>

	<div class="my-3 p-3 bg-body rounded shadow-sm <#if registeredIpt?has_content>admin-item</#if>">
		<table id="admintable">
			<tr>
				<td>
					<#if registeredIpt?has_content>
						<a href="${baseURL}/admin/organisations.do">
							<span class="fa-stack fa-2x">
								<i class="fa fa-university fa-stack-1x text-gbif-primary"></i>
							</span>
						</a>
					<#else>
						<span class="fa-stack fa-2x">
							<i class="fa fa-university fa-stack-1x"></i>
						</span>
					</#if>
				</td>
				<td colspan="2">
					<h6 class="text-gbif-header">
						<@s.text name="admin.home.editOrganisations"/>
					</h6>
					<#if !registeredIpt?has_content>
						<small class="pt-0 mt-0">
							<@s.text name="admin.home.editOrganisations.disabled"/>
						</small>
					</#if>
				</td>
			</tr>
		</table>
	</div>

	<div class="my-3 p-3 bg-body rounded shadow-sm admin-item">
		<table id="admintable">
			<tr>
				<td>
					<a href="${baseURL}/admin/extensions.do">
						<span class="fa-stack fa-2x">
							<i class="fa fa-cubes fa-stack-1x text-gbif-primary"></i>
						</span>
					</a>
				</td>
				<td colspan="2">
					<h6 class="text-gbif-header">
						<@s.text name="admin.home.manageExtensions"/>
					</h6>
				</td>
			</tr>
		</table>
	</div>

	<div class="my-3 p-3 bg-body rounded shadow-sm admin-item">
		<table id="admintable">
			<tr>
				<td>
					<a href="${baseURL}/admin/logs.do">
						<span class="fa-stack fa-2x">
							<i class="fa fa-search fa-stack-1x text-gbif-primary"></i>
						</span>
					</a>
				</td>
				<td colspan="2">
					<h6 class="text-gbif-header">
						<@s.text name="admin.home.manageLogs"/>
					</h6>
				</td>
			</tr>
		</table>
	</div>
</main>


<#include "/WEB-INF/pages/inc/footer.ftl">
