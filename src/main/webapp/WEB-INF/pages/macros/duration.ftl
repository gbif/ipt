<#-- how long something took, e.g. "12 s" or "3 min 5 s" -->
<#macro duration ms>
    <#local sec = (ms / 1000)?floor>
    <#if sec lt 1><@s.text name="manage.report.duration.underSecond"/><#t>
    <#elseif sec lt 60><@s.text name="manage.report.duration.s"><@s.param>${sec?c}</@s.param></@s.text><#t>
    <#elseif sec lt 3600><@s.text name="manage.report.duration.m"><@s.param>${(sec / 60)?floor?c}</@s.param><@s.param>${(sec % 60)?c}</@s.param></@s.text><#t>
    <#else><@s.text name="manage.report.duration.h"><@s.param>${(sec / 3600)?floor?c}</@s.param><@s.param>${((sec % 3600) / 60)?floor?c}</@s.param></@s.text><#t>
    </#if>
</#macro>
