[#ftl]
[#assign languages = {
    "en": "English",
    "fr": "Française",
    "es": "Español",
    "zh": "繁體中文",
    "pt": "Português",
    "ja": "日本語",
    "ru": "Русский",
    "fa": "فارسی"
}]
<a href="#">${languages[localeLanguage]}</a>
<ul id="languages">
[#if !requestURL?has_content]
    [#assign requrl = baseURL + "?request_locale="]
[#elseif requestURL?contains("?")]
    [#assign requrl = requestURL + "&request_locale="]
[#else]
    [#assign requrl = requestURL + "?request_locale="]
[/#if]
    <!-- add more languages as translations become available. -->

[#list languages as lang, name]
    [#if lang != localeLanguage]
        <li><a href="${requrl}${lang}">${name}</a></li>
    [/#if]
[/#list]
</ul>
