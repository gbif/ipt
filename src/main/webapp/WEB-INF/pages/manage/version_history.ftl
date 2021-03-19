<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
<title><@s.text name='manage.source.title'/></title>
<script type="text/javascript" src="${baseURL}/js/jconfirmation-bootstrap.jquery.js"></script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
<#include "/WEB-INF/pages/macros/forms-bootstrap.ftl"/>

<main class="container">

    <form action="history.do" method="post">

        <div class="my-3 p-3 bg-body rounded shadow-sm">

            <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

            <#if resource?? && version?? && resource.versionHistory??>
                <input type="hidden" name="r" value="${resource.shortname}" />
                <input type="hidden" name="v" value="${version.toPlainString()}" />
                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
                    <@s.text name='manage.history.title'/>: <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </h5>

                <p class="mx-md-4 mx-2 text-muted">
                    <@s.text name="manage.history.intro"><@s.param>${version.toPlainString()}</@s.param><@s.param>${resource.title!resource.shortname}</@s.param></@s.text>
                </p>

                <#assign versionTitle><@s.text name="manage.overview.published.version"/></#assign>
                <#assign releasedTitle><@s.text name="manage.overview.published.released"/></#assign>
                <#assign recordsTitle><@s.text name="portal.home.records"/></#assign>
                <#assign modifiedTitle><@s.text name="portal.home.modifiedBy"/></#assign>
                <#assign summaryTitle><@s.text name="portal.home.summary"/></#assign>
                <#assign doiTitle><@s.text name="portal.home.doi"/></#assign>
                <#assign none><@s.text name="basic.none"/></#assign>
                <#assign emptyPlaceholder="-"/>

                <div class="mx-md-4 mx-2">
                    <#list resource.versionHistory as history>
                        <#if history.version == version.toPlainString()>
                            <table id="${history.version}" class="table table-borderless">
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
                                        <td>${history.released?date!}</td>
                                    <#else>
                                        <td>${emptyPlaceholder}</td>
                                    </#if>
                                </tr>
                                <tr>
                                    <th>${recordsTitle?cap_first}</th>
                                    <td>${history.recordsPublished}</td>
                                </tr>
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
                                        <textarea name="summary" class="form-control" placeholder="Please summarize what has changed in this version">${history.changeSummary!}</textarea>
                                    </td>
                                </tr>
                            </table>
                        </#if>
                    </#list>
                </div>

                <div class="row">
                    <div class="buttons col-12">
                        <@s.submit cssClass="button btn btn-outline-success" name="save" key="button.save"/>
                        <@s.submit cssClass="button btn btn-outline-secondary" name="back" key="button.cancel"/>
                    </div>
                </div>
            <#else>
                <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </h5>

                <div class="row">
                    <div class="buttons col-12">
                        <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.back"/>
                    </div>
                </div>
            </#if>

        </div>

    </form>
</main>

<#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
</#escape>
