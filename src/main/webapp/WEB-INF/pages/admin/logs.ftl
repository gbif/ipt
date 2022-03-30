<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.home.manageLogs"/></title>
<#assign currentMenu = "admin"/>
<script>
    $(document).ready(function(){
        $.get("${baseURL}/admin/logfile.do", {log:"admin"}, function(data){
            $("#logs").text(data);
        });
    });
</script>
<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="container-fluid bg-body border-bottom">
    <div class="container">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container my-3 p-3">
        <div class="text-center">
            <div class="text-uppercase fw-bold fs-smaller-2">
                <span><@s.text name="menu.admin"/></span>
            </div>

            <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                <@s.text name="admin.home.manageLogs"/>
            </h1>

            <div class="mt-2">
                <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button">
                    <@s.text name="button.cancel"/>
                </a>
            </div>
        </div>
    </div>
</div>

<main class="container">
    <div class="my-3 p-3">
        <p class="mx-md-4 mx-2">
            <strong><@s.text name="admin.logs.warn"/></strong>
        </p>
        <p class="mx-md-4 mx-2">
            <@s.text name="admin.logs.download"><@s.param>logfile.do?log=debug</@s.param></@s.text>
        </p>

        <pre id="logs" class="mx-md-4 mx-2"></pre>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
