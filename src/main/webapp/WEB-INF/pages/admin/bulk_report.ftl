<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.PublishingStatusAction" -->
<#if runningPublications?has_content>
    <div id="publishingStatusRunning" style="">
        <#list runningPublications?keys as shortname>
        <div class="report running">
            <h5 class="fs-regular"><a href="${baseURL}/manage/resource?r=${shortname}">${shortname}</a></h5>
            <div class="fs-smaller">
                <div class="small">
                    <@s.text name="admin.config.publish.status"/>: ${(runningPublications[shortname].state)!?no_esc}
                </div>

                <div class="small">
                    <@s.text name="admin.config.publish.time"/>: ${.now?time?string}
                </div>

                <div class="small">
                    <@s.text name="admin.config.publish.result"/>: <@s.text name="admin.config.publish.inProgress"/>
                    <div class="inline-spinner" aria-hidden="true">
                        <div class="dot"></div>
                        <div class="dot"></div>
                        <div class="dot"></div>
                        <div class="dot"></div>
                    </div>
                </div>
            </div>
            <ul class="list-unstyled fs-smaller">
                <#list runningPublications[shortname].messages as msg>
                    <li class="${msg.level}"><span class="small">${msg.date?time?string}</span> ${msg.message}</li>
                </#list>
            </ul>
        </div>
        <hr>
        </#list>
    </div>
</#if>
<#if completedPublications?has_content>
    <div id="publishingStatusCompleted">
        <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
            <@s.text name="admin.config.publish.recentlyPublished"/>
        </h5>
        <#list completedPublications?keys as shortname>
            <div class="report completed">
                <h5 class="fs-regular"><a href="${baseURL}/manage/resource?r=${shortname}">${shortname}</a></h5>
                <p class="small">
                    <@s.text name="admin.config.publish.status"/>: ${(completedPublications[shortname].state)!?no_esc}<br>
                    <#if completedPublications[shortname].exception?has_content>
                        <@s.text name="admin.config.publish.result"/>:
                        <i class="bi bi-x-circle-fill text-gbif-danger"></i>
                        <@s.text name="admin.config.publish.failed"/><br>
                    <#else>
                        <@s.text name="admin.config.publish.result"/>:
                        <i class="bi bi-check-circle-fill text-gbif-primary"></i>
                        <@s.text name="admin.config.publish.completed"/><br>
                    </#if>
                    <#if completedPublications[shortname].messages?size gt 0>
                        <a href="#" class="show-log-link" data-resource='${shortname}'><@s.text name="admin.config.publish.showLogs"/></a>
                    </#if>
                </p>
                <ul class="list-unstyled report-messages" style="display: none;">
                    <#list completedPublications[shortname].messages as msg>
                        <li class="${msg.level}"><span class="small">${msg.date?time?string}</span> ${msg.message}</li>
                    </#list>
                </ul>
            </div>
            <hr>
        </#list>
    </div>
</#if>
