<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.history.title'/></title>
<script src="${baseURL}/js/jconfirmation.jquery.js"></script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="text-center fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.version"/></li>
                        </ol>
                    </nav>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name='manage.history.title'/>
                </h1>

                <div class="text-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>

                <div class="mt-2">
                    <#if resource?? && version?? && resource.versionHistory??>
                        <@s.submit form="history" cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                        <a href="${baseURL}/resource?r=${resource.shortname}" class="btn btn-sm btn-outline-secondary mt-1 me-xl-1 top-button">
                            <@s.text name="button.cancel"/>
                        </a>
                    <#else>
                        <@s.submit form="history" cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back"/>
                    </#if>
                </div>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container">
    <form id="history" action="history.do" method="post">
        <div class="my-3 p-3">
            <#if resource?? && version?? && resource.versionHistory??>
                <input type="hidden" name="r" value="${resource.shortname}" />
                <input type="hidden" name="v" value="${version.toPlainString()}" />

                <p>
                    <@s.text name="manage.history.intro"><@s.param>${version.toPlainString()}</@s.param><@s.param>${resource.title!resource.shortname}</@s.param></@s.text>
                </p>

                <#assign versionTitle><@s.text name="manage.overview.published.version"/></#assign>
                <#assign versionTitle = versionTitle?markup_string>
                <#assign releasedTitle><@s.text name="manage.overview.published.released"/></#assign>
                <#assign releasedTitle = releasedTitle?markup_string>
                <#assign recordsTitle><@s.text name="portal.home.records"/></#assign>
                <#assign recordsTitle = recordsTitle?markup_string>
                <#assign modifiedTitle><@s.text name="portal.home.modifiedBy"/></#assign>
                <#assign modifiedTitle = modifiedTitle?markup_string>
                <#assign summaryTitle><@s.text name="portal.home.summary"/></#assign>
                <#assign summaryTitle = summaryTitle?markup_string>
                <#assign doiTitle><@s.text name="portal.home.doi"/></#assign>
                <#assign doiTitle = doiTitle?markup_string>
                <#assign none><@s.text name="basic.none"/></#assign>
                <#assign emptyPlaceholder="-"/>

                <div>
                    <#list resource.versionHistory as history>
                        <#if history.version == version.toPlainString()>
                            <table id="${history.version}" class="table table-sm table-borderless">
                                <tr>
                                    <th>${versionTitle?cap_first}</th>
                                    <td>${history.version}</td>
                                </tr>
                                <tr>
                                    <th>${doiTitle}</th>
                                    <#if history.status! == "PUBLIC">
                                        <#if history.doi??>
                                            <td>${history.doi.getDoiName()}</td>
                                        <#else>
                                            <td>${none}</td>
                                        </#if>
                                    <#else>
                                        <td>${emptyPlaceholder}</td>
                                    </#if>
                                </tr>
                                <tr>
                                    <th>${releasedTitle?cap_first}</th>
                                    <#if history.released??>
                                        <td>${history.released?datetime?string.long_medium!}</td>
                                    <#else>
                                        <td>${emptyPlaceholder}</td>
                                    </#if>
                                </tr>
                                <#if !resource.schemaIdentifier??>
                                <tr>
                                    <th>${recordsTitle?cap_first}</th>
                                    <td>${history.recordsPublished}</td>
                                </tr>
                                </#if>
                                <tr>
                                    <th>${modifiedTitle?cap_first}</th>
                                    <#if history.modifiedBy??>
                                        <td>${history.modifiedBy.firstname!} ${history.modifiedBy.lastname!}</td>
                                    <#else>
                                        <td>${emptyPlaceholder}</td>
                                    </#if>
                                </tr>
                                <tr>
                                    <th>${summaryTitle?cap_first}</th>
                                    <td>
                                        <textarea name="summary" class="form-control" cols="60" rows="5" placeholder="Please summarize what has changed in this version">${history.changeSummary!}</textarea>
                                    </td>
                                </tr>
                            </table>
                        </#if>
                    </#list>
                </div>
            </#if>
        </div>
    </form>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
