<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.mapping.title'/></title>
    <#assign currentMenu = "manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

        <form class="topForm" action="schemaMapping.do" method="post">
            <div class="container-fluid bg-body border-bottom">

                <div class="container pt-2">
                    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
                </div>

                <div class="container p-3 my-3">

                    <div class="text-center">
                        <h1 class="pt-2 text-gbif-header fs-4 fw-400 text-center">
                            <@s.text name="manage.mapping.title"/>
                        </h1>

                        <div class="text-center fs-smaller">
                            <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                        </div>

                        <div class="my-2">
                            <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                            <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.cancel" method="cancel"/>
                        </div>

                    </div>
                </div>
            </div>

            <div class="container-fluid bg-body">
                <div class="container pt-4">
                    <p>${mapping.dataSchema.description}</p>

                    <input type="hidden" name="r" value="${resource.shortname}" />
                    <input type="hidden" name="id" value="${mapping.dataSchema.identifier}" />
                    <input type="hidden" name="schemaName" value="${mapping.dataSchema.name}" />
                    <input type="hidden" name="mid" value="${mid!}" />
                    <input id="showAllValue" type="hidden" name="showAll" value="${Parameters.showAll!"true"}" />

                    <p><@s.text name='manage.mapping.source.help'/></p>

                    <div class="row">
                        <div class="col-sm-6">
                            <@selectList name="source" options=resource.sources objValue="name" objTitle="name" i18nkey="manage.mapping.source" />
                        </div>
                    </div>
                </div>
            </div>
        </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
