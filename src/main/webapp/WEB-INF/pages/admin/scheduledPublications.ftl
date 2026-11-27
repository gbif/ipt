<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.ScheduledPublicationsAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/auto-publishing.css">
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<title><@s.text name="admin.scheduledPublications.title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<#macro when date><span title="${date?datetime?string("yyyy-MM-dd HH:mm:ss")}">${date?datetime?string("yyyy-MM-dd HH:mm")}</span></#macro>

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
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="admin.scheduledPublications.title"/></li>
                        </ol>
                    </nav>
                </div>
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="admin.scheduledPublications.title"/></h1>

                <#-- counts as "Label: N" -->
                <div class="ov-meta">
                    <span><@s.text name="admin.home.state.scheduled"><@s.param>${scheduledResources?size}</@s.param></@s.text></span>
                    <#if suspendedCount gt 0>
                        <span>·</span>
                        <span><@s.text name="admin.scheduledPublications.suspendedCount"><@s.param>${suspendedCount}</@s.param></@s.text></span>
                    </#if>
                    <span>·</span>
                    <span><@s.text name="manage.overview.autopublish.title"/>:
                        <#if !autoPublishingEnabled><@s.text name="admin.home.state.autoPublishing.off"/><#elseif autoPublishingAdminOnly><@s.text name="admin.home.state.autoPublishing.adminOnly"/><#else><@s.text name="admin.home.state.autoPublishing.on"/></#if>
                    </span>
                </div>
            </div>
            <div class="ov-acts">
                <a href="${baseURL}/admin/autoPublishingSettings.do" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="admin.home.autoPublicationSettings"/></a>
                <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
            </div>
        </div>
    </div>
</div>

<#-- one list of records in a bordered box, as Users and Organizations -->
<main class="container main-content-container border rounded-2 mt-4 mb-5">
    <div class="py-4 px-md-5 px-3 ap-page">

        <#if !autoPublishingEnabled && scheduledResources?has_content>
            <div class="ov-banner warn" role="status">
                <@icon name="warning" class="ov-ic"/>
                <div class="ov-grow">
                    <@s.text name="admin.scheduledPublications.suppressed"/>
                    <a href="${baseURL}/admin/autoPublishingSettings.do"><@s.text name="admin.home.autoPublicationSettings"/></a>
                </div>
            </div>
        </#if>

        <#if scheduledResources?has_content>
            <div class="table-responsive">
                <table class="ov-table">
                    <thead>
                    <tr>
                        <th scope="col"><@s.text name="admin.scheduledPublications.resource"/></th>
                        <th scope="col"><@s.text name="admin.scheduledPublications.frequency"/></th>
                        <th scope="col"><@s.text name="admin.scheduledPublications.nextPublished"/></th>
                        <th scope="col"><@s.text name="admin.scheduledPublications.lastPublished"/></th>
                        <th scope="col" class="ov-num"><@s.text name="admin.scheduledPublications.records"/></th>
                        <th scope="col"><@s.text name="admin.scheduledPublications.managers"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <#list scheduledResources as r>
                        <tr>
                            <td class="ap-name">
                                <a href="${baseURL}/manage/resource.do?r=${r.shortname?url}" class="resource-table-link">${r.title?has_content?then(r.title, r.shortname)}</a>
                                <div class="ap-shortname">${r.shortname}</div>
                            </td>
                            <td class="text-nowrap">
                                <a href="${baseURL}/manage/publication-settings.do?r=${r.shortname?url}" class="ap-link">${r.frequency!}</a>
                            </td>
                            <td class="text-nowrap">
                                <#if r.suspended>
                                    <span class="ap-status bad" title="${action.getText("manage.publicationSettings.auto.suspended")}"><@icon name="error"/> <@s.text name="manage.overview.autopublish.suspended"/></span>
                                <#elseif !r.nextPublished??>
                                    <span class="ap-muted">-</span>
                                <#elseif !autoPublishingEnabled>
                                    <span class="ap-muted ap-paused"><@when r.nextPublished/></span>
                                <#elseif r.overdue>
                                    <span class="ap-status warn"><@icon name="warning"/> <@when r.nextPublished/></span>
                                <#else>
                                    <@when r.nextPublished/>
                                </#if>
                            </td>
                            <td class="text-nowrap"><#if r.lastPublished??><@when r.lastPublished/><#else><span class="ap-muted">-</span></#if></td>
                            <td class="ov-num">${r.recordsPublished?string.number}</td>
                            <td class="ap-managers">${r.managers!}</td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
        <#else>
            <p class="ap-ok"><@icon name="info_outlined"/> <@s.text name="admin.scheduledPublications.empty"/></p>
        </#if>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
