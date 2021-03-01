<#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
 <title><@s.text name="title"/></title>
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();
	});
</script>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">

<main class="container pt-5">
	<div class="my-3 p-3 bg-body rounded shadow-sm">
		<table id="admintable">
			<@s.form cssClass="topForm" action="publishAll.do" method="post" namespace="" includeContext="false">
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
							<span tabindex="0" data-bs-container="body" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-placement="top" data-bs-html="true" data-bs-content="<@s.text name='admin.home.publishResources.help'/>">
								<svg xmlns="http://www.w3.org/2000/svg"
									 width="14" height="14"
									 fill="#198754" class="bi bi-info-circle" viewBox="0 0 16 16">
									<path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
									<path d="M8.93 6.588l-2.29.287-.082.38.45.083c.294.07.352.176.288.469l-.738 3.468c-.194.897.105 1.319.808 1.319.545 0 1.178-.252 1.465-.598l.088-.416c-.2.176-.492.246-.686.246-.275 0-.375-.193-.304-.533L8.93 6.588zM9 4.5a1 1 0 1 1-2 0 1 1 0 0 1 2 0z"/>
								</svg>
    						</span>
						</h6>
						<@s.submit cssClass="btn btn-sm btn-outline-success" name="publishAll" key="admin.home.publishResources"/>
					</td>
				</tr>
			</@s.form>
		</table>
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
