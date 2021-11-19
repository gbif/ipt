[#ftl output_format="HTML"]
</head>

<body class="bg-light d-flex flex-column h-100">

<header>
    <nav class="navbar navbar-expand-xl navbar-dark bg-gbif-green-gradient fixed-top py-1 [#if !auxTopNavbar]shadow-sm[/#if]">
        <div class="container">
            <a href="${baseURL}/" rel="home" title="Logo" class="navbar-brand" >
                <img src="${baseURL}/images/gbif-logo-L.svg" alt="IPT" class="gbif-logo"/>
                [#if !cfg.devMode() && cfg.getRegistryType()?has_content && cfg.getRegistryType()=='PRODUCTION']
                [#else]
                    <img class="testmode" alt="[@s.text name="menu.testMode"/]" src="${baseURL}/images/testmode.png" style="width: 100px;"/>
                [/#if]
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarCollapse" aria-controls="navbarCollapse" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarCollapse">
                <!-- Navbar -->
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
                    <!-- Health -->
                    <div class="navbar-nav">
                      <a href="${baseURL}/health.do" class="nav-link" title="[@s.text name="portal.health.title"/]">
                          <img src="${baseURL}/images/gbif-heartbeat.svg" alt="Status">
                      </a>
                    </div>

                    <!-- Languages -->
                    <div id="navbarNavDropdown">
                        [#include "/WEB-INF/pages/inc/languages.ftl"/]
                    </div>

                    <!-- Login, account -->
                    [#if (Session.curr_user)??]
                        <ul class="navbar-nav">
                            <li class="nav-item dropdown d-xl-flex align-content-xl-center">
                                <a class="btn btn-sm btn-light menu-link m-xl-auto navbar-button" id="accountDropdownLink" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    ${Session.curr_user.email}
                                </a>
                                <ul class="dropdown-menu dropdown-menu-light text-light" aria-labelledby="accountDropdownLink">
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
                        <form action="${baseURL}/login.do" method="post" class="d-xl-flex align-content-xl-center px-1">
                            <button class="btn btn-sm btn-light m-xl-auto navbar-button text-capitalize" type="submit" name="login-submit">
                                [@s.text name="portal.login"/]
                            </button>
                        </form>
                    [/#if]
                </div>
            </div>
        </div>
    </nav>
</header>

<div id="dialog-confirm" class="modal fade" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true"></div>
