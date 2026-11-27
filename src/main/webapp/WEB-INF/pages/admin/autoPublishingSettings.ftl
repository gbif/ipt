<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.AutoPublishingSettingsAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/auto-publishing.css">
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<title><@s.text name="admin.autoPublishing.title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3 ov-head ov-head-left">
            <div class="ov-grow">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.autopublication"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="admin.autoPublishing.title"/></h1>
            </div>
            <div class="ov-acts">
                <#-- the submit button lives outside the form element, so it points to the form by id -->
                <button type="submit" form="autoPublishingForm" id="save" name="save" value="save" class="btn btn-sm btn-gbif-solid top-button">
                    <@icon name="save"/> <@s.text name="button.save"/>
                </button>
                <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container mb-5">
    <div class="my-3 ap-page">
        <form id="autoPublishingForm" action="${baseURL}/admin/autoPublishingSettings.do" method="post">
            <section class="ov-card" aria-labelledby="ap-settings-title">
                <div class="ov-ch"><h2 id="ap-settings-title"><@s.text name="manage.overview.autopublish.title"/></h2></div>
                <p class="ov-sub"><@s.text name="admin.autoPublishing.intro"/></p>

                <div class="form-check form-switch ap-switch">
                    <input class="form-check-input" type="checkbox" role="switch" id="autoPublishingEnabled" name="autoPublishingEnabled" value="true"<#if autoPublishingEnabled> checked</#if>>
                    <input type="hidden" name="__checkbox_autoPublishingEnabled" value="true">
                    <label class="form-check-label" for="autoPublishingEnabled"><@s.text name="admin.autoPublishing.enable"/></label>
                    <div class="ap-hint"><@s.text name="admin.autoPublishing.help"/></div>
                </div>

                <#-- only means something while auto-publishing is on: disabled, not hidden, otherwise.
                     The action keeps the stored value when auto-publishing is switched off. -->
                <div class="form-check form-switch ap-switch ap-sub" id="ap-admin-only">
                    <input class="form-check-input" type="checkbox" role="switch" id="autoPublishingAdminOnly" name="autoPublishingAdminOnly" value="true"<#if autoPublishingAdminOnly> checked</#if><#if !autoPublishingEnabled> disabled</#if>>
                    <input type="hidden" name="__checkbox_autoPublishingAdminOnly" value="true">
                    <label class="form-check-label" for="autoPublishingAdminOnly"><@s.text name="admin.autoPublishing.adminOnly"/></label>
                    <div class="ap-hint"><@s.text name="admin.autoPublishing.adminOnly.help"/></div>
                </div>
            </section>
        </form>

        <#-- what the switch affects, as on the admin home card -->
        <section class="ov-card" aria-labelledby="ap-scheduled-title">
            <div class="ov-ch"><h2 id="ap-scheduled-title"><@s.text name="admin.scheduledPublications.title"/></h2></div>
            <p class="ap-state">
                <#if scheduledCount == 0>
                    <@s.text name="admin.home.state.scheduled.none"/>
                <#else>
                    <@s.text name="admin.home.state.scheduled"><@s.param>${scheduledCount}</@s.param></@s.text>
                    <#if !autoPublishingEnabled>
                        · <@s.text name="admin.home.state.scheduled.paused"/>
                    <#elseif nextScheduled??>
                        · <@s.text name="admin.home.state.scheduled.next"><@s.param>${nextScheduled.title?has_content?then(nextScheduled.title, nextScheduled.shortname)}</@s.param><@s.param>${nextScheduled.nextPublished?datetime?string("yyyy-MM-dd HH:mm")}</@s.param></@s.text>
                    </#if>
                </#if>
            </p>
            <#if scheduledCount gt 0>
                <a href="${baseURL}/admin/scheduledPublications.do" class="btn btn-sm btn-outline-secondary"><@s.text name="admin.autoPublishing.viewScheduled"/></a>
            </#if>
        </section>
    </div>
</main>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        var toggle = document.getElementById('autoPublishingEnabled');
        var adminOnly = document.getElementById('autoPublishingAdminOnly');
        toggle.addEventListener('change', function () {
            adminOnly.disabled = !toggle.checked;
        });
    });
</script>

<#include "/WEB-INF/pages/inc/footer.ftl">
