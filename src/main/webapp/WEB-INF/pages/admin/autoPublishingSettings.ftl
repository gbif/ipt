<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.autoPublishing.title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<style>
    .form-switch .form-check-input:focus:not(:checked) {
        background-image: radial-gradient(circle closest-side,
            rgb(var(--color-gbif-primary)) 0,
            rgb(var(--color-gbif-primary)) 74%,
            transparent 78%);
        background-size: 1em 1em;
        background-repeat: no-repeat;
    }
</style>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                            <li class="breadcrumb-item"><@s.text name="breadcrumb.admin.autopublication"/></li>
                        </ol>
                    </nav>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="admin.autoPublishing.title"/>
                </h1>

                <#-- The submit button lives outside the form element, so it points to the form by id -->
                <div class="mt-2">
                    <button type="submit" form="autoPublishingForm" name="save" class="btn btn-sm btn-outline-gbif-primary top-button">
                        <@s.text name="button.save"/>
                    </button>
                    <a href="${baseURL}/admin/scheduledPublications.do" class="btn btn-sm btn-outline-gbif-primary top-button">
                        <@s.text name="admin.autoPublishing.viewScheduled"/>
                    </a>
                    <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button">
                        <@s.text name="button.cancel"/>
                    </a>
                </div>

                <p class="mt-3 mb-0 text-smaller fst-italic">
                    <@s.text name="admin.autoPublishing.intro"/>
                </p>
            </div>
        </div>
    </div>
</div>

<main class="container">
    <div class="my-3 p-3">
        <@s.form id="autoPublishingForm" action="autoPublishingSettings" method="post" cssClass="needs-validation">
            <div class="form-check form-switch mb-3">
                <@s.checkbox name="autoPublishingEnabled" id="autoPublishingEnabled" cssClass="form-check-input" theme="simple"/>
                <label class="form-check-label" for="autoPublishingEnabled">
                    <@s.text name="admin.autoPublishing.enable"/>
                </label>
                <div class="form-text">
                    <@s.text name="admin.autoPublishing.help"/>
                </div>
            </div>

            <#-- Only relevant while auto-publication is enabled; hidden (not disabled) otherwise.
                 The action preserves the stored value when auto-publication is switched off. -->
            <div id="adminOnlyContainer" class="form-check mb-3 <#if !autoPublishingEnabled> d-none</#if>">
                <@s.checkbox name="autoPublishingAdminOnly" id="autoPublishingAdminOnly" cssClass="form-check-input" theme="simple"/>
                <label class="form-check-label" for="autoPublishingAdminOnly">
                    <@s.text name="admin.autoPublishing.adminOnly"/>
                </label>
                <div class="form-text">
                    <@s.text name="admin.autoPublishing.adminOnly.help"/>
                </div>
            </div>
        </@s.form>
    </div>
</main>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        var toggle = document.getElementById('autoPublishingEnabled');
        var container = document.getElementById('adminOnlyContainer');
        if (toggle && container) {
            toggle.addEventListener('change', function () {
                container.classList.toggle('d-none', !toggle.checked);
            });
        }
    });
</script>

<#include "/WEB-INF/pages/inc/footer.ftl">
