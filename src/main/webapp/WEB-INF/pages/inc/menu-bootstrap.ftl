[#ftl output_format="HTML"]
</head>

<body class="bg-light d-flex flex-column h-100">

<header>
    <nav class="navbar navbar-expand-lg navbar-dark fixed-top bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="${baseURL}/">
                <svg class="logo" viewBox="90 239.1 539.7 523.9" xmlns="http://www.w3.org/2000/svg">
                    <path className="gbif-logo-svg"
                          d="M325.5,495.4c0-89.7,43.8-167.4,174.2-167.4C499.6,417.9,440.5,495.4,325.5,495.4"/>
                    <path className="gbif-logo-svg" d="M534.3,731c24.4,0,43.2-3.5,62.4-10.5c0-71-42.4-121.8-117.2-158.4c-57.2-28.7-127.7-43.6-192.1-43.6
        c28.2-84.6,7.6-189.7-19.7-247.4c-30.3,60.4-49.2,164-20.1,248.3c-57.1,4.2-102.4,29.1-121.6,61.9c-1.4,2.5-4.4,7.8-2.6,8.8
        c1.4,0.7,3.6-1.5,4.9-2.7c20.6-19.1,47.9-28.4,74.2-28.4c60.7,0,103.4,50.3,133.7,80.5C401.3,704.3,464.8,731.2,534.3,731"/>
                </svg>
                GBIF IPT
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarCollapse" aria-controls="navbarCollapse" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarCollapse">
                <ul class="navbar-nav me-auto mb-2 mb-md-0">
                    <li class="nav-item">
                        <a class="nav-link [#if currentMenu=='home']active[/#if]" href="${baseURL}/">[@s.text name="menu.home"/]</a>
                    </li>
                    [#if managerRights]
                        <li class="nav-item">
                            <a class="nav-link [#if currentMenu=='manage']active[/#if]" href="${baseURL}/manage/">[@s.text name="menu.manage"/]</a>
                        </li>
                    [/#if]
                    [#if adminRights]
                        <li class="nav-item">
                            <a class="nav-link [#if currentMenu=='admin']active[/#if]" href="${baseURL}/admin/">[@s.text name="menu.admin"/]</a>
                        </li>
                    [/#if]
                    <li class="nav-item">
                        <a class="nav-link [#if currentMenu=='about']active[/#if]" href="${baseURL}/about.do">[@s.text name="menu.about"/]</a>
                    </li>
                </ul>
                <div class="d-flex">
                    <div class="collapse navbar-collapse" id="navbarNavDarkDropdown">
                        [#include "/WEB-INF/pages/inc/languages-bootstrap.ftl"/]
                    </div>
                    [#if (Session.curr_user)??]
                        <ul class="navbar-nav">
                            <li class="nav-item dropdown">
                                <a class="btn btn-outline-light" href="#" id="navbarDarkDropdownMenuLink" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    ${Session.curr_user.email}
                                </a>
                                <ul class="dropdown-menu dropdown-menu-dark text-light" aria-labelledby="navbarDarkDropdownMenuLink">
                                    <li>
                                        <a class="dropdown-item" href="${baseURL}/account.do">
                                            [@s.text name="menu.account"/]
                                        </a>
                                    </li>
                                    <li>
                                        <a class="dropdown-item" href="${baseURL}/logout.do">
                                            [@s.text name="menu.logout"/]
                                        </a>
                                    </li>
                                </ul>
                            </li>
                        </ul>
                    [#else]
                        <form class="d-flex" action="${baseURL}/login.do" method="post">
                            <button class="btn btn-outline-light" type="submit" name="login-submit">
                                [@s.text name="portal.login"/]
                            </button>
                        </form>
                    [/#if]
                </div>
            </div>
        </div>
    </nav>
</header>

[@s.actionmessage/]
[#if warnings?size>0]
    <ul class="warnMessage">
        [#list warnings as w]
            <li><span>${w!}</span></li>
        [/#list]
    </ul>
[/#if]
[@s.actionerror/]
