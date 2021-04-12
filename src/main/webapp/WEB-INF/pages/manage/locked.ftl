<#escape x as x?html>
<#setting url_escaping_charset="UTF-8">
<#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
<script type="text/javascript">
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
<#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">

<main class="container">
    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
            ${resource.title!resource.shortname}
        </h5>

        <div id="report" class="mx-md-4 mx-2"></div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
</#escape>
