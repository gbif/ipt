<#setting url_escaping_charset="UTF-8">

<h5 class="text-gbif-header-2 fw-400">
    <@s.text name='manage.report.title'/>
</h5>

<#attempt>
    <#if report.messages?has_content>
        <#list report.messages as message>
            <#assign publicationStartTimestamp = message.timestamp />
            <#break>
        </#list>
    </#if>

    <#assign elapsedSec = (now?long - publicationStartTimestamp?long) / 1000>
    <#assign hours   = (elapsedSec / 3600)?floor>
    <#assign minutes = ((elapsedSec % 3600) / 60)?floor>
    <#assign seconds = (elapsedSec % 60)>

    <p>
        <span class="small">
            <@s.text name="manage.report.publication.started"/>: ${publicationStartTimestamp?number_to_datetime?string.full}
        </span><br>
        <span class="small">
            <@s.text name="manage.report.publication.time"/>: ${hours?string["00"]}:${minutes?string["00"]}:${seconds?string["00"]}
        </span>
    </p>
<#recover>
    <p>
        <span class="small">${now?datetime?string.full}</span><br>
    </p>
</#attempt>

<#if report??>

    <div<#if report.completed> class="completed"</#if>>

        <#if report.completed>
            <#if !report.hasException() >
                <div class="alert alert-success" role="alert">
                    ${report.state?no_esc}
                </div>
            <#else>
                <div class="alert alert-danger" role="alert">
                    ${report.state?no_esc}
                </div>
            </#if>

            <p>
                <@s.text name='manage.report.continueTo'><@s.param>${resource.shortname}</@s.param></@s.text>
                <#if resource.status=="REGISTERED" && resource.key??>
                    <@s.text name="manage.report.gbif"><@s.param><a type="button" href="${cfg.portalUrl}/dataset/${resource.key!}">GBIF.org</a></@s.param></@s.text>
                </#if>
            </p>
            <p>
                <@s.text name='portal.publication.download.log'/> <a target="_blank" href="${baseURL}/publicationlog.do?r=${resource.shortname}"><@s.text name='portal.publication.log'/></a>
            </p>
        <#else>
            <p>
                <@s.text name="manage.locked"><@s.param>${baseURL}/manage/cancel.do?id=${resource.shortname}</@s.param></@s.text>
            </p>
            <div class="alert alert-warning" role="alert">
                ${report.state?no_esc}
            </div>
            <p>
                <a href="cancel.do?r=${resource.shortname}"><@s.text name="button.cancel"/></a> <@s.text name="manage.overview.publishing"/>.
            </p>
        </#if>

        <h5 class="text-gbif-header-2 fw-400">
            <@s.text name='manage.report.logMessage'/>
        </h5>
        <ul class="list-unstyled">
            <#list report.messages as msg>
                <li class="${msg.level}"><span class="small">${msg.date?time?string}</span> ${msg.message}</li>
            </#list>
        </ul>

        <#if cfg.debug() && report.hasException()>
            <br/>
            <h5 class="text-gbif-header-2 fw-400">
                <@s.text name='manage.report.exception'/>
            </h5>
            <ul class="simple">
                <#list report.exceptionStacktrace as msg>
                    <li>${msg}</li>
                </#list>
            </ul>
        </#if>

    </div>

<#else>
    <h5 class="text-gbif-header fw-400">
        <@s.text name='manage.report.finished'/>
    </h5>
    <#if (resource.shortname)?has_content>
    <p>
        <@s.text name='manage.report.continueTo'><@s.param>${resource.shortname}</@s.param></@s.text>
    </p>
    </#if>
</#if>
