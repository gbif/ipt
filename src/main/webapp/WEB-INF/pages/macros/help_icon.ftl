<#ftl output_format="HTML">
<#if help?has_content>
    <a tabindex="0" role="button"
       class="popover-link"
       data-bs-toggle="popover"
       data-bs-trigger="focus"
       data-bs-html="true"
       data-bs-content="<#include '/WEB-INF/pages/macros/help_icon_popover.ftl'>">
        <i class="bi bi-info-circle text-gbif-primary px-1"></i>
    </a>
</#if>
