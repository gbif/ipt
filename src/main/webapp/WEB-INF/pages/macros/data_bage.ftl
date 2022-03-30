<#ftl output_format="HTML">

<#macro dataBage fieldType>
    <#if fieldType == "string">
        <span class="badge rounded-pill bg-blue">${fieldType}</span>
    <#elseif fieldType == "uri">
        <span class="badge rounded-pill bg-red">${fieldType}</span>
    <#elseif fieldType == "number">
        <span class="badge rounded-pill bg-teal">${fieldType}</span>
    <#elseif fieldType == "integer">
        <span class="badge rounded-pill bg-cyan">${fieldType}</span>
    <#elseif fieldType == "decimal">
        <span class="badge rounded-pill bg-light-blue">${fieldType}</span>
    <#elseif fieldType == "object">
        <span class="badge rounded-pill bg-purple">${fieldType}</span>
    <#elseif fieldType == "boolean">
        <span class="badge rounded-pill bg-indigo">${fieldType}</span>
    <#elseif fieldType == "datetime">
        <span class="badge rounded-pill bg-orange">${fieldType}</span>
    <#elseif fieldType == "date">
        <span class="badge rounded-pill bg-amber">${fieldType}</span>
    <#elseif fieldType == "year">
        <span class="badge rounded-pill bg-yellow">${fieldType}</span>
    <#else>
        ${fieldType}
    </#if>
</#macro>
