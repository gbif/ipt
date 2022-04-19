<#escape x as x?html>
<#setting url_escaping_charset="UTF-8">
<#include "/WEB-INF/pages/inc/header.ftl">
<script>
    $(document).ready(function(){
        loadReport();
        var reporter = setInterval(loadReport, 1000);
        function loadReport(){
            $("#report").load("${baseURL}/manage/report.do?r=${resource.shortname}", function() {
                if ($(".completed").length > 0){
                    // stop timer and hide gif
                    clearInterval(reporter);
                }
            });
        }
    });
</script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="container-fluid bg-body border-bottom">
    <div class="container">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container my-3 p-3">
        <div class="text-center">
            <div class="text-uppercase fw-bold fs-smaller-2">
                <span><@s.text name="basic.resource"/></span>
            </div>

            <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                <@s.text name="manage.publication.title"/>
            </h1>

            <div class="text-smaller">
                <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
            </div>
        </div>
    </div>
</div>

<main class="container">
    <div class="my-3 p-3">
        <div id="report" class="mx-md-4 mx-2"></div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
