<#ftl output_format="HTML">
<#-- heading of the EML metadata pages: left-aligned, as on the overview and mapping pages (styles/overview.css .ov-head-left).
     Breadcrumb Manage / <resource title> / Metadata, the page name as the title, Save and Back (to the resource overview) bottom right.
     Needs currentMetadataPage (the title is manage.metadata.<page>.title) and the icon macro: each page includes
     macros/icon.ftl near the top, before inc/action_alerts.ftl, so the alerts get Material icons too -->

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3 ov-head ov-head-left">
            <div class="ov-grow">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item ov-crumb-resource"><a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="manage.metadata.${currentMetadataPage}.title"/></h1>
            </div>
            <div class="ov-acts">
                <#-- id top-save: coverage.ftl checks the coordinates before submitting -->
                <button type="submit" id="top-save" class="btn btn-sm btn-gbif-solid top-button" name="save" value="save"><@icon name="save"/> <@s.text name="button.save"/></button>
                <a class="btn btn-sm btn-outline-secondary top-button" href="resource.do?r=${resource.shortname}"><@s.text name="button.back"/></a>
            </div>
        </div>
    </div>
</div>
