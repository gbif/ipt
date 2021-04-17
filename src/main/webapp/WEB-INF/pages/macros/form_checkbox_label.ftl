<#ftl output_format="HTML">
<label for="${name}" class="form-check-label">
    <#if i18nkey=="">
        <@s.text name="${name}"/>
    <#else>
        <@s.text name="${i18nkey}"/>
    </#if>
    <#if requiredField>&#42;</#if>
</label>
