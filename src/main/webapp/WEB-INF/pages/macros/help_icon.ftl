<#ftl output_format="HTML">
<#if help?has_content>
    <span data-bs-container="body" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="<#include '/WEB-INF/pages/macros/help_icon_popover.ftl'>">
        <i class="bi bi-info-circle text-gbif-primary"></i>
    </span>
</#if>
