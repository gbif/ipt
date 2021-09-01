<#ftl output_format='HTML'>
<div class='info'>
    <#if help=='i18n'>
        <#if i18nkey==''>
            <@s.text name='${name}.help' escapeHtml=true/>
        <#else>
            <@s.text name='${i18nkey}.help' escapeHtml=true/>
        </#if>
    <#else>
        <#if help?has_content>
            ${help}
        </#if>
    </#if>
    <#if (helpOptions?? && helpOptions?size>0)>
        <p><@s.text name='help.options'/></p>
        <ol>
            <#list helpOptions?keys as val>
                <li>
                    <a href='#' class='helpOptionLink inputName-${name} inputValue-${val}'>${helpOptions[val]}</a>
                </li>
            </#list>
        </ol>
    </#if>
</div>
