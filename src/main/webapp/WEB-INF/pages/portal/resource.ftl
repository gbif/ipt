<#if resource.isDataPackage()>
    <#if resource.coreType?has_content && resource.coreType == "col-dp">
        <#include "/WEB-INF/pages/portal/resource_col_dp.ftl">
    <#else>
        <#include "/WEB-INF/pages/portal/resource_dp.ftl">
    </#if>
<#else>
    <#include "/WEB-INF/pages/portal/resource_new.ftl">
</#if>
