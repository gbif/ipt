<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.mapping.title'/></title>
    <script type="text/javascript">
        $(document).ready(function(){
            initHelp();
        });
    </script>

    <#assign currentMenu = "manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

<main class="container">
    <form class="topForm" action="mapping.do" method="post">
        <div class="my-3 p-3 bg-body rounded shadow-sm">

            <#include "/WEB-INF/pages/inc/action_alerts.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
                ${mapping.extension.title}
            </h5>

            <p class="text-muted mx-md-4 mx-2">${mapping.extension.description}</p>
            <#if mapping.extension.link?has_content>
                <p class="text-muted mx-md-4 mx-2"><@s.text name="basic.link"/>: <a href="${mapping.extension.link}">${mapping.extension.link}</a></p>
            </#if>
            <input type="hidden" name="r" value="${resource.shortname}" />
            <input type="hidden" name="id" value="${mapping.extension.rowType}" />
            <input type="hidden" name="mid" value="${mid!}" />
            <input id="showAllValue" type="hidden" name="showAll" value="${Parameters.showAll!"true"}" />
        </div>

        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
                <@s.text name='manage.mapping.source'/>
            </h5>

            <p class="text-muted mx-md-4 mx-2"><@s.text name='manage.mapping.source.help'/></p>

            <div class="row mx-md-3 mx-1">
                <div class="col-sm-6">
                    <@selectList name="source" options=resource.sources objValue="name" objTitle="name" i18nkey="manage.mapping.source" />
                </div>
            </div>

            <div class="row mt-3 mx-md-3 mx-1">
                <div class="col-12">
                    <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save"/>
                    <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.cancel" method="cancel"/>
                </div>
            </div>
        </div>
    </form>
</main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
