<#setting url_escaping_charset="UTF-8">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="manage.publication.title"/>: ${resource.title!resource.shortname}</title>
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

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.publishing"/></li>
                        </ol>
                    </nav>
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
</div>

<main class="container main-content-container">
    <div class="my-3 py-3">
        <div id="report"></div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
