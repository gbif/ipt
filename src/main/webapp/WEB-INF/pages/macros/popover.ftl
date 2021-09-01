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

<#macro popoverPropertyInfo propertyName>
    <a tabindex="0" role="button"
       class="popover-link"
       data-bs-toggle="popover"
       data-bs-trigger="focus"
       data-bs-html="true"
       data-bs-content="<@s.text name="${propertyName}" escapeHtml=true/>">
        <i class="bi bi-info-circle text-gbif-primary"></i>
    </a>
</#macro>

<#macro popoverTextWarning text>
    <a tabindex="0" role="button"
       class="popover-link"
       data-bs-toggle="popover"
       data-bs-trigger="focus"
       data-bs-html="true"
       data-bs-content="${text}">
        <i class="bi bi-exclamation-triangle-fill text-warning"></i>
    </a>
</#macro>

<#macro popoverTextInfo text>
    <a tabindex="0" role="button"
       class="popover-link"
       data-bs-toggle="popover"
       data-bs-trigger="focus"
       data-bs-html="true"
       data-bs-content="${text}">
        <i class="bi bi-info-circle text-gbif-primary"></i>
    </a>
</#macro>
