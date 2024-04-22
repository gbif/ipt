<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="title"/></title>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/popover.ftl"/>

<div class="container-fluid bg-body">
	<div class="container px-0">
		<#include "/WEB-INF/pages/inc/action_alerts.ftl">
	</div>
</div>

<div class="container-fluid bg-body border-bottom">
	<div class="container bg-body mb-4 px-3">
		<div class="container border rounded-2 p-4">
			<div class="text-center fs-smaller">
				<@s.text name="breadcrumb.admin"/>
			</div>

			<div class="text-center">
				<h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
					<@s.text name="admin.home.title"/>
				</h1>
			</div>
		</div>
	</div>
</div>

<main class="container mt-4">
	<div class="flex-auto">
		<div class="d-flex flex-items-stretch flex-wrap">
			<div class="d-flex flex-column col-lg-3 col-md-4 col-sm-6 col-12 px-2">
				<div class="border rounded-2 d-flex flex-column overflow-hidden w-100 flex-auto mb-3">
					<div class="d-flex flex-justify-between px-4 pt-4 pb-0">
						<div>
							<h4 class="d-flex fs-regular mt-1">
								<span class="text-gbif-primary me-2">
									<i class="bi bi-gear-fill admin-icon"></i>
								</span>
								<span>
									<@s.text name="admin.home.editConfig"/>
								</span>
							</h4>
						</div>
					</div>
					<div class="d-flex flex-column flex-auto flex-justify-between">
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-0 px-4 fs-smaller">
							<@s.text name="admin.home.editConfig.description"/>
						</div>
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-4 px-4">
							<a href="${baseURL}/admin/config.do" title="" class="action-link-button action-link-button-primary">
								<@s.text name="button.view"/>
							</a>
						</div>
					</div>
				</div>
			</div>

			<div class=" d-flex flex-column col-lg-3 col-md-4 col-sm-6 col-12 px-2">
				<div class="border rounded-2 d-flex flex-column overflow-hidden w-100 flex-auto mb-3">
					<div class="d-flex flex-justify-between px-4 pt-4 pb-0">
						<div>
							<h4 class="d-flex fs-regular mt-1">
								<span class="text-gbif-primary me-2">
									<i class="bi bi-stack admin-icon"></i>
								</span>
								<span>
									<@s.text name="admin.home.bulkPublication"/>
								</span>
							</h4>
						</div>
					</div>
					<div class="d-flex flex-column flex-auto flex-justify-between">
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-0 px-4 fs-smaller">
							<@s.text name="admin.home.bulkPublication.description"/>
						</div>
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-4 px-4">
							<a href="${baseURL}/admin/bulk-publication.do" title="" class="action-link-button action-link-button-primary">
								<@s.text name="button.view"/>
							</a>
						</div>
					</div>
				</div>
			</div>

			<div class="d-flex flex-column col-lg-3 col-md-4 col-sm-6 col-12 px-2">
				<div class="border rounded-2 d-flex flex-column overflow-hidden w-100 flex-auto mb-3">
					<div class="d-flex flex-justify-between px-4 pt-4 pb-0">
						<div>
							<h4 class="d-flex fs-regular mt-1">
								<span class="text-gbif-primary me-2">
									<i class="bi bi-people-fill admin-icon"></i>
								</span>
								<span>
									<@s.text name="admin.home.manageUsers"/>
								</span>
							</h4>
						</div>
					</div>
					<div class="d-flex flex-column flex-auto flex-justify-between">
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-0 px-4 fs-smaller">
							<@s.text name="admin.home.manageUsers.description"/>
						</div>
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-4 px-4">
							<a href="${baseURL}/admin/users.do" title="" class="action-link-button action-link-button-primary">
								<@s.text name="button.view"/>
							</a>
						</div>
					</div>
				</div>
			</div>

			<div class="d-flex flex-column col-lg-3 col-md-4 col-sm-6 col-12 px-2">
				<div class="border rounded-2 d-flex flex-column overflow-hidden w-100 flex-auto mb-3">
					<div class="d-flex flex-justify-between px-4 pt-4 pb-0">
						<div>
							<h4 class="d-flex fs-regular mt-1">
								<span class="text-gbif-primary me-2">
									<i class="bi bi-cloud-arrow-up-fill admin-icon"></i>
								</span>
								<span>
									<@s.text name="admin.home.editRegistration"/>
								</span>
							</h4>
						</div>
					</div>
					<div class="d-flex flex-column flex-auto flex-justify-between">
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-0 px-4 fs-smaller">
							<@s.text name="admin.home.editRegistration.description"/>
						</div>
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-4 px-4">
							<a href="${baseURL}/admin/registration.do" title="" class="action-link-button action-link-button-primary">
								<@s.text name="button.view"/>
							</a>
						</div>
					</div>
				</div>
			</div>

			<div class="d-flex flex-column col-lg-3 col-md-4 col-sm-6 col-12 px-2">
				<div class="border rounded-2 d-flex flex-column overflow-hidden w-100 flex-auto mb-3">
					<div class="d-flex flex-justify-between px-4 pt-4 pb-0">
						<div>
							<h4 class="d-flex fs-regular mt-1">
								<span class="text-gbif-primary me-2">
									<i class="bi bi-building admin-icon"></i>
								</span>
								<span>
									<@s.text name="admin.home.organisations"/>
								</span>
							</h4>
						</div>
					</div>
					<div class="d-flex flex-column flex-auto flex-justify-between">
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-0 px-4 fs-smaller">
							<@s.text name="admin.home.organisations.description"/>
						</div>
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-4 px-4">
							<a href="${baseURL}/admin/organisations.do" title="" class="action-link-button action-link-button-primary">
								<@s.text name="button.view"/>
							</a>
						</div>
					</div>
				</div>
			</div>

			<div class="d-flex flex-column col-lg-3 col-md-4 col-sm-6 col-12 px-2">
				<div class="border rounded-2 d-flex flex-column overflow-hidden w-100 flex-auto mb-3">
					<div class="d-flex flex-justify-between px-4 pt-4 pb-0">
						<div>
							<h4 class="d-flex fs-regular mt-1">
								<span class="text-gbif-primary me-2">
									<i class="bi bi-collection-fill admin-icon"></i>
								</span>
								<span>
									<@s.text name="admin.home.manageExtensions"/>
								</span>
							</h4>
						</div>
					</div>
					<div class="d-flex flex-column flex-auto flex-justify-between">
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-0 px-4 fs-smaller">
							<@s.text name="admin.home.manageExtensions.description"/>
						</div>
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-4 px-4">
							<a href="${baseURL}/admin/extensions.do" title="" class="action-link-button action-link-button-primary">
								<@s.text name="button.view"/>
							</a>
						</div>
					</div>
				</div>
			</div>

			<div class="d-flex flex-column col-lg-3 col-md-4 col-sm-6 col-12 px-2">
				<div class="border rounded-2 d-flex flex-column overflow-hidden w-100 flex-auto mb-3">
					<div class="d-flex flex-justify-between px-4 pt-4 pb-0">
						<div>
							<h4 class="d-flex fs-regular mt-1">
								<span class="text-gbif-primary me-2">
									<i class="bi bi-columns-gap admin-icon"></i>
								</span>
								<span>
									<@s.text name="admin.home.manageDataPackageSchemas"/> <span class="badge rounded-pill fs-smaller-2 fw-400 bg-gbif-primary"><@s.text name="admin.home.new"/></span>
								</span>
							</h4>
						</div>
					</div>
					<div class="d-flex flex-column flex-auto flex-justify-between">
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-0 px-4 fs-smaller">
							<@s.text name="admin.home.manageDataPackageSchemas.description"/>
						</div>
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-4 px-4">
							<a href="${baseURL}/admin/dataPackages.do" title="" class="action-link-button action-link-button-primary">
								<@s.text name="button.view"/>
							</a>
						</div>
					</div>
				</div>
			</div>

			<div class="d-flex flex-column col-lg-3 col-md-4 col-sm-6 col-12 px-2">
				<div class="border rounded-2 d-flex flex-column overflow-hidden w-100 flex-auto mb-3">
					<div class="d-flex flex-justify-between px-4 pt-4 pb-0">
						<div>
							<h4 class="d-flex fs-regular mt-1">
								<span class="text-gbif-primary me-2">
									<i class="bi bi-tv admin-icon"></i>
								</span>
								<span>
									<@s.text name="admin.home.manageUI"/>
								</span>
							</h4>
						</div>
					</div>
					<div class="d-flex flex-column flex-auto flex-justify-between">
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-0 px-4 fs-smaller">
							<@s.text name="admin.home.manageUI.description"/>
						</div>
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-4 px-4">
							<a href="${baseURL}/admin/uiManagement.do" title="" class="action-link-button action-link-button-primary">
								<@s.text name="button.view"/>
							</a>
						</div>
					</div>
				</div>
			</div>

			<div class="d-flex flex-column col-lg-3 col-md-4 col-sm-6 col-12 px-2">
				<div class="border rounded-2 d-flex flex-column overflow-hidden w-100 flex-auto mb-3">
					<div class="d-flex flex-justify-between px-4 pt-4 pb-0">
						<div>
							<h4 class="d-flex fs-regular mt-1">
								<span class="text-gbif-primary me-2">
									<i class="bi bi-journal-text admin-icon"></i>
								</span>
									<span>
									<@s.text name="admin.home.manageLogs"/>
								</span>
							</h4>
						</div>
					</div>
					<div class="d-flex flex-column flex-auto flex-justify-between">
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-0 px-4 fs-smaller">
							<@s.text name="admin.home.manageLogs.description"/>
						</div>
						<div class="d-flex flex-justify-between flex-items-center pt-2 pb-4 px-4">
							<a href="${baseURL}/admin/logs.do" title="" class="action-link-button action-link-button-primary">
								<@s.text name="button.view"/>
							</a>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
