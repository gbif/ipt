[#ftl output_format="HTML"]
</head>

<body class="bg-body d-flex flex-column h-100">

<header>
    <nav class="navbar navbar-expand-xl navbar-dark bg-gbif-main-navbar fixed-top py-1 border-bottom shadow-sm">
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
                    <li class="nav-item nav-item-border-bottom">
                        <a class="nav-link [#if currentMenu=='home']active[/#if]" href="${baseURL}/">[@s.text name="menu.home"/]</a>
                    </li>
                    [#if managerRights]
                        <li class="nav-item nav-item-border-bottom">
                            <a class="nav-link [#if currentMenu=='manage']active[/#if]" href="${baseURL}/manage/">[@s.text name="menu.manage"/]</a>
                        </li>
                    [/#if]
                    [#if adminRights]
                        <li class="nav-item nav-item-border-bottom">
                            <a class="nav-link [#if currentMenu=='admin']active[/#if]" href="${baseURL}/admin/">[@s.text name="menu.admin"/]</a>
                        </li>
                    [/#if]
                    <li class="nav-item nav-item-border-bottom">
                        <a class="nav-link [#if currentMenu=='about']active[/#if]" href="${baseURL}/about.do">[@s.text name="menu.about"/]</a>
                    </li>
                </ul>

                <div class="d-xl-flex align-content-between">
                    <!-- Health -->
                    <div class="navbar-nav nav-item-border-bottom">
                      <a href="${baseURL}/health.do" class="nav-link position-relative health-link" title="[@s.text name="portal.health.title"/]">
                          <svg class="gbif-heartbeat-icon [#if currentMenu=='health']active[/#if]" height="20" width="20" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="-356 246.5 90 77.6"  xml:space="preserve">
                              <path d="M-277.2,286.2h-22.9l-5,11l-10.1-43.8l-10.9,32.8h-18.6c-1.8,0-3.2,1.4-3.2,3.2c0,1.8,1.4,3.2,3.2,3.2h23.3l5.2-15.5l9.2,40l11.1-24.5h18.7c1.8,0,3.2-1.4,3.2-3.2C-274,287.7-275.4,286.2-277.2,286.2z"/>
                          </svg>
                      </a>
                    </div>

                    <!-- Languages -->
                    <div id="navbarNavDropdown">
                        [#include "/WEB-INF/pages/inc/languages.ftl"/]
                    </div>

                    <!-- Login, account -->
                    [#if (Session.curr_user)??]
                        <ul class="navbar-nav show-xl-bigger">
                            <li class="nav-item dropdown d-xl-flex align-content-xl-center">
                                <a class="btn btn-sm menu-link m-xl-auto navbar-button border" id="accountDropdownLink" role="button" data-bs-toggle="dropdown" aria-expanded="false">
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
                        <div class="show-xl-smaller d-flex nav-item-border-top">
                            <a class="nav-link ps-0 show-xl-smaller" href="${baseURL}/logout.do">
                                [@s.text name="menu.logout"/]
                            </a>
                            <a href="${baseURL}/account.do" class="nav-link show-xl-smaller nav-link-account">
                                ${Session.curr_user.email}
                            </a>
                        </div>
                    [#else]
                        <form action="${baseURL}/login.do" method="post" class="d-xl-flex align-content-xl-center">
                            <button class="btn btn-sm m-xl-auto navbar-button border text-capitalize show-xl-bigger" type="submit" name="login-submit">
                                [@s.text name="portal.login"/]
                            </button>
                            <div class="navbar-nav show-xl-smaller nav-item-border-top">
                                <a href="javascript:{}" class="nav-link text-capitalize" onclick="this.closest('form').submit();return false;">
                                    [@s.text name="portal.login"/]
                                </a>
                            </div>
                        </form>
                    [/#if]
                </div>
            </div>
        </div>
    </nav>
</header>

<div id="dialog-confirm" class="modal fade" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true"></div>
