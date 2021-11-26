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
        <a class="nav-link" href="#" id="languageDropdownLink" role="button" data-bs-toggle="dropdown" aria-expanded="false">
            <svg class="gbif-translate-icon" height="20" viewBox="0 0 24 24" width="20" xmlns="http://www.w3.org/2000/svg">
                <path d="M0 0h24v24H0z" fill="none"/>
                <path d="M12.87 15.07l-2.54-2.51.03-.03c1.74-1.94 2.98-4.17 3.71-6.53H17V4h-7V2H8v2H1v1.99h11.17C11.5 7.92 10.44 9.75 9 11.35 8.07 10.32 7.3 9.19 6.69 8h-2c.73 1.63 1.73 3.17 2.98 4.56l-5.09 5.02L4 19l5-5 3.11 3.11.76-2.04zM18.5 10h-2L12 22h2l1.12-3h4.75L21 22h2l-4.5-12zm-2.62 7l1.62-4.33L19.12 17h-3.24z"/>
            </svg>
        </a>
        <ul class="dropdown-menu dropdown-menu-light text-light" aria-labelledby="languageDropdownLink">
            [#list interfaceLanguges as lang, name]
                [#if lang != localeLanguage]
                    <li><a class="dropdown-item menu-link" href="${requrl}${lang}">${name}</a></li>
                [#else]
                    <li><a class="dropdown-item menu-link fw-bold">${name}</a></li>
                [/#if]
            [/#list]
        </ul>
    </li>
</ul>
