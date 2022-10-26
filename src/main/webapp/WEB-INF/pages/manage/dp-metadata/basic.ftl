<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageMetadataAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.basic.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "basic"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <form class="needs-validation" action="datapackage-metadata-${section}.do" method="post" novalidate>
        <div class="container-fluid bg-body border-bottom">
            <div class="container pt-2">
                <#include "/WEB-INF/pages/inc/action_alerts.ftl">
            </div>

            <div class="container my-3 p-3">
                <div class="text-center text-uppercase fw-bold fs-smaller-2">
                    <div class="text-center text-uppercase fw-bold fs-smaller-2">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb justify-content-center mb-0">
                                <li class="breadcrumb-item"><a href="/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div class="text-center">
                    <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                        <@s.text name='manage.datapackagemetadata.basic.title'/>
                    </h1>
                </div>

                <div class="text-center fs-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">
                        <#if resource.title?has_content>
                            ${resource.title}
                        <#else>
                            ${resource.shortname}
                        </#if>
                    </a>
                </div>

                <div class="text-center mt-2">
                    <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                    <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back"/>
                </div>
            </div>
        </div>

<#--        <#include "metadata_section_select.ftl"/>-->

        <div class="container-fluid bg-body">
            <div class="container bd-layout">

                <main class="bd-main bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "metadata_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">

                        <div class="my-md-3 p-3">
                            <div class="row g-3">
                                <div class="col-12">
                                    <@input name="metadata.title" i18nkey="datapackagemetadata.title" requiredField=true />
                                </div>

                                <div class="col-12">
                                    <@text name="metadata.description" i18nkey="datapackagemetadata.description" requiredField=true />
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <#assign removeContributorLink><@s.text name='manage.metadata.removethis'/> <@s.text name='datapackagemetadata.contributor'/></#assign>
                            <#assign addContributorLink><@s.text name='manage.metadata.addnew'/> <@s.text name='datapackagemetadata.contributor'/></#assign>

                            <!-- List of Contributors -->
                            <div>
                                <@textinline name="datapackagemetadata.contributors"/>
                                <div id="contributor-items">
                                    <#list metadata.contributors as item>
                                        <div id="contributor-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="columnLinks mt-2 d-flex justify-content-end">
                                                <a id="contributor-removeLink-${item_index}" href="" class="removeContributorLink text-smaller">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${removeContributorLink?lower_case?cap_first}</span>
                                                </a>
                                            </div>
                                            <div>
                                                <@input name="metadata.contributors[${item_index}].title" i18nkey="datapackagemetadata.title" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].path" i18nkey="datapackagemetadata.path" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].email" i18nkey="datapackagemetadata.email" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].role" i18nkey="datapackagemetadata.contributor.role" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.contributors[${item_index}].organization" i18nkey="datapackagemetadata.contributor.organization" />
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-contributor" class="text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addContributorLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <#assign removeSourceLink><@s.text name='manage.metadata.removethis'/> <@s.text name='datapackagemetadata.source'/></#assign>
                            <#assign addSourceLink><@s.text name='manage.metadata.addnew'/> <@s.text name='datapackagemetadata.source'/></#assign>

                            <!-- List of Sources -->
                            <div>
                                <@textinline name="datapackagemetadata.sources"/>
                                <div id="collection-items">
                                    <#list metadata.sources as item>
                                        <div id="source-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="columnLinks mt-2 d-flex justify-content-end">
                                                <a id="source-removeLink-${item_index}" href="" class="removeSourceLink text-smaller">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${removeSourceLink?lower_case?cap_first}</span>
                                                </a>
                                            </div>
                                            <div>
                                                <@input name="metadata.sources[${item_index}].title" i18nkey="datapackagemetadata.title" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.sources[${item_index}].path" i18nkey="datapackagemetadata.path" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.sources[${item_index}].email" i18nkey="datapackagemetadata.email" />
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-source" class="text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addSourceLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <#assign removeLicenseLink><@s.text name='manage.metadata.removethis'/> <@s.text name='datapackagemetadata.license'/></#assign>
                            <#assign addLicenseLink><@s.text name='manage.metadata.addnew'/> <@s.text name='datapackagemetadata.license'/></#assign>

                            <!-- List of Licenses -->
                            <div>
                                <@textinline name="datapackagemetadata.licenses"/>
                                <div id="license-items">
                                    <#list metadata.licenses as item>
                                        <div id="license-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="columnLinks mt-2 d-flex justify-content-end">
                                                <a id="license-removeLink-${item_index}" href="" class="removeLicenseLink text-smaller">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${removeSourceLink?lower_case?cap_first}</span>
                                                </a>
                                            </div>
                                            <div>
                                                <@input name="metadata.licenses[${item_index}].title" i18nkey="datapackagemetadata.title" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.licenses[${item_index}].path" i18nkey="datapackagemetadata.path" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.licenses[${item_index}].name" i18nkey="datapackagemetadata.name" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="metadata.licenses[${item_index}].scope" i18nkey="datapackagemetadata.license.scope" />
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-license" class="text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addLicenseLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>



                        <div class="my-md-3 p-3">
                            <p>
                                <strong>Profile (internal)</strong>
                                <br>
                                ${metadata.profile!"-"}
                            </p>

                            <p>
                                <strong>Resources (internal)</strong>
                                <br>
                                <#if metadata.resources??>
                                    <ol>
                                        <#list metadata.resources as resource>
                                            <li>
                                                <strong>Name:</strong> ${resource.name!}<br>
                                                <strong>Profile:</strong> ${resource.profile!}<br>
                                                <strong>Path:</strong> ${resource.path!}<br>
                                                <strong>Schema:</strong> ${resource.schema!}<br>
                                                <strong>Format:</strong> ${resource.format!}<br><br>
                                            </li>
                                        </#list>
                                    </ol>
                                <#else>
                                    -
                                </#if>
                            </p>

                            <p>
                                <strong>Name (internal)</strong>
                                <br>
                                ${metadata.name!"-"}
                            </p>

                            <p>
                                <strong>Id (internal)</strong>
                                <br>
                                ${metadata.id!"-"}
                            </p>

                            <p>
                                <strong>Created (internal)</strong>
                                <br>
                                <#if metadata.created??>
                                    ${metadata.created?datetime?string.medium}
                                <#else>
                                    -
                                </#if>
                            </p>

                            <p>
                                <strong>Version (internal)</strong>
                                <br>
                                ${metadata.version!"-"}
                            </p>

                            <p>
                                <strong>Homepage</strong>
                                <br>
                                <#if metadata.homepage?has_content>
                                    ${metadata.homepage}
                                <#else>
                                    -
                                </#if>
                            </p>

                            <p>
                                <strong>Image</strong>
                                <br>
                                <#if metadata.image?has_content>
                                    ${metadata.image}
                                <#else>
                                    -
                                </#if>
                            </p>

                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
