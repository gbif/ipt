<#if (resource.schemaIdentifier)?has_content>
    <#include "/WEB-INF/pages/portal/resource_dp.ftl">
<#else>
    <#include "/WEB-INF/pages/portal/resource_new.ftl">
</#if>
