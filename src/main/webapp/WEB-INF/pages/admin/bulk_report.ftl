<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.PublishingStatusAction" -->
<#if runningPublications?has_content>
    <div id="publishingStatusRunning" style="">
        <#list runningPublications?keys as shortname>
        <div class="report">
            <h5 class="fs-regular"><a href="${baseURL}/manage/resource?r=${shortname}">${shortname}</a></h5>
            <p class="fs-smaller">
                <strong><@s.text name="admin.config.publish.status"/>:</strong> ${(runningPublications[shortname].state)!?no_esc}<br>
                <strong><@s.text name="admin.config.publish.time"/>:</strong> ${.now?time?string}<br>
                <strong><@s.text name="admin.config.publish.result"/>:</strong>
                <i class="bi bi-hourglass-split text-gbif-header"></i><@s.text name="admin.config.publish.inProgress"/>
            </p>
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
        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400"><@s.text name="admin.config.publish.recentlyPublished"/></h4>
        <#list completedPublications?keys as shortname>
            <div class="report">
                <h5 class="fs-regular"><a href="${baseURL}/manage/resource?r=${shortname}">${shortname}</a></h5>
                <p class="fs-smaller">
                    <strong><@s.text name="admin.config.publish.status"/>:</strong> ${(completedPublications[shortname].state)!?no_esc}<br>
                    <#if completedPublications[shortname].exception?has_content>
                        <strong><@s.text name="admin.config.publish.result"/>:</strong>
                        <i class="bi bi-x-circle-fill text-gbif-danger"></i>
                        <@s.text name="admin.config.publish.failed"/><br>
                    <#else>
                        <strong><@s.text name="admin.config.publish.result"/>:</strong>
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
