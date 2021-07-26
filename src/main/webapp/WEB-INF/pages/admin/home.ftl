<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="title"/></title>
<script>
	$(document).ready(function(){
		initHelp();
	});
</script>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/popover.ftl"/>

<main class="container">
	<div class="row my-2 p-3">
		<div class="col-sm-12 mb-3">
			<div class="card admin-card rounded shadow-sm">
				<h5 class="border-bottom pb-2 mb-0 mx-4 pt-4 text-gbif-header text-center">
					<@s.text name="menu.admin"/>
				</h5>

				<div class="card-body my-lg-3 mt-0 mb-3">
					<div class="row gx-0 text-center admin-col-listing admin-col-listing-hover">
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/config.do" title="" class="admin-col-listing-item border-xl-right border-bottom">
								<i class="bi bi-gear-fill admin-icon"></i>
								<h5 class="admin-card-title">
									<@s.text name="admin.home.editConfig"/>
								</h5>
							</a>
						</div>
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/bulk-publication.do" title="" class="admin-col-listing-item border-xl-right border-bottom">
								<i class="bi bi-stack admin-icon"></i>
								<h5 class="admin-card-title">
									<@s.text name="admin.home.bulkPublication"/>
								</h5>
							</a>
						</div>
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/users.do" title="" class="admin-col-listing-item border-xl-right border-bottom">
								<i class="bi bi-people-fill admin-icon"></i>
								<h5 class="admin-card-title">
									<@s.text name="admin.home.manageUsers"/>
								</h5>
							</a>
						</div>
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/registration.do" title="" class="admin-col-listing-item border-bottom">
								<i class="bi bi-cloud-arrow-up-fill admin-icon"></i>
								<h5 class="admin-card-title">
									<@s.text name="admin.home.editRegistration"/>
								</h5>
							</a>
						</div>
						<div class="col-xl-3 col-12">
							<#if !registeredIpt?has_content>
								<div href="javascript:void(0);" title="" class="admin-col-listing-item border-xl-right border-xl-max-bottom text-gbif-header admin-col-listing-item-disabled">
									<i class="bi bi-building admin-icon text-gbif-header"></i>
									<h5 class="admin-card-title">
										<a tabindex="0" role="button"
										   class="popover-link"
										   data-bs-toggle="popover"
										   data-bs-trigger="focus"
										   data-bs-html="true"
										   data-bs-content="<@s.text name="admin.home.editOrganisations.disabled" escapeHtml=true/>">
											<i class="bi bi-exclamation-triangle-fill text-secondary"></i>
										</a>
										<@s.text name="admin.home.editOrganisations"/>
									</h5>
								</div>
							<#else>
								<a href="${baseURL}/admin/organisations.do" title="" class="admin-col-listing-item border-xl-right border-xl-max-bottom">
									<i class="bi bi-building admin-icon"></i>
									<h5 class="admin-card-title">
										<@s.text name="admin.home.editOrganisations"/>
									</h5>
								</a>
							</#if>
						</div>
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/extensions.do" title="" class="admin-col-listing-item border-xl-right border-xl-max-bottom">
								<i class="bi bi-collection-fill admin-icon"></i>
								<h5 class="admin-card-title">
									<@s.text name="admin.home.manageExtensions"/>
								</h5>
							</a>
						</div>
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/logs.do" title="" class="admin-col-listing-item border-xl-right">
								<i class="bi bi-journal-text admin-icon"></i>
								<h5 class="admin-card-title">
									<@s.text name="admin.home.manageLogs"/>
								</h5>
							</a>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
