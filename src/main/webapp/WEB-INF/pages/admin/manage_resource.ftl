<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.AdminResourceManagementAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <script src="${baseURL}/js/jquery/jquery-3.7.0.min.js"></script>
    <title><@s.text name="title"/></title>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/macros/forms.ftl">
    <#include "/WEB-INF/pages/inc/menu.ftl">

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
                                <li class="breadcrumb-item"><a
                                            href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                                <li class="breadcrumb-item"><@s.text name="breadcrumb.admin.manageResources"/></li>
                            </ol>
                        </nav>
                    </div>

                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        <@s.text name="admin.home.manageResources"/>
                    </h1>

                    <div class="mt-2">
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" form="ui-management-form" name="save" id="save" key="button.save" />
                        <a href="${baseURL}/admin/manageResources.do" class="button btn btn-sm btn-outline-secondary top-button">
                            <@s.text name="button.back"/>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <main class="container main-content-container border rounded my-4">
        <div class="py-md-4 py-3 px-md-5 px-3">
            Manage resource
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
