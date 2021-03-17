[#ftl output_format="HTML"]
</head>

<body class="bg-light d-flex flex-column h-100">

<header>
    <nav class="navbar navbar-expand-xl navbar-dark fixed-top bg-dark">
        <div class="container-fluid">
            <a href="${baseURL}/" rel="home" title="GBIF Logo" class="navbar-brand" >
                <img src="${baseURL}/images/gbif-logo-2.5.svg" alt="GBIF IPT" class="gbif-logo"/>
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarCollapse" aria-controls="navbarCollapse" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarCollapse">
                <ul class="navbar-nav me-auto mb-md-0">
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
                <div class="d-xl-flex align-content-between">
                    <div id="navbarNavDarkDropdown">
                        [#include "/WEB-INF/pages/inc/languages-bootstrap.ftl"/]
                    </div>
                    [#if (Session.curr_user)??]
                        <ul class="navbar-nav">
                            <li class="nav-item dropdown">
                                <a class="btn btn-outline-light menu-link" id="navbarDarkDropdownMenuLink" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    ${Session.curr_user.email}
                                </a>
                                <ul class="dropdown-menu dropdown-menu-dark text-light" aria-labelledby="navbarDarkDropdownMenuLink">
                                    <li>
                                        <a class="dropdown-item menu-link" href="${baseURL}/account.do">
                                            [@s.text name="menu.account"/]
                                        </a>
                                    </li>
                                    <li>
                                        <a class="dropdown-item menu-link" href="${baseURL}/logout.do">
                                            [@s.text name="menu.logout"/]
                                        </a>
                                    </li>
                                </ul>
                            </li>
                        </ul>
                    [#else]
                        <form action="${baseURL}/login.do" method="post">
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

<div id="dialog-confirm" class="staticBackdrop modal fade" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true" style="display: none"></div>
