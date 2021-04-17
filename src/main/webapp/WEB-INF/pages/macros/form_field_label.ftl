<#ftl output_format="HTML">
<label for="${name}" class="form-label">
    <#if i18nkey=="">
        <@s.text name="${name}"/>
    <#else>
        <@s.text name="${i18nkey}"/>
    </#if>
    <#if requiredField>&#42;</#if>
    <#include '/WEB-INF/pages/macros/help_icon.ftl'>
</label>
