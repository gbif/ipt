<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.failedResources.title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

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
                            <li class="breadcrumb-item"><@s.text name="breadcrumb.admin.failedResources"/></li>
                        </ol>
                    </nav>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="admin.failedResources.title"/>
                </h1>

                <div class="mt-2">
                    <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button">
                        <@s.text name="button.cancel"/>
                    </a>
                </div>

                <p class="mt-3 mb-0 text-smaller fst-italic">
                    <@s.text name="admin.failedResources.intro"/>
                </p>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container border rounded-2 mt-4 mb-5">
    <div class="mb-3 py-4 px-5">
        <h5 class="text-gbif-header-2 fw-400 mb-3"><@s.text name="admin.failedResources.failed"/></h5>
        <#if failures?has_content>
            <p class="text-smaller"><@s.text name="admin.failedResources.failed.intro"/></p>
            <p class="text-smaller">
                <@s.text name="admin.failedResources.folder"/>
                <code>${resourcesDirPath}</code>
            </p>
            <p class="text-smaller">
                <@s.text name="admin.failedResources.logs"/>
                <a href="${baseURL}/admin/logs.do"><@s.text name="admin.home.manageLogs"/></a>
            </p>
            <p class="text-smaller">
                <@s.text name="admin.failedResources.retry.help"/>
            </p>

            <div class="table-responsive text-smaller pt-2">
                <table class="table table-sm align-middle">
                    <thead>
                    <tr>
                        <th scope="col"><@s.text name="admin.failedResources.shortname"/></th>
                        <th scope="col"><@s.text name="admin.failedResources.reason"/></th>
                        <th scope="col" class="text-nowrap"><@s.text name="admin.failedResources.failedAt"/></th>
                        <th scope="col"><span class="visually-hidden"><@s.text name="admin.failedResources.retry"/></span></th>
                    </tr>
                    </thead>
                    <tbody>
                    <#list failures as f>
                        <tr>
                            <td class="text-nowrap"><code>${f.shortname}</code></td>
                            <td class="text-break">${f.reason}</td>
                            <td class="text-nowrap">${f.failedAt?datetime?string("yyyy-MM-dd HH:mm")}</td>
                            <td class="text-end">
                                <form action="${baseURL}/admin/retryFailedResource.do" method="post" class="d-inline">
                                    <input type="hidden" name="r" value="${f.shortname}"/>
                                    <button type="submit" class="btn btn-sm btn-outline-gbif-primary text-nowrap">
                                        <@s.text name="admin.failedResources.retry"/>
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
        <#else>
            <p class="text-smaller"><@s.text name="admin.failedResources.empty"/></p>
        </#if>

        <h5 class="text-gbif-header-2 fw-400 mt-4 mb-3"><@s.text name="admin.failedResources.notices"/></h5>
        <#if resourcesWithNotices?has_content>
            <p class="text-smaller"><@s.text name="admin.failedResources.notices.intro"/></p>
            <div class="table-responsive text-smaller pt-2">
                <table class="table table-sm align-middle">
                    <thead>
                    <tr>
                        <th scope="col"><@s.text name="admin.failedResources.resource"/></th>
                        <th scope="col"><@s.text name="admin.failedResources.notice"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <#list resourcesWithNotices as res>
                        <tr>
                            <td class="text-nowrap">
                                <a href="${baseURL}/manage/resource.do?r=${res.shortname?url}">${res.title!res.shortname}</a>
                            </td>
                            <td>
                                <#list res.loadNotices as n>
                                    <div<#if n.needsAttention> class="text-gbif-danger"</#if>>
                                        <@s.text name=n.textKey><@s.param>${n.detail}</@s.param></@s.text>
                                    </div>
                                </#list>
                            </td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
        <#else>
            <p class="text-smaller mb-0"><@s.text name="admin.failedResources.notices.empty"/></p>
        </#if>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
