<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.AdminResourceManagementOverviewAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <script src="${baseURL}/js/jquery/jquery-3.7.0.min.js"></script>
    <script>
        $(document).ready(function(){
            // display/hide stacktrace
            $('#toggle-stacktrace').click(function(event) {
                event.preventDefault();

                var stacktraceDiv = $('#error-stack-trace-block');

                // Toggle visibility
                stacktraceDiv.toggle();

                var isStacktraceDivVisible = stacktraceDiv.is(':visible');

                // Change the link text
                if (isStacktraceDivVisible) {
                    $(this).text('Hide stacktrace');
                } else {
                    $(this).text('Show stacktrace');
                }
            });
        });
    </script>
    <title><@s.text name="title"/></title>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/macros/forms.ftl">
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <#macro fileItem shortname name size lastModified index>
        <div class="col-xl-6">
            <div class="d-flex border rounded-2 mx-1 p-1 py-2 source-item">
                <div class="d-flex source-item-icon ps-2 my-auto" data-ipt-resource="${shortname}">
                    <i class="bi bi-file-text me-1 text-gbif-primary"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.file'/><br><span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br>"></i>
                </div>

                <div class="fs-smaller-2 source-item-link text-truncate ps-2 me-auto" data-ipt-resource="${shortname}">
                    <span class="fs-smaller fw-bold">${name}</span><br>
                    <small>
                        <#attempt><@action.formattedFileSizeSimplified(size)?interpret /><#recover>-</#attempt> <span class="fw-bold">|</span>
                    </small>

                    <small>
                        ${(lastModified?replace(",", "")?number)?number_to_datetime?datetime?string.medium}
                    </small>
                </div>

                <div class="d-flex justify-content-end my-auto source-item-actions">
                    <a title="<@s.text name="button.view"/>" class="icon-button icon-material-actions source-item-action fs-smaller-2 d-sm-max-none" type="button" href="">
                        <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                        </svg>
                    </a>
                    <a title="<@s.text name="button.download"/>" class="icon-button icon-material-actions source-item-action fs-smaller-2 d-sm-max-none" type="button" href="">
                        <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                            <path d="M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z"></path>
                        </svg>
                    </a>

                    <div class="dropdown d-sm-none">
                        <a class="icon-button icon-material-actions source-item-action" type="button" href="#" id="dropdown-source-item-actions-${index}" data-bs-toggle="dropdown" aria-expanded="false">
                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                            </svg>
                        </a>

                        <ul class="dropdown-menu" aria-labelledby="dropdown-source-item-actions-${index}">
                            <li>
                                <a class="dropdown-item action-link" type="button" href="" target="_blank">
                                    <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                        <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                    </svg>
                                    <@s.text name="button.view"/>
                                </a>
                            </li>
                            <li>
                                <a class="dropdown-item action-link" type="button" href="">
                                    <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                        <path d="M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z"></path>
                                    </svg>
                                    <@s.text name="button.download"/>
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </#macro>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid bg-body border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center">
                    <div class="fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);"
                             aria-label="breadcrumb">
                            <ol class="breadcrumb justify-content-center mb-0">
                                <li class="breadcrumb-item">
                                    <a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a>
                                </li>
                                <li class="breadcrumb-item">
                                    <a href="${baseURL}/admin/manageResources.do">
                                        <@s.text name="breadcrumb.admin.manageResources"/>
                                    </a>
                                </li>
                                <li class="breadcrumb-item">
                                    <@s.text name="breadcrumb.admin.manageResource"/>
                                </li>
                            </ol>
                        </nav>
                    </div>

                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        ${shortname}
                    </h1>

                    <#if resourceSuccessfullyLoaded>
                        <div class="text-gbif-primary fs-smaller-2 mt-1">
                            <span>
                                Resource has been loaded successfully
                            </span>
                        </div>

                        <div class="fs-smaller-2 mt-1">
                            <span>
                                <a href="${baseURL}/manage/resource?r=${shortname}">Resource overview</a>
                            </span>
                        </div>
                    <#else>
                         <div class="text-gbif-danger fs-smaller-2 mt-1">
                            <span>
                                Resource has been failed to load
                            </span>
                         </div>
                    </#if>

                    <div class="mt-2">
                        <a href="${baseURL}/admin/manageResources.do" class="button btn btn-sm btn-outline-secondary top-button">
                            <@s.text name="button.back"/>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <main class="container main-content-container my-4">
        <#if resource.failed>
        <div class="py-md-4 py-3">
            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                Error
            </h4>

            <div>
                ${resource.errorMessage!}
            </div>

            <#if resource.errorStackTrace?has_content>
                <div class="mt-2">
                    <a href="#" id="toggle-stacktrace">Show stacktrace</a>
                </div>

                <div id="error-stack-trace-block" class="mt-2 fs-smaller" style="display: none;">
                    <pre>${resource.errorStackTrace}</pre>
                </div>
            </#if>
        </div>
        </#if>

        <div class="py-md-4 py-3">
            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                Resource file
            </h4>

            <div class="row g-2">
                <@fileItem shortname=shortname name=resourceFile.name size=resourceFile.length() lastModified=resourceFile.lastModified() index=0/>
            </div>
        </div>

        <#if sourceFiles?has_content>
        <div class="py-md-4 py-3">
            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                Source files
            </h4>

            <div class="row g-2">
                <#list sourceFiles as sf>
                    <@fileItem shortname=shortname name=sf.name size=sf.length() lastModified=sf.lastModified() index=sf_index/>
                </#list>
            </div>
        </div>
        </#if>

        <div class="py-md-4 py-3">
            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                Metadata files
            </h4>

            <div class="row g-2">
                <#list metadataFiles as mf>
                    <@fileItem shortname=shortname name=mf.name size=mf.length() lastModified=mf.lastModified() index=mf_index/>
                </#list>
            </div>
        </div>

        <#if generatedArchives?has_content>
        <div class="py-md-4 py-3">
            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                Generated archives
            </h4>

            <div class="row g-2">
                <#list generatedArchives as ga>
                    <@fileItem shortname=shortname name=ga.name size=ga.length() lastModified=ga.lastModified() index=ga_index/>
                </#list>
            </div>
        </div>
        </#if>

        <#if otherFiles?has_content>
        <div class="py-md-4 py-3">
            <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                Other files
            </h4>

            <div class="row g-2">
                <#list otherFiles as of>
                    <@fileItem shortname=shortname name=of.name size=of.length() lastModified=of.lastModified() index=of_index/>
                </#list>
            </div>
        </div>
        </#if>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
