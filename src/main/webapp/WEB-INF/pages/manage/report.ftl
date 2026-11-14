<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#-- Publishing status, loaded into locked.ftl every second until publishing has ended -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>

<#-- how long something took, e.g. "12 s" or "3 min 5 s" -->
<#macro duration ms>
    <#local sec = (ms / 1000)?floor>
    <#if sec lt 1><@s.text name="manage.report.duration.underSecond"/><#t>
    <#elseif sec lt 60><@s.text name="manage.report.duration.s"><@s.param>${sec?c}</@s.param></@s.text><#t>
    <#elseif sec lt 3600><@s.text name="manage.report.duration.m"><@s.param>${(sec / 60)?floor?c}</@s.param><@s.param>${(sec % 60)?c}</@s.param></@s.text><#t>
    <#else><@s.text name="manage.report.duration.h"><@s.param>${(sec / 3600)?floor?c}</@s.param><@s.param>${((sec % 3600) / 60)?floor?c}</@s.param></@s.text><#t>
    </#if>
</#macro>

<#if report??>
    <#assign version = resource.metadataVersion.toPlainString()>

    <#-- steps in the order the generators run them: data packages write the metadata before the data, DwC-A after it.
         DwC-DP's ARCHIVING only moves the zip into place, which the others do in BUNDLING, so it's shown as Bundling -->
    <#if resource.isDataPackage()>
        <#assign steps = ["STARTED", "METADATA", "DATARESOURCES", "VALIDATING", "BUNDLING"]>
    <#else>
        <#assign steps = ["STARTED", "DATAFILES", "METADATA", "VALIDATING", "BUNDLING"]>
    </#if>
    <#assign step = report.step!"">
    <#if step == "ARCHIVING"><#assign step = "BUNDLING"></#if>
    <#-- index of the step running, or failed or cancelled at; past the last one once the generator is done -->
    <#if (report.completed && !report.hasException()) || step == "COMPLETED" || (report.hasException() && !step?has_content)>
        <#assign currentIndex = steps?size>
    <#elseif steps?seq_index_of(step) gte 0>
        <#assign currentIndex = steps?seq_index_of(step)>
    <#else>
        <#assign currentIndex = 0>
    </#if>

    <#-- how long each step took: from its start to the next step's start (ARCHIVING counts as Bundling), or to this
         report for the last one. stepStarts is in the order the steps ran -->
    <#assign stepStarts = report.stepStarts>
    <#assign stepTimes = {}>
    <#list steps as stepId>
        <#if stepStarts.get(stepId)??>
            <#assign aliases = (stepId == "BUNDLING")?then(["BUNDLING", "ARCHIVING"], [stepId])>
            <#assign seen = false>
            <#assign stepEnd = report.timestamp>
            <#list stepStarts?keys as k>
                <#if seen && !aliases?seq_contains(k)>
                    <#assign stepEnd = stepStarts.get(k)>
                    <#break>
                </#if>
                <#if k == stepId><#assign seen = true></#if>
            </#list>
            <#assign stepTimes = stepTimes + {stepId: stepEnd - stepStarts.get(stepId)}>
        </#if>
    </#list>

    <#-- when it started and how long it took (the report's timestamp is when it was made) -->
    <#if report.messages?has_content>
        <#assign startTimestamp = report.messages[0].timestamp>
        <#assign elapsedSec = ((report.timestamp - startTimestamp) / 1000)?floor>
        <#if elapsedSec lt 0><#assign elapsedSec = 0></#if>
        <#assign elapsed = (elapsedSec / 3600)?floor?string["00"] + ":" + ((elapsedSec % 3600) / 60)?floor?string["00"] + ":" + (elapsedSec % 60)?string["00"]>
    </#if>

    <#-- a cancelled run ends with an InterruptedException. A completed run without an error may still have been
         reverted (data not changed, records dropped): only a published one sets lastPublished -->
    <#assign cancelled = report.isCancelled()>
    <#assign failed = report.hasException() && !cancelled>
    <#assign published = report.completed && !report.hasException() && (!startTimestamp?? || (resource.lastPublished?? && resource.lastPublished?long gte startTimestamp))>
    <#assign reverted = report.completed && !report.hasException() && !published>
    <#assign warnings = report.messages?filter(m -> m.level?string == "WARN")?size>
    <#assign errors = report.messages?filter(m -> m.level?string == "ERROR")?size>

    <div id="report-block"<#if report.completed> class="completed"</#if>>
        <div class="pub-head">
            <#-- the outcome; the box under the steps has the details -->
            <h2 class="${published?then('ok', failed?then('bad', (reverted || cancelled)?then('neutral', '')))}">
                <#if published>
                    <@icon name="check_circle"/> <@s.text name="manage.report.published"><@s.param>${version}</@s.param></@s.text>
                <#elseif failed>
                    <@icon name="error"/>
                    <#if step?has_content && steps?seq_index_of(step) gte 0>
                        <@s.text name="manage.report.failedAt"><@s.param><@s.text name="manage.report.step.${step}"/></@s.param></@s.text>
                    <#else>
                        <@s.text name="manage.report.failed"/>
                    </#if>
                <#elseif cancelled>
                    <@icon name="cancel"/> <@s.text name="manage.report.cancelled"/>
                <#elseif reverted>
                    <@icon name="info"/> <@s.text name="manage.report.notPublished"/>
                <#else>
                    <@s.text name="manage.report.publishing"><@s.param>${version}</@s.param></@s.text>
                </#if>
            </h2>
            <#if startTimestamp??>
                <span class="pub-meta">
                    <@s.text name="manage.report.publication.started"/> ${startTimestamp?number_to_datetime?string.medium}
                    · <#if report.completed><@s.text name="manage.report.took"><@s.param>${elapsed}</@s.param></@s.text><#else><@s.text name="manage.report.runningFor"><@s.param>${elapsed}</@s.param></@s.text></#if>
                </span>
            </#if>
        </div>

        <#if steps?has_content>
            <ol class="pub-steps">
                <#list steps as stepId>
                    <#if stepId_index lt currentIndex>
                        <#assign css = "done"><#assign ic = "check_circle">
                    <#elseif stepId_index == currentIndex && failed>
                        <#assign css = "error"><#assign ic = "error">
                    <#elseif stepId_index == currentIndex && cancelled>
                        <#assign css = "stopped"><#assign ic = "cancel">
                    <#elseif stepId_index == currentIndex && !report.completed>
                        <#assign css = "active"><#assign ic = "autorenew">
                    <#else>
                        <#assign css = "todo"><#assign ic = "radio_button_unchecked">
                    </#if>
                    <li class="${css}"<#if css == "active"> aria-current="step"</#if>>
                        <@icon name=ic class="pub-step-icon"/>
                        <span><@s.text name="manage.report.step.${stepId}"/></span>
                        <#if css != "todo" && stepTimes[stepId]??>
                            <span class="pub-step-time"><@duration stepTimes[stepId]/></span>
                        </#if>
                    </li>
                </#list>
            </ol>
        </#if>

        <#if !report.completed>
            <#-- what's being done now, e.g. "Processing record 12000 for data file Occurrence" -->
            <div class="pub-current" role="status">
                <div class="pub-now">${report.state?no_esc}</div>
                <div class="pub-small">
                    <@icon name="lock"/> <@s.text name="manage.report.locked"/>
                </div>
            </div>
        <#elseif published>
            <div class="pub-result ok" role="status">
                <dl class="pub-facts">
                    <#-- the count the overview shows: core records for DwC-A, observations for Camtrap DP, all rows for the
                         other data packages. Metadata-only resources are published without this page -->
                    <div><dt><@s.text name="manage.overview.publication.records"/></dt><dd>${resource.recordsPublished?string.number}</dd></div>
                    <#-- versionHistory[0] is the version just published -->
                    <#if (resource.versionHistory?size gt 1)>
                        <#assign previousVersion = resource.versionHistory[1]>
                        <#assign recordsChange = resource.recordsPublished - previousVersion.recordsPublished>
                        <div><dt><@s.text name="manage.report.recordsChange"><@s.param>${previousVersion.version}</@s.param></@s.text></dt>
                            <dd><#if recordsChange == 0><@s.text name="manage.report.recordsChange.none"/><#else>${(recordsChange gt 0)?then("+", "")}${recordsChange?string.number}</#if></dd></div>
                    </#if>
                    <div><dt><@s.text name="manage.overview.publication.visibleTo"/></dt>
                        <dd><#if resource.status == "PRIVATE"><@s.text name="manage.overview.publication.managersOnly"/><#else><@s.text name="manage.overview.publication.everyone"/></#if></dd></div>
                    <#if resource.isAlreadyAssignedDoi() && (resource.versionHistory[0].doi)??>
                        <div><dt>DOI</dt><dd>${resource.versionHistory[0].doi}</dd></div>
                    </#if>
                </dl>
                <#if warnings gt 0>
                    <p class="pub-warn"><@icon name="warning"/> <@s.text name="manage.report.warnings"><@s.param>${warnings}</@s.param></@s.text></p>
                </#if>
                <div class="pub-acts">
                    <a class="btn btn-sm btn-outline-gbif-primary" href="${baseURL}/resource?r=${resource.shortname}"><@s.text name="manage.overview.viewPage"/></a>
                    <#if resource.status == "REGISTERED" && resource.key??>
                        <a class="btn btn-sm btn-outline-secondary" href="${cfg.portalUrl}/dataset/${resource.key}" target="_blank" rel="noopener"><@s.text name="manage.overview.registration.view"/> <@icon name="open_in_new"/></a>
                    </#if>
                    <a class="btn btn-sm btn-outline-secondary" href="${baseURL}/manage/history.do?r=${resource.shortname}&amp;v=${version}"><@s.text name="manage.report.changeSummary"/></a>
                    <#if resource.isDwcDp() && action.isValidationReportAvailable()>
                        <a class="btn btn-sm btn-outline-secondary" href="${baseURL}/manage/validationReport.do?r=${resource.shortname}"><@s.text name="portal.publication.validation.report"/></a>
                    </#if>
                    <a class="btn btn-sm btn-outline-secondary" href="${baseURL}/manage/resource.do?r=${resource.shortname}"><@s.text name="manage.report.toOverview"/></a>
                </div>
            </div>
        <#else>
            <div class="pub-result ${failed?then('bad', 'neutral')}" role="alert">
                <#if failed>
                    <div class="pub-reason">${report.exceptionMessage!report.state}</div>
                <#elseif reverted>
                    <p>${report.state}</p>
                </#if>
                <p>
                    <#if resource.lastPublishedVersionsVersion??>
                        <@s.text name="manage.report.stillPublished"><@s.param>${resource.lastPublishedVersionsVersion.toPlainString()}</@s.param></@s.text>
                    <#else>
                        <@s.text name="manage.report.nothingPublished"/>
                    </#if>
                    ${" "}<@s.text name="manage.report.editable"/>
                </p>
                <div class="pub-acts">
                    <a class="btn btn-sm btn-outline-gbif-primary" href="${baseURL}/manage/resource.do?r=${resource.shortname}"><@s.text name="manage.report.toOverview"/></a>
                    <#if resource.isDwcDp() && action.isValidationReportAvailable()>
                        <a class="btn btn-sm btn-outline-secondary" href="${baseURL}/manage/validationReport.do?r=${resource.shortname}"><@s.text name="portal.publication.validation.report"/></a>
                    </#if>
                </div>
            </div>
        </#if>

        <div class="pub-log">
            <div class="pub-log-head">
                <h3><@s.text name="manage.report.logMessage"/></h3>
                <span class="pub-small">
                    <@s.text name="manage.report.count"><@s.param>${report.messages?size}</@s.param></@s.text><#if warnings gt 0> · <@s.text name="manage.report.count.warnings"><@s.param>${warnings}</@s.param></@s.text></#if><#if errors gt 0> · <@s.text name="manage.report.count.errors"><@s.param>${errors}</@s.param></@s.text></#if>
                </span>
                <span class="pub-sp"></span>
                <label class="pub-small pub-filter"><input type="checkbox" id="log-only-problems"> <@s.text name="manage.report.onlyProblems"/></label>
                <#if report.completed>
                    <a class="btn btn-sm btn-outline-secondary" href="${baseURL}/publicationlog.do?r=${resource.shortname}" target="_blank"><@icon name="download"/> <@s.text name="portal.publication.log"/></a>
                </#if>
            </div>
            <ol class="pub-log-list" id="log-list">
                <#list report.messages as msg>
                    <li class="${msg.level}"><span class="pub-tm">${msg.date?time?string("HH:mm:ss")}</span><span class="pub-lv">${msg.level}</span><span>${msg.message}</span></li>
                </#list>
            </ol>
            <#if cfg.debug() && report.hasException()>
                <div class="pub-exception">
                    <h3><@s.text name="manage.report.exception"/></h3>
                    <ul class="list-unstyled">
                        <#list report.exceptionStacktrace as line>
                            <li>${line}</li>
                        </#list>
                    </ul>
                </div>
            </#if>
        </div>
    </div>
<#else>
    <#-- no report: nothing was published since the IPT started -->
    <div id="report-block" class="completed">
        <div class="pub-result neutral">
            <p class="mt-0"><@s.text name="manage.report.none"/></p>
            <div class="pub-acts">
                <a class="btn btn-sm btn-outline-gbif-primary" href="${baseURL}/manage/resource.do?r=${resource.shortname}"><@s.text name="manage.report.toOverview"/></a>
            </div>
        </div>
    </div>
</#if>
