<#setting url_escaping_charset="UTF-8">

<#if report??>
    <div id="preview-report">
        <#if report.completed>
            <#if !report.hasException() >
                <div class="alert alert-success" role="alert">
                    <@s.text name='mapping.preview.success'/>
                </div>
            <#else>
                <div class="alert alert-danger" role="alert">
                    <@s.text name='mapping.preview.failed'/>
                </div>
            </#if>
        </#if>

        <h6 class="text-gbif-header">
            <@s.text name='manage.report.logMessage'/>
        </h6>
        <ul class="simple">
            <#list report.messages as msg>
                <li>${msg.message} <span class="small">${msg.date?time?string}</span></li>
            </#list>
        </ul>

        <#if cfg.debug() && report.hasException()>
            <br/>
            <h6 class="text-gbif-header">
                <@s.text name='manage.report.exception'/>: ${report.exceptionMessage!}
            </h6>
            <ul class="simple">
                <#list report.exceptionStacktrace as msg>
                    <li>${msg}</li>
                </#list>
            </ul>
        </#if>
    </div>
</#if>

<div class="table-responsive">
    <table class="simple table table-sm table-borderless text-smaller">
        <tr>
            <#list columns as col><th>${col}</th></#list>
        </tr>

        <#list peek as row><#if row??>
            <tr<#if (row_index % 2) == 0> class="even"</#if>>
                <#list row as col><td>${col!"<em>null</em>"}</td></#list>
            </tr>
        </#if></#list>
    </table>
</div>
