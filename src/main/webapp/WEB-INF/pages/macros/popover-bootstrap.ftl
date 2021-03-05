<#ftl output_format="HTML">
<#macro popoverPropertyWarning propertyName>
    <span data-bs-container="body" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="<@s.text name="${propertyName}"/>">
        <i class="bi bi-exclamation-triangle-fill text-warning"></i>
    </span>
</#macro>

<#macro popoverPropertyInfo propertyName>
    <span data-bs-container="body" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="<@s.text name="${propertyName}"/>">
        <i class="bi bi-info-circle text-success"></i>
    </span>
</#macro>

<#macro popoverTextWarning text>
    <span data-bs-container="body" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="${text}">
        <i class="bi bi-exclamation-triangle-fill text-warning"></i>
    </span>
</#macro>

<#macro popoverTextInfo text>
    <span data-bs-container="body" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="${text}">
        <i class="bi bi-info-circle text-success"></i>
    </span>
</#macro>
