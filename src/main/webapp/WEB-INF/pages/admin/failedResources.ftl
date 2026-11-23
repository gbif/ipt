<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.FailedResourcesAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/failed-resources.css">
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<title><@s.text name="admin.failedResources.title"/></title>
<script>
    $(document).ready(function () {
        // The Clipboard API needs HTTPS (or localhost); many IPTs run on plain HTTP, so fall back to a selected text area
        function copy(text) {
            if (navigator.clipboard && window.isSecureContext) return navigator.clipboard.writeText(text);
            var area = document.createElement("textarea");
            area.value = text;
            area.setAttribute("readonly", "");
            area.style.position = "fixed";
            area.style.opacity = "0";
            document.body.appendChild(area);
            area.select();
            var ok = document.execCommand("copy");
            area.remove();
            return ok ? Promise.resolve() : Promise.reject();
        }

        // copy the resources folder path, to open it in a terminal or file manager; labels come from data attributes,
        // so translations with quotes stay intact
        $("#fr-copy-path").on("click", function () {
            var btn = $(this), label = btn.find(".fr-copy-label");
            copy($("#fr-path").text()).then(function () {
                label.text(btn.attr("data-copied"));
                setTimeout(function () {
                    label.text(btn.attr("data-copy"));
                }, 2000);
            });
        });

        // one retry at a time; enabled again when the page comes back from the browser's back-forward cache
        $(".fr-retry-form").on("submit", function () {
            $(this).find("button").prop("disabled", true);
        });
        $(window).on("pageshow", function () {
            $(".fr-retry-form button").prop("disabled", false);
        });
    });
</script>
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
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.failedResources"/></li>
                        </ol>
                    </nav>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">
                    <@s.text name="admin.failedResources.title"/>
                </h1>

                <#-- counts as "Label: N" -->
                <div class="ov-meta">
                    <span><@s.text name="admin.home.state.failed"><@s.param>${failures?size}</@s.param></@s.text></span>
                    <span>·</span>
                    <span><@s.text name="admin.home.state.notices"><@s.param>${resourcesWithNotices?size}</@s.param></@s.text></span>
                </div>
            </div>
            <div class="ov-acts">
                <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button">
                    <@s.text name="button.back"/>
                </a>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container mb-5">
    <div class="my-3 fr-page">

        <#-- resource folders the IPT couldn't load: they show nowhere else -->
        <section class="ov-card">
            <div class="ov-ch">
                <h2><@s.text name="admin.failedResources.failed"/></h2>
                <#if failures?has_content><span class="fr-count bad">${failures?size}</span></#if>
            </div>

            <#if failures?has_content>
                <p class="ov-sub"><@s.text name="admin.failedResources.failed.intro"/></p>

                <div class="fr-list">
                    <#list failures as f>
                        <div class="fr-row">
                            <span class="fr-ic bad"><@icon name="error"/></span>
                            <div class="fr-body">
                                <div class="fr-name">
                                    <code>${f.shortname}</code>
                                    <span class="fr-when"><@s.text name="admin.failedResources.failedAt"/>: ${f.failedAt?datetime?string("yyyy-MM-dd HH:mm")}</span>
                                </div>
                                <div class="fr-reason">${f.reason}</div>
                            </div>
                            <form action="${baseURL}/admin/retryFailedResource.do" method="post" class="fr-retry-form">
                                <input type="hidden" name="r" value="${f.shortname}"/>
                                <button type="submit" class="btn btn-sm btn-outline-gbif-primary text-nowrap">
                                    <@icon name="autorenew"/> <@s.text name="admin.failedResources.retry"/>
                                </button>
                            </form>
                        </div>
                    </#list>
                </div>

                <div class="ov-banner info compact fr-help">
                    <span class="ov-ic"><@icon name="info_outlined"/></span>
                    <div class="ov-grow">
                        <p class="mb-1"><@s.text name="admin.failedResources.retry.help"/></p>
                        <p class="mb-1 fr-path-line">
                            <@s.text name="admin.failedResources.folder"/>
                            <code id="fr-path">${resourcesDirPath}</code>
                            <button type="button" id="fr-copy-path" class="fr-copy" data-copy="<@s.text name="button.copy"/>" data-copied="<@s.text name="admin.failedResources.copied"/>">
                                <@icon name="content_copy"/> <span class="fr-copy-label"><@s.text name="button.copy"/></span>
                            </button>
                        </p>
                        <p class="mb-0">
                            <@s.text name="admin.failedResources.logs"/>
                            <a href="${baseURL}/admin/logs.do" class="resource-table-link"><@s.text name="admin.home.manageLogs"/></a>
                        </p>
                    </div>
                </div>
            <#else>
                <p class="fr-ok"><@icon name="check_circle"/> <@s.text name="admin.failedResources.empty"/></p>
            </#if>
        </section>

        <#-- resources loaded with a workaround, e.g. metadata from the last published version -->
        <section class="ov-card">
            <div class="ov-ch">
                <h2><@s.text name="admin.failedResources.notices"/></h2>
                <#if resourcesWithNotices?has_content><span class="fr-count warn">${resourcesWithNotices?size}</span></#if>
            </div>

            <#if resourcesWithNotices?has_content>
                <p class="ov-sub"><@s.text name="admin.failedResources.notices.intro"/></p>

                <div class="fr-list">
                    <#list resourcesWithNotices as res>
                        <div class="fr-row">
                            <div class="fr-body">
                                <div class="fr-name">
                                    <a href="${baseURL}/manage/resource.do?r=${res.shortname?url}" class="resource-table-link">${res.title?has_content?then(res.title, res.shortname)}</a>
                                    <span class="fr-when">${res.shortname}</span>
                                </div>
                                <ul class="fr-notices">
                                    <#list res.loadNotices as n>
                                        <li<#if n.needsAttention> class="warn"</#if>>
                                            <span class="fr-ic"><#if n.needsAttention><@icon name="warning"/><#else><@icon name="info_outlined"/></#if></span>
                                            <span><@s.text name=n.textKey><@s.param>${n.detail}</@s.param></@s.text></span>
                                        </li>
                                    </#list>
                                </ul>
                            </div>
                        </div>
                    </#list>
                </div>
            <#else>
                <p class="fr-ok"><@icon name="check_circle"/> <@s.text name="admin.failedResources.notices.empty"/></p>
            </#if>
        </section>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
