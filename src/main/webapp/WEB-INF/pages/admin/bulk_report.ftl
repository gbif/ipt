<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.PublishingStatusAction" -->
<#-- The running or last bulk publication: included in bulk_publication.ftl and polled from there while it runs -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>

<#-- worst first; running at the top while it runs -->
<#assign order = (run.order)![]/>
<#assign known_steps = ["STARTED", "DATAFILES", "DATARESOURCES", "METADATA", "VALIDATING", "BUNDLING"]/>
<#assign css = {"RUNNING": "run", "WAITING": "wait", "PUBLISHED": "ok", "NOT_CHANGED": "unch", "RECORDS_DROPPED": "drop", "FAILED": "fail", "CANCELLED": "stop", "SKIPPED": "skip"}/>

<#-- finished results with their counts; the separator is text, so the copied report reads well too -->
<#macro summary>
    <#list order?filter(st -> st != "RUNNING" && st != "WAITING" && (run.count(st) > 0)) as st><span><@s.text name="admin.bulk.state.${st}"/>: ${run.count(st)}</span><#sep> · </#sep></#list><#t>
</#macro>

<#-- the version left current when a resource wasn't published -->
<#macro kept item>
    <#if item.previousVersion?has_content> <span class="bp-kept">· <@s.text name="admin.bulk.kept"><@s.param>${item.previousVersion}</@s.param></@s.text></span></#if><#t>
</#macro>

<#macro result item>
    <#assign st = item.state?string/>
    <div class="bp-res ${css[st]}">
        <#switch st>
            <#case "WAITING">
                <span class="bp-h"><@icon name="radio_button_unchecked"/> <@s.text name="admin.bulk.state.WAITING"/></span>
                <span class="bp-d"><@s.text name="admin.bulk.waiting.help"/></span>
                <#break>
            <#case "RUNNING">
                <#assign step = (item.step!"")?replace("ARCHIVING", "BUNDLING")/>
                <span class="bp-h"><@icon name="autorenew" class="bp-spin"/> <@s.text name="admin.bulk.publishing"><@s.param>${item.nextVersion}</@s.param></@s.text></span>
                <span class="bp-d"><#if known_steps?seq_contains(step)><b><@s.text name="manage.report.step.${step}"/></b><#if item.stateText?has_content> · </#if></#if>${(item.stateText!"")?no_esc}</span>
                <#break>
            <#case "PUBLISHED">
                <span class="bp-h"><@icon name="check_circle"/> <@s.text name="admin.bulk.published"><@s.param>${item.nextVersion}</@s.param></@s.text></span>
                <#if item.format != "metadata" && item.records??><span class="bp-d"><@s.text name="manage.overview.publication.records"/>: ${item.records?string.number}</span></#if>
                <#break>
            <#case "NOT_CHANGED">
                <span class="bp-h"><@icon name="info"/> <@s.text name="admin.bulk.state.NOT_CHANGED"/><@kept item/></span>
                <span class="bp-d"><@s.text name="admin.bulk.notChanged.help"/></span>
                <#break>
            <#case "RECORDS_DROPPED">
                <span class="bp-h"><@icon name="warning"/> <@s.text name="admin.bulk.state.RECORDS_DROPPED"/><@kept item/></span>
                <span class="bp-d"><@s.text name="admin.bulk.recordsDropped.help"><@s.param>${item.recordsDropThreshold}</@s.param></@s.text></span>
                <#break>
            <#case "FAILED">
                <span class="bp-h"><@icon name="error"/> <@s.text name="admin.bulk.state.FAILED"/><@kept item/></span>
                <#if item.error?has_content><span class="bp-d bp-err">${item.error}</span></#if>
                <#break>
            <#case "CANCELLED">
                <span class="bp-h"><@icon name="cancel"/> <@s.text name="admin.bulk.state.CANCELLED"/><@kept item/></span>
                <#break>
            <#case "SKIPPED">
                <span class="bp-h"><@icon name="cancel"/> <@s.text name="admin.bulk.state.SKIPPED"/></span>
                <#if item.notReady??><span class="bp-d"><@s.text name="admin.bulk.notReady.${item.notReady}"/></span></#if>
                <#break>
        </#switch>
    </div>
</#macro>

<#if run??>
    <#assign total = run.items?size/>
    <#assign problems = run.count("FAILED") + run.count("RECORDS_DROPPED") + run.count("SKIPPED") + run.count("CANCELLED")/>
    <#-- only one page of results is rendered; the filter and the page come back with each poll -->
    <div id="bp-run-block" data-finished="${runPage.finished?c}" data-matched="${runPage.matched?c}" data-page="${runPage.page?c}" data-size="${runPage.size?c}" data-filter="${runPage.filter}">
        <#if runPage.finished>
            <div class="bp-banner ${(problems > 0)?then('warn', 'ok')}" role="status">
                <@icon name=(problems > 0)?then("warning", "check_circle") class="bp-banner-icon"/>
                <div class="bp-grow">
                    <b><@s.text name="admin.bulk.finished"><@s.param>${runPage.ended?time?string["HH:mm"]}</@s.param></@s.text></b>
                    <span class="bp-sum"><@summary/></span>
                    <#if (problems > 0)><div class="bp-banner-text"><@s.text name="admin.bulk.finished.problems"/></div></#if>
                </div>
                <#if (run.count("FAILED") > 0)>
                    <form action="${baseURL}/admin/publishAll.do" method="post" class="m-0">
                        <input type="hidden" name="publishMode" value="SELECTED">
                        <#list run.items?filter(i -> i.state?string == "FAILED") as i><input type="hidden" name="selectedResources" value="${i.shortname}"></#list>
                        <button type="submit" class="btn btn-sm btn-outline-secondary"><@icon name="autorenew"/> <@s.text name="admin.bulk.retry"/>: ${run.count("FAILED")}</button>
                    </form>
                </#if>
            </div>
        <#else>
            <div class="bp-banner info" role="status">
                <@icon name="info" class="bp-banner-icon"/>
                <div class="bp-grow"><@s.text name="admin.bulk.running.help"/></div>
            </div>
        </#if>

        <div class="bp-runhead">
            <div class="bp-runtop">
                <h2><#if runPage.finished><@s.text name="admin.bulk.run.finished"/><#else><@s.text name="admin.bulk.run.running"/></#if></h2>
                <span class="pub-meta">
                    <@s.text name="manage.report.publication.started"/> ${run.started?datetime?string["d MMMM yyyy HH:mm"]}<#if run.startedBy?has_content> · ${run.startedBy}</#if>
                    · <@s.text name="admin.bulk.scope.${run.scope}"/>
                </span>
                <span class="pub-sp"></span>
                <#if runPage.finished><button type="button" class="btn btn-sm btn-outline-secondary" id="bp-copy" data-done="${action.getText('admin.bulk.copied')}"><@icon name="content_copy"/> <@s.text name="admin.bulk.copy"/></button></#if>
            </div>
            <div class="bp-prog" aria-hidden="true">
                <#list ["PUBLISHED", "NOT_CHANGED", "RECORDS_DROPPED", "FAILED", "CANCELLED", "SKIPPED", "RUNNING"] as st>
                    <#if (run.count(st) > 0)><span class="k-${css[st]}" style="width: ${(run.count(st) * 100 / total)?c}%"></span></#if>
                </#list>
            </div>
            <div class="bp-legend" role="group" aria-label="${action.getText('admin.bulk.show')}">
                <button type="button" data-rchip=""<#if !runPage.filter?has_content> class="on"</#if>><@s.text name="admin.bulk.chip.all"/>: <b>${total}</b> · <@s.text name="admin.bulk.done"/>: <b>${run.finishedCount}</b></button>
                <#list order as st>
                    <#if (run.count(st) > 0)><button type="button" data-rchip="${st}"<#if runPage.filter == st> class="on"</#if>><i class="k-${css[st]}"></i><@s.text name="admin.bulk.state.${st}"/>: <b>${run.count(st)}</b></button></#if>
                </#list>
            </div>
        </div>

        <table class="ov-table bp-table bp-runtable">
            <thead>
            <tr>
                <th><@s.text name="admin.bulk.col.resource"/></th>
                <th><@s.text name="admin.bulk.col.result"/></th>
                <th class="bp-hide-md"><@s.text name="admin.bulk.col.visibility"/></th>
                <th><span class="visually-hidden"><@s.text name="admin.bulk.col.actions"/></span></th>
            </tr>
            </thead>
            <tbody>
            <#compress>
            <#list runPage.items as item>
                    <#assign st = item.state?string/>
                    <#assign logOpen = openLogs?seq_contains(item.shortname)/>
                    <tr class="bp-row" data-sn="${item.shortname}" data-state="${action.getText('admin.bulk.state.' + st)}"
                        data-version="<#if st == "PUBLISHED">${item.nextVersion}<#else>${item.previousVersion!}</#if>" data-error="${item.error!}">
                        <td class="bp-name">
                            <a class="resource-table-link" href="${baseURL}/manage/resource.do?r=${item.shortname?url('UTF-8')}" target="_blank">${item.title}</a>
                            <span class="bp-sn">${item.shortname}</span>
                        </td>
                        <td>
                            <@result item/>
                            <#if logOpen>
                                <ol class="pub-log-list bp-log">
                                    <#list item.messages as msg>
                                        <li class="${msg.level}"><span class="pub-tm">${msg.date?time?string("HH:mm:ss")}</span><span class="pub-lv">${msg.level}</span><span>${msg.message}</span></li>
                                    <#else>
                                        <li><span></span><span></span><span><@s.text name="admin.bulk.log.empty"/></span></li>
                                    </#list>
                                </ol>
                            </#if>
                        </td>
                        <td class="bp-hide-md">
                            <span class="text-nowrap fs-smaller-2 status-pill status-${item.status?string?lower_case}">
                                <@icon name=(item.status?string == "PUBLIC" || item.status?string == "PRIVATE")?then("radio_button_unchecked", "circle")/><span><@s.text name="manage.home.visible.${item.status?string?lower_case}"/></span>
                            </span>
                        </td>
                        <td>
                            <div class="bp-acts">
                                <#if item.messages?has_content>
                                    <button type="button" class="ex-act" data-log="${item.shortname}" aria-expanded="${logOpen?c}"><@s.text name="admin.bulk.log"/></button>
                                </#if>
                            </div>
                        </td>
                    </tr>
            </#list>
            </#compress>
            </tbody>
        </table>
        <nav class="bp-pager" id="bp-run-pager" aria-label="${action.getText('portal.home.pages')}"></nav>
    </div>
</#if>
