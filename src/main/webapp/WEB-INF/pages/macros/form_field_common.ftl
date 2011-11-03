<label for="${name}"><#if i18nkey==""><@s.text name="${name}"/><#else><@s.text name="${i18nkey}"/></#if></label>
<#if errorfield==""><@s.fielderror cssClass="fielderror" fieldName="${name}"/><#else><@s.fielderror cssClass="fielderror" fieldName="${errorfield}"/></#if>
<#-- 
	something doesnt work right when calling macros inside macro definitions
	thats why we use this include instead to keep the help code in one place
 -->
<#include "/WEB-INF/pages/macros/help_icon.ftl">
