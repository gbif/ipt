[#ftl]
<a href="#"><img src="${baseURL}/images/flags/flag_${localeLanguage}.png"/></a>
<ul id="languages">
[#if !requestURL?has_content]
    [#assign requrl = baseURL + "?request_locale="]
[#elseif requestURL?contains("?")]
    [#assign requrl = requestURL + "&request_locale="]
[#else]
    [#assign requrl = requestURL + "?request_locale="]
[/#if]
    <!-- add more languages as translations become available. -->
[#list ["en","fr","es", "zh", "pt", "ja", "ru", "fa"] as lang]
    [#if lang != localeLanguage]
        <li><a href="${requrl}${lang}"><img src="${baseURL}/images/flags/flag_${lang}.png"/></a></li>
    [/#if]
[/#list]
</ul>