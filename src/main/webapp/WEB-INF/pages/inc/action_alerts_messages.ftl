[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/action_alerts_list.ftl"]

[#if actionMessages?size>0]
    [#list actionMessages as message]
        <div class="alert alert-success alert-dismissible fade show d-flex" role="alert">
            <div class="me-3">
                [@alertIcon "check_circle" "bi-check2-circle alert-green-2"/]
            </div>
            <div class="overflow-x-hidden pt-1">
                [#if message?has_content]
                <span>[@message?interpret /]</span>
                [/#if]
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    [/#list]
[/#if]
