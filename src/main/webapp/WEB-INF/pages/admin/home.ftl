<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="title"/></title>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/popover.ftl"/>

<div class="container-fluid bg-body">
	<div class="container my-3">
		<#include "/WEB-INF/pages/inc/action_alerts.ftl">
	</div>
</div>

<main class="container">
	<div class="row mt-xl-5">
		<div class="col-sm-12 p-0 border-xl shadow-sm">
			<div class="card admin-card">
				<div class="card-body m-0 p-0">
					<div class="row gx-0 text-center admin-col-listing">
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/config.do" title="" class="admin-col-listing-item border-xl-right border-bottom">
								<div class="admin-icon-wrapper">
									<i class="bi bi-gear-fill admin-icon"></i>
								</div>
								<h5 class="admin-card-title fw-400">
									<@s.text name="admin.home.editConfig"/>
								</h5>
							</a>
						</div>
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/bulk-publication.do" title="" class="admin-col-listing-item border-xl-right border-bottom">
								<div class="admin-icon-wrapper">
									<i class="bi bi-stack admin-icon"></i>
								</div>
								<h5 class="admin-card-title fw-400">
									<@s.text name="admin.home.bulkPublication"/>
								</h5>
							</a>
						</div>
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/users.do" title="" class="admin-col-listing-item border-xl-right border-bottom">
								<div class="admin-icon-wrapper">
									<i class="bi bi-people-fill admin-icon"></i>
								</div>
								<h5 class="admin-card-title fw-400">
									<@s.text name="admin.home.manageUsers"/>
								</h5>
							</a>
						</div>
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/registration.do" title="" class="admin-col-listing-item border-bottom">
								<div class="admin-icon-wrapper">
									<i class="bi bi-cloud-arrow-up-fill admin-icon"></i>
								</div>
								<h5 class="admin-card-title fw-400">
									<@s.text name="admin.home.editRegistration"/>
								</h5>
							</a>
						</div>
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/organisations.do" title="" class="admin-col-listing-item border-xl-right border-xl-max-bottom">
								<div class="admin-icon-wrapper">
									<i class="bi bi-building admin-icon"></i>
								</div>
								<h5 class="admin-card-title fw-400">
									<@s.text name="admin.home.editOrganisations"/>
								</h5>
							</a>
						</div>
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/extensions.do" title="" class="admin-col-listing-item border-xl-right border-xl-max-bottom">
								<div class="admin-icon-wrapper">
									<i class="bi bi-collection-fill admin-icon"></i>
								</div>
								<h5 class="admin-card-title fw-400">
									<@s.text name="admin.home.manageExtensions"/>
								</h5>
							</a>
						</div>
						<div class="col-xl-3 col-12">
							<a href="${baseURL}/admin/logs.do" title="" class="admin-col-listing-item border-xl-right border-xl-max-bottom">
								<div class="admin-icon-wrapper">
									<i class="bi bi-journal-text admin-icon"></i>
								</div>
								<h5 class="admin-card-title fw-400">
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
