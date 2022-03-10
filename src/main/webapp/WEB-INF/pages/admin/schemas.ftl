<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.home.manageSchemas"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>

    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <main class="container">
        <div class="my-3 p-3 border rounded shadow-sm">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header fw-400 text-center">
                <@s.text name="admin.schemas.title"/>
            </h5>

        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
