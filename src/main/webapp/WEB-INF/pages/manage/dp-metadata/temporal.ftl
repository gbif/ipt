<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.basic.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "temporal"/>
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
                        <@s.text name='manage.datapackagemetadata.temporal.title'/>
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
                                <div class="col-lg-6">
                                    <@input name="metadata.temporal.start" i18nkey="datapackagemetadata.temporal.start" requiredField=true />
                                </div>

                                <div class="col-lg-6">
                                    <@input name="metadata.temporal.end" i18nkey="datapackagemetadata.temporal.end" requiredField=true />
                                </div>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
