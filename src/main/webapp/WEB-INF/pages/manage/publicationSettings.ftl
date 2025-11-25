<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.PublicationSettingsAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.publication.title'/></title>

<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/popover.ftl"/>

<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
<script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
<script>
    $(document).ready(function() {
        $('select#id').select2({
            placeholder: '${action.getText("admin.organisation.name.select")?js_string}',
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            minimumResultsForSearch: 15,
            theme: 'bootstrap4',
            allowClear: true
        });

        // publishing organisation selection is only disabled, if resource has been registered with GBIF or assigned a DOI (no matter if it's reserved or public).
        var isRegisteredWithGBIF="${resource.key!}";
        var isAssignedDOI="${resource.doi!}";
        if (isRegisteredWithGBIF !== "" || isAssignedDOI !== "") {
            $("#id").attr('disabled','disabled');
        }
    });
</script>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center fs-smaller">
                <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                    <ol class="breadcrumb justify-content-center mb-0">
                        <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                        <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                        <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.publicationSettings"/></li>
                    </ol>
                </nav>
            </div>

            <div class="text-center">
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="manage.publicationSettings.title"/>
                </h1>

                <div class="text-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>

                <div class="mt-2">
                    <input type="submit" value="Save" id="save" name="save" class="btn btn-sm btn-outline-gbif-primary top-button" form="publication-form">
                    <input type="submit" value="Cancel" id="cancel" name="cancel" class="btn btn-sm btn-outline-secondary top-button" form="publication-form">
                </div>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container">
    <div class="my-3 p-3">
        <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
            <@popoverPropertyInfo "eml.publishingOrganisation.help"/>
            <@s.text name="eml.publishingOrganisation"/>
        </h5>

        <div class="row g-3 mt-0">
            <div class="col-lg-6">
                <form id="publication-form" class="needs-validation" action="publication-settings.do" method="post" novalidate>
                    <input type="hidden" name="r" value="${resource.shortname}" />
                    <@select name="id" i18nkey="eml.publishingOrganisation" options=organisations value="${(resource.organisation.key)!''}" requiredField=true />
                </form>
            </div>
        </div>
    </div>

    <div class="my-3 p-3">
        <div class="d-flex justify-content-between">
            <div class="d-flex">
                <h5 class="my-auto text-gbif-header-2 fw-400">
                    <@popoverPropertyInfo "manage.overview.autopublish.description"/>
                    <@s.text name="manage.overview.autopublish.title"/>
                </h5>
            </div>

            <div class="d-flex justify-content-end">
                <a id="edit-autopublish-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button" href="auto-publish.do?r=${resource.shortname}">
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                    </svg>
                    <@s.text name="button.edit"/>
                </a>
            </div>
        </div>

        <div class="mt-4">
            <p class="mb-0">
                <#if resource.usesAutoPublishing()>
                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill autopublish-enabled">
                                    <@s.text name="manage.overview.autopublish.enabled"/>: ${autoPublishFrequencies.get(resource.updateFrequency.identifier)}
                                </span>
                    <@s.text name="manage.overview.autopublish.intro.activated"/>
                <#else>
                    <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill autopublish-disabled">
                                    <@s.text name="manage.overview.autopublish.disabled"/>
                                </span>
                    <@s.text name="manage.overview.autopublish.intro.deactivated"/>
                </#if>
            </p>

            <#if resource.isDeprecatedAutoPublishingConfiguration()>
                <div class="callout callout-warning text-smaller">
                    <@s.text name="manage.overview.autopublish.deprecated.warning.button" escapeHtml=true/>
                </div>
            </#if>
        </div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
