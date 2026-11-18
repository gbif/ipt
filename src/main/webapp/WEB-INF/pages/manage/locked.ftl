<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="manage.publication.title"/>: ${resource.title!resource.shortname}</title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/publishing.css">
<script>
    $(document).ready(function(){
        // the log filter and scroll position survive the reloads
        var onlyProblems = false;
        var logScroll = null; // null: follow the end of the log

        // the next request is sent a second after the last one answered, so slow answers can't overlap
        loadReport();
        function loadReport() {
            var list = document.getElementById("log-list");
            if (list) {
                var atEnd = list.scrollHeight - list.scrollTop - list.clientHeight < 8;
                logScroll = atEnd ? null : list.scrollTop;
            }
            $("#report").load("${baseURL}/manage/report.do?r=${resource.shortname}", function (response, status, xhr) {
                // the session expired: stop polling and open the overview, which goes to login and returns there
                if (xhr.status === 401) {
                    window.location.href = "${baseURL}/manage/resource.do?r=${resource.shortname}";
                    return;
                }
                if (status === "error") {
                    // the IPT can't be reached (status 0): try again; other errors stop the polling
                    if (xhr.status === 0) {
                        setTimeout(loadReport, 1000);
                    } else {
                        $("#report").prepend($('<div class="alert alert-danger fs-smaller-2" role="alert"></div>')
                            .text('${action.getText("basic.error")?js_string?no_esc} (' + xhr.status + ')'));
                    }
                    return;
                }
                $("#log-only-problems").prop("checked", onlyProblems);
                $("#report .pub-log").toggleClass("only-problems", onlyProblems);
                var list = document.getElementById("log-list");
                if (list) {
                    list.scrollTop = logScroll === null ? list.scrollHeight : logScroll;
                }
                if ($("#report-block.completed").length > 0) {
                    // nothing left to cancel
                    $("#cancel-publishing, #cancel-confirm").prop("hidden", true);
                } else {
                    setTimeout(loadReport, 1000);
                }
            });
        }

        $("#report").on("change", "#log-only-problems", function () {
            onlyProblems = this.checked;
            $("#report .pub-log").toggleClass("only-problems", onlyProblems);
        });

        // "Show warnings" in the result: the log with only warnings and errors
        $("#report").on("click", ".pub-show-warnings", function (e) {
            e.preventDefault();
            onlyProblems = true;
            $("#log-only-problems").prop("checked", true);
            $("#report .pub-log").addClass("only-problems");
            var log = $("#report .pub-log")[0];
            if (log) log.scrollIntoView({behavior: "smooth", block: "start"});
        });

        $("#cancel-publishing").on("click", function () {
            $("#cancel-confirm").prop("hidden", false);
            $("#keep-publishing").trigger("focus");
        });
        $("#keep-publishing").on("click", function () {
            $("#cancel-confirm").prop("hidden", true);
            $("#cancel-publishing").trigger("focus");
        });
    });
</script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3 ov-head ov-head-left">
            <div class="ov-grow">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item ov-crumb-resource"><a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.publishing"/></li>
                        </ol>
                    </nav>
                </div>
                <#-- the resource is in the breadcrumb -->
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="manage.publication.title"/></h1>
            </div>
            <div class="ov-acts">
                <button type="button" id="cancel-publishing" class="btn btn-sm btn-outline-gbif-danger top-button"><@s.text name="manage.report.cancel"/></button>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container">
    <div class="pub-page mb-3 mt-4 pb-3">
        <#-- asked before the POST: cancelling throws away the version being made -->
        <div class="pub-confirm" id="cancel-confirm" role="group" aria-labelledby="cancel-confirm-title" hidden>
            <div class="pub-confirm-head" id="cancel-confirm-title"><@s.text name="manage.report.cancel.title"/></div>
            <div class="pub-confirm-body" id="cancel-confirm-text"><@s.text name="manage.report.cancel.text"/></div>
            <div class="pub-confirm-foot">
                <button type="button" class="btn btn-sm btn-outline-secondary" id="keep-publishing"><@s.text name="manage.report.cancel.keep"/></button>
                <a href="${baseURL}/manage/cancel.do?r=${resource.shortname}" class="btn btn-sm btn-outline-gbif-danger" data-post><@s.text name="manage.report.cancel"/></a>
            </div>
        </div>

        <div id="report"></div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
