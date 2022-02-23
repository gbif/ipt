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
                };
            });
        }
    });
</script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<main class="container">
    <div class="my-3 p-3 border rounded shadow-sm">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header fw-400 text-center">
            ${resource.title!resource.shortname}
        </h5>

        <div id="report" class="mx-md-4 mx-2"></div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
