<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.PublishingStatusAction" -->
<#if runningPublications?has_content>
    <div id="publishingStatusRunning" style="">
        <#list runningPublications?keys as shortname>
        <div class="report">
            <h5><a href="${baseURL}/manage/resource?r=${shortname}">${shortname}</a></h5>
            <p>
                <strong>Status:</strong> ${(runningPublications[shortname].state)!?no_esc}<br>
                <strong>Time:</strong> ${.now?time?string}<br>
                <strong>Completed:</strong>⏳ In progress
            </p>
            <ul class="list-unstyled">
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
        <h4>Recently Published</h4>
        <#list completedPublications?keys as shortname>
            <div class="report">
                <h5><a href="${baseURL}/manage/resource?r=${shortname}">${shortname}</a></h5>
                <p>
                    <strong>Status:</strong> ${(completedPublications[shortname].state)!?no_esc}<br>
                    <#if completedPublications[shortname].exception?has_content>
                        <strong>Result:</strong> ❌ Failed<br>
                    <#else>
                        <strong>Result:</strong> ✅ Completed<br>
                    </#if>
                    <#if completedPublications[shortname].messages?size gt 0>
                        <a href="#" class="show-log-link" data-resource='${shortname}'>Show logs</a>
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
