[#ftl]
[#assign interfaceLanguges = {
"en": "English",
"fr": "Française",
"es": "Español",
"zh": "繁體中文",
"pt": "Português",
"ja": "日本語",
"ru": "Русский"
}]

<ul class="navbar-nav">
    [#if !requestURL?has_content]
        [#assign requrl = baseURL + "?request_locale="]
    [#elseif requestURL?contains("?")]
        [#assign requrl = requestURL + "&request_locale="]
    [#else]
        [#assign requrl = requestURL + "?request_locale="]
    [/#if]
    <!-- add more languages as translations become available. -->

    <li class="nav-item dropdown">
        <a class="nav-link" href="#" id="navbarDarkDropdownMenuLink" role="button" data-bs-toggle="dropdown" aria-expanded="false">
            ${interfaceLanguges[localeLanguage]}
        </a>
        <ul class="dropdown-menu dropdown-menu-dark text-light" aria-labelledby="navbarDarkDropdownMenuLink">
            [#list interfaceLanguges as lang, name]
                [#if lang != localeLanguage]
                    <li><a class="dropdown-item" href="${requrl}${lang}">${name}</a></li>
                [/#if]
            [/#list]
        </ul>
    </li>
</ul>
