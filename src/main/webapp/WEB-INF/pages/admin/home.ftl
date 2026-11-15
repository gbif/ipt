<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="title"/></title>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<link rel="stylesheet" href="${baseURL}/styles/admin-home.css">

<#-- One administration page; the whole card is the link. Nested content: the state line, if any.
     severity ("warning" or "danger"): for a card that is itself an alert; colours its icon, hover border and title. -->
<#macro card href iconName title description off=false severity="">
    <div class="ah-card<#if off> ah-card-off</#if><#if severity?has_content> ah-card-${severity}</#if>">
        <h3><@icon iconName/><a class="custom-link" href="${baseURL}${href}"><@s.text name=title/></a></h3>
        <p><@s.text name=description/></p>
        <#nested>
    </div>
</#macro>
<#-- counts as "Label: N", not "N resources": plural forms differ per language -->
<#macro loadProblems><#if failed><@s.text name="admin.home.state.failed"><@s.param>${failedResourcesCount}</@s.param></@s.text></#if><#if failed && (resourcesWithNoticesCount > 0)> · </#if><#if (resourcesWithNoticesCount > 0)><@s.text name="admin.home.state.notices"><@s.param>${resourcesWithNoticesCount}</@s.param></@s.text></#if></#macro>

<div class="container">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<#assign failed = (failedResourcesCount > 0)/>
<#assign hasLoadProblems = failed || (resourcesWithNoticesCount > 0)/>

<main class="container my-4">
    <h1 class="visually-hidden"><@s.text name="admin.home.title"/></h1>

    <#if hasLoadProblems>
        <#-- as the overview page's banners: red when resources failed to load, orange when they only loaded with problems -->
        <div class="ah-banner <#if failed>ah-banner-danger<#else>ah-banner-warning</#if>" role="status">
            <#if failed><@icon name="error" class="ah-banner-icon"/><#else><@icon name="warning" class="ah-banner-icon"/></#if>
            <div class="ah-banner-body">
                <h2><@s.text name="admin.failedResources.title"/></h2>
                <div class="ah-banner-text"><@loadProblems/></div>
            </div>
            <a href="${baseURL}/admin/failedResources.do" class="btn btn-sm btn-outline-secondary"><@s.text name="admin.failedResources.view"/></a>
        </div>
    </#if>

    <section class="ah-group" aria-labelledby="ah-configuration">
        <h2 id="ah-configuration"><@s.text name="admin.home.group.configuration"/></h2>
        <div class="ah-grid">
            <@card href="/admin/config.do" iconName="settings" title="admin.home.editConfig" description="admin.home.editConfig.description">
                <div class="ah-state"><#if testInstallation><b><@s.text name="admin.home.state.testMode"/></b><#else><@s.text name="admin.home.state.production"/></#if> · ${iptBaseUrl!}</div>
            </@card>
            <@card href="/admin/appearance.do" iconName="palette" title="admin.home.manageUI" description="admin.home.manageUI.description">
                <div class="ah-state"><#if colorSchemeReplaced><b><@s.text name="admin.home.state.coloursReplaced"/></b><#else><@s.text name="admin.home.state.colours"><@s.param><#if customColours><@s.text name="admin.home.state.custom"/><#else><@s.text name="admin.home.state.default"/></#if></@s.param></@s.text></#if> · <@s.text name="admin.home.state.logo"><@s.param><#if customLogo><@s.text name="admin.home.state.custom"/><#else><@s.text name="admin.home.state.default"/></#if></@s.param></@s.text></div>
            </@card>
            <@card href="/admin/logs.do" iconName="article" title="admin.home.manageLogs" description="admin.home.manageLogs.description"/>
        </div>
    </section>

    <section class="ah-group" aria-labelledby="ah-registration">
        <h2 id="ah-registration"><@s.text name="admin.home.group.registration"/></h2>
        <div class="ah-grid">
            <@card href="/admin/registration.do" iconName="cloud_upload" title="admin.home.editRegistration" description="admin.home.editRegistration.description">
                <#if hostingOrganisation??>
                    <div class="ah-state ah-state-ok"><@icon "check_circle"/><@s.text name="admin.home.state.registered"><@s.param>${hostingOrganisation.name!}</@s.param></@s.text></div>
                <#else>
                    <div class="ah-state ah-state-warning"><@icon "warning"/><@s.text name="admin.home.state.notRegistered"/></div>
                </#if>
            </@card>
            <@card href="/admin/organisations.do" iconName="business" title="admin.home.organisations" description="admin.home.organisations.description" off=!(hostingOrganisation??)>
                <div class="ah-state"><#if hostingOrganisation??><@s.text name="admin.home.state.organisations"><@s.param>${organisationsCount}</@s.param></@s.text><#else><@s.text name="admin.home.organisations.disabled"/></#if></div>
            </@card>
        </div>
    </section>

    <section class="ah-group" aria-labelledby="ah-publishing">
        <h2 id="ah-publishing"><@s.text name="admin.home.group.publishing"/></h2>
        <div class="ah-grid">
            <@card href="/admin/bulk-publication.do" iconName="layers" title="admin.home.bulkPublication" description="admin.home.bulkPublication.description"/>
            <@card href="/admin/autoPublishingSettings.do" iconName="update" title="admin.home.autoPublicationSettings" description="admin.home.autoPublicationSettings.description">
                <div class="ah-state"><#if !autoPublishingEnabled><@s.text name="admin.home.state.autoPublishing.off"/><#elseif autoPublishingAdminOnly><@s.text name="admin.home.state.autoPublishing.adminOnly"/><#else><@s.text name="admin.home.state.autoPublishing.on"/></#if></div>
            </@card>
            <@card href="/admin/scheduledPublications.do" iconName="event" title="admin.scheduledPublications.title" description="admin.scheduledPublications.intro">
                <div class="ah-state"><#if scheduledCount == 0><@s.text name="admin.home.state.scheduled.none"/><#else><@s.text name="admin.home.state.scheduled"><@s.param>${scheduledCount}</@s.param></@s.text><#if !autoPublishingEnabled> · <@s.text name="admin.home.state.scheduled.paused"/><#elseif nextScheduled??> · <@s.text name="admin.home.state.scheduled.next"><@s.param>${nextScheduled.title?has_content?then(nextScheduled.title, nextScheduled.shortname)}</@s.param><@s.param>${nextScheduled.nextPublished?datetime?string("yyyy-MM-dd HH:mm")}</@s.param></@s.text></#if></#if></div>
            </@card>
            <#-- only while there are any, with the banner's icon and colour -->
            <#if hasLoadProblems>
                <@card href="/admin/failedResources.do" iconName=failed?then("error", "warning") severity=failed?then("danger", "warning") title="admin.failedResources.title" description="admin.failedResources.intro">
                    <div class="ah-state <#if failed>ah-state-danger"><@icon "error"/><#else>ah-state-warning"><@icon "warning"/></#if><@loadProblems/></div>
                </@card>
            </#if>
        </div>
    </section>

    <section class="ah-group" aria-labelledby="ah-users">
        <h2 id="ah-users"><@s.text name="admin.home.group.users"/></h2>
        <div class="ah-grid">
            <@card href="/admin/users.do" iconName="people" title="admin.home.manageUsers" description="admin.home.manageUsers.description">
                <div class="ah-state"><@s.text name="admin.home.state.users"><@s.param>${usersCount}</@s.param></@s.text> · <@s.text name="admin.home.state.admins"><@s.param>${adminsCount}</@s.param></@s.text><#if (deactivatedUsersCount > 0)> · <@s.text name="admin.home.state.deactivated"><@s.param>${deactivatedUsersCount}</@s.param></@s.text></#if></div>
            </@card>
        </div>
    </section>

    <section class="ah-group" aria-labelledby="ah-formats">
        <h2 id="ah-formats"><@s.text name="admin.home.group.formats"/></h2>
        <div class="ah-grid">
            <@card href="/admin/extensions.do" iconName="extension" title="admin.home.manageExtensions" description="admin.home.manageExtensions.description">
                <#if (extensionUpdatesCount > 0)>
                    <div class="ah-state ah-state-warning"><@icon "warning"/><@s.text name="admin.home.state.updates"><@s.param>${extensionUpdatesCount}</@s.param></@s.text></div>
                <#elseif coreTypesCount + extensionsCount == 0>
                    <div class="ah-state"><@s.text name="admin.home.state.noneInstalled"/></div>
                <#else>
                    <div class="ah-state"><@s.text name="admin.home.state.coreTypes"><@s.param>${coreTypesCount}</@s.param></@s.text> · <@s.text name="admin.home.state.extensions"><@s.param>${extensionsCount}</@s.param></@s.text></div>
                </#if>
            </@card>
            <@card href="/admin/dataPackages.do" iconName="view_quilt" title="admin.home.manageDataPackageSchemas" description="admin.home.manageDataPackageSchemas.description">
                <#if (schemaUpdatesCount > 0)>
                    <div class="ah-state ah-state-warning"><@icon "warning"/><@s.text name="admin.home.state.updates"><@s.param>${schemaUpdatesCount}</@s.param></@s.text></div>
                <#elseif schemasCount == 0>
                    <div class="ah-state"><@s.text name="admin.home.state.noneInstalled"/></div>
                <#else>
                    <div class="ah-state"><@s.text name="admin.home.state.installed"><@s.param>${schemasCount}</@s.param></@s.text></div>
                </#if>
            </@card>
        </div>
    </section>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
