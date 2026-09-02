<#setting url_escaping_charset="UTF-8">

<#assign isDwcDp=resource.isDwcDp()/>

<#if isDwcDp>
    <#if report.state == "Not started yet">
        <#assign currentStep = "WAITING">
    <#elseif report.state == "Starting Data Package generation">
        <#assign currentStep = "STARTED">
    <#elseif report.state?starts_with("Processing record")>
        <#assign currentStep = "DATARESOURCES">
    <#elseif report.state == "Creating metadata files">
        <#assign currentStep = "METADATA">
    <#elseif report.state == "Compressing Data Package (archive)">
        <#assign currentStep = "BUNDLING">
    <#elseif report.state == "Data Package generated!">
        <#assign currentStep = "COMPLETED">
    <#elseif report.state == "Validating Data Package">
        <#assign currentStep = "VALIDATING">
    <#elseif report.state == "Archiving version of data package">
        <#assign currentStep = "ARCHIVING">
    <#else>
        <#assign currentStep = "COMPLETED">
    </#if>

    <#assign steps = [
    {"id":"WAITING",       "label":"Waiting"},
    {"id":"STARTED",       "label":"Started"},
    {"id":"METADATA",      "label":"Metadata"},
    {"id":"DATARESOURCES", "label":"Resources"},
    {"id":"BUNDLING",      "label":"Bundling"},
    {"id":"VALIDATING",    "label":"Validating"},
    {"id":"ARCHIVING",     "label":"Archiving"},
    {"id":"COMPLETED",     "label":"Completed"}
    ]>

    <#assign stateOrder = {
    "WAITING":0,
    "STARTED":1,
    "METADATA":2,
    "DATARESOURCES":3,
    "BUNDLING":4,
    "VALIDATING":5,
    "ARCHIVING":6,
    "COMPLETED":7,
    "FAILED":7
    }>

    <#assign failedIndex = -1>
    <#if currentStep == "COMPLETED" && report.step?has_content>
        <#assign failedIndex = stateOrder[report.step]>
    </#if>

    <div id="state-stepper" class="px-3 pb-3 border rounded-2 publication-stepper-wrapper">
        <div class="publication-box-root">
            <ol class="publication-stepper-root publication-stepper-horizontal publication-stepper-label-horizontal">
                <#list steps as step>
                    <#assign stepIndex = stateOrder[step.id]>
                    <#assign currentIndex = stateOrder[currentStep]>

                    <#if failedIndex != -1>
                        <#if stepIndex == failedIndex>
                            <#assign css = "error">
                        <#elseif stepIndex < failedIndex>
                            <#assign css = "completed">
                        <#else>
                            <#assign css = "disabled">
                        </#if>
                    <#else>
                        <#if stepIndex < currentIndex>
                            <#assign css = "completed">
                        <#elseif step.id == currentStep>
                            <#assign css = "active">
                        <#else>
                            <#assign css = "disabled">
                        </#if>
                    </#if>

                    <li class="publication-step-root publication-step-horizontal publication-step-label ${css}">
                        <#if step_index != 0>
                            <div class="publication-StepConnector-root publication-StepConnector-horizontal publication-StepConnector-label active">
                                <span class="publication-StepConnector-line"></span>
                            </div>
                        </#if>
                        <span class="publication-stepLabel-root publication-StepLabel-horizontal publication-StepLabel-label">
                            <#if css=="completed" || (css=="completed" && step_index==7)>
                                <span class="publication-StepLabel-iconContainer completed publication-StepLabel-label">
                                    <svg class="publication-SvgIcon-root publication-SvgIcon-fontSizeMedium publication-StepIcon-root completed"
                                         focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                        <path d="M12 0a12 12 0 1 0 0 24 12 12 0 0 0 0-24zm-2 17l-5-5 1.4-1.4 3.6 3.6 7.6-7.6L19 8l-9 9z"></path>
                                    </svg>
                                </span>
                            <#elseif css=="error">
                                <span class="publication-StepLabel-iconContainer active publication-StepLabel-label">
                                    <svg class="publication-SvgIcon-root publication-SvgIcon-fontSizeMedium publication-StepIcon-root active error"
                                         focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                        <path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"></path>
                                    </svg>
                                </span>
                            <#elseif css=="disabled">
                                <span class="publication-StepLabel-iconContainer disabled publication-StepLabel-label">
                                    <svg class="publication-SvgIcon-root publication-SvgIcon-fontSizeMedium publication-StepIcon-root disabled"
                                         focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2m0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8"></path>
                                    </svg>
                                </span>
                            <#else>
                                <span class="publication-StepLabel-iconContainer active publication-StepLabel-label">
                                    <svg class="publication-SvgIcon-root publication-SvgIcon-fontSizeMedium publication-StepIcon-root active"
                                         focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                        <path d="M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5m0-5C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2m0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8"></path>
                                    </svg>
                                </span>
                            </#if>
                            <span class="publication-StepLabel-labelContainer publication-StepLabel-label">
                                <span class="publication-StepLabel-label ${css}">${step.label}</span>
                            </span>
                        </span>
                    </li>

                </#list>
            </ol>
        </div>
    </div>
</#if>

<h5 class="text-gbif-header-2 fw-400 mt-4">
    <@s.text name='manage.report.title'/>
</h5>

<#attempt>
    <#if report.messages?has_content>
        <#list report.messages as message>
            <#assign publicationStartTimestamp = message.timestamp />
            <#break>
        </#list>
    </#if>

    <div class="mb-2">
        <#if publicationStartTimestamp??>
            <#assign elapsedSec = (now?long - publicationStartTimestamp?long) / 1000>
            <#assign hours   = (elapsedSec / 3600)?floor>
            <#assign minutes = ((elapsedSec % 3600) / 60)?floor>
            <#assign seconds = (elapsedSec % 60)>
            <div class="small">
                <@s.text name="manage.report.publication.started"/>
                : ${publicationStartTimestamp?number_to_datetime?string.full}
            </div>
            <div class="small">
                <@s.text name="manage.report.publication.time"/>: ${hours?string["00"]}:${minutes?string["00"]}
                :${seconds?string["00"]}
            </div>
        </#if>
        <div class="small">
            <@s.text name="manage.report.publication.status"/>:
            <#if report.hasException()>
                <i class="bi bi-x-circle-fill text-gbif-danger"></i>
                <@s.text name="admin.config.publish.failed"/>
            <#elseif report.completed>
                <i class="bi bi-check-circle-fill text-gbif-primary"></i>
                <@s.text name="admin.config.publish.completed"/>
            <#else>
                <@s.text name="admin.config.publish.inProgress"/>
                <div class="inline-spinner" aria-hidden="true">
                    <div class="dot"></div>
                    <div class="dot"></div>
                    <div class="dot"></div>
                    <div class="dot"></div>
                </div>
            </#if>
        </div>
    </div>
    <#recover>
        <p>
            <span class="small">${now?datetime?string.full}</span><br>
        </p>
</#attempt>

<#if report??>

    <div id="report-block" <#if report.completed> class="completed"</#if>>

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
                    <@s.text name="manage.report.gbif"><@s.param><a type="button"
                                                                    href="${cfg.portalUrl}/dataset/${resource.key!}">
                            GBIF.org</a></@s.param></@s.text>
                </#if>
            </p>
            <p>
                <@s.text name='portal.publication.download.log'/> <a target="_blank"
                                                                     href="${baseURL}/publicationlog.do?r=${resource.shortname}"><@s.text name='portal.publication.log'/></a>
            </p>
            <#if isDwcDp>
                <p>
                    <@s.text name='portal.publication.validation.report.info'><@s.param><a target="_blank"
                                                                                           href="${baseURL}/manage/validationReport.do?r=${resource.shortname}"><@s.text name='portal.publication.validation.report'/></a></@s.param></@s.text>
                </p>
            </#if>
        <#else>
            <p>
                <@s.text name="manage.locked"><@s.param>${baseURL}/manage/cancel.do?r=${resource.shortname}</@s.param></@s.text>
            </p>
            <div class="alert alert-warning" role="alert">
                ${report.state?no_esc}
            </div>
            <p>
                <a href="cancel.do?r=${resource.shortname}"><@s.text name="button.cancel"/></a> <@s.text name="manage.overview.publishing"/>
                .
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
