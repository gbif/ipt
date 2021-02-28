<#ftl output_format="HTML">
<label for="${name}" class="form-label">
    <#if i18nkey=="">
        <@s.text name="${name}"/>
    <#else>
        <@s.text name="${i18nkey}"/>
    </#if>
    <#if requiredField>&#42;</#if>
</label>
<#if errorfield=="">
    <@s.fielderror cssClass="invalid-feedback list-unstyled field-error" fieldName="${name}"/>
<#else>
    <@s.fielderror cssClass="invalid-feedback list-unstyled field-error" fieldName="${errorfield}"/>
</#if>
<#--
	something doesnt work right when calling macros inside macro definitions
	thats why we use this include instead to keep the help code in one place
 -->
<#include "/WEB-INF/pages/macros/help_icon-bootstrap.ftl">
