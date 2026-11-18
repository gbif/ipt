<#ftl output_format="HTML">
<#macro popoverPropertyWarning propertyName>
    <a tabindex="0" role="button"
       class="popover-link"
       data-bs-toggle="popover"
       data-bs-trigger="focus"
       data-bs-html="true"
       data-bs-content="<@s.text name="${propertyName}" escapeHtml=true/>">
        <i class="bi bi-exclamation-triangle-fill text-warning"></i>
    </a>
</#macro>

<#-- material: the outlined Material info icon (needs macros/icon.ftl) instead of Bootstrap Icons, for pages already redesigned -->
<#macro popoverPropertyInfo propertyName material=false>
    <a tabindex="0" role="button"
       class="popover-link"
       data-bs-toggle="popover"
       data-bs-trigger="focus"
       data-bs-html="true"
       data-bs-content="<@s.text name="${propertyName}" escapeHtml=true/>">
        <#if material><@icon name="info_outlined" class="text-gbif-primary"/><#else><i class="bi bi-info-circle text-gbif-primary"></i></#if>
    </a>
</#macro>

<#macro popoverTextWarning text>
    <a tabindex="0" role="button"
       class="popover-link"
       data-bs-toggle="popover"
       data-bs-trigger="focus"
       data-bs-html="true"
       data-bs-content="${text?markup_string}">
        <i class="bi bi-exclamation-triangle-fill text-warning"></i>
    </a>
</#macro>

<#macro popoverTextInfo text material=false>
    <a tabindex="0" role="button"
       class="popover-link"
       data-bs-toggle="popover"
       data-bs-trigger="focus"
       data-bs-html="true"
       data-bs-content="${text?markup_string}">
        <#if material><@icon name="info_outlined" class="text-gbif-primary"/><#else><i class="bi bi-info-circle text-gbif-primary"></i></#if>
    </a>
</#macro>
