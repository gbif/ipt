[#ftl output_format="HTML"]
</head>

[#if auxTopNavbar]
<style>
    body {
        padding-top: 120px;
    }

    .second-nav {
        position: fixed;
        top: 50px;
        right: 0;
        left: 0;
        z-index: 999 !important; /* less than top nav dropdowns */
    }
</style>
[/#if]

<body class="bg-light d-flex flex-column h-100">

[#if auxTopNavbarPage == 'mapping']
    <form id="mappingForm" action="mapping.do" method="post">
[/#if]

<header>
    <nav class="navbar navbar-expand-xl navbar-dark fixed-top bg-dark py-1">
        <div class="container">
            <a href="${baseURL}/" rel="home" title="GBIF Logo" class="navbar-brand" >
                <img src="${baseURL}/images/gbif-logo-L.svg" alt="GBIF IPT" class="gbif-logo"/>
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
                    <!-- Languages -->
                    <div id="navbarNavDarkDropdown">
                        [#include "/WEB-INF/pages/inc/languages-bootstrap.ftl"/]
                    </div>

                    <!-- Login, account -->
                    [#if (Session.curr_user)??]
                        <ul class="navbar-nav">
                            <li class="nav-item dropdown d-xl-flex align-content-xl-center">
                                <a class="btn btn-sm btn-outline-light menu-link m-xl-auto" id="accountDropdownLink" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    ${Session.curr_user.email}
                                </a>
                                <ul class="dropdown-menu dropdown-menu-dark text-light" aria-labelledby="accountDropdownLink">
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
                        <form action="${baseURL}/login.do" method="post" class="d-xl-flex align-content-xl-center">
                            <button class="btn btn-sm btn-outline-light m-xl-auto" type="submit" name="login-submit">
                                [@s.text name="portal.login"/]
                            </button>
                        </form>
                    [/#if]
                </div>
            </div>
        </div>
    </nav>

    [#if auxTopNavbar]
        <nav class="navbar navbar-expand-sm navbar-light second-nav bg-body shadow-sm py-1">
            <div class="container">
                [#if auxTopNavbarPage=='mapping']
                    <ul class="navbar-nav me-auto">
                        [#assign groups = fieldsByGroup?keys/]

                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" id="fieldIndexDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                [@s.text name='manage.mapping.index'/]
                            </a>
                            <ul class="dropdown-menu dropdown-menu-light text-light" aria-labelledby="fieldIndexDropdown">
                                [#if (groups?size>0)]
                                    [#list groups as g]
                                        <li [#if redundants?seq_contains(g)] class="redundant" [/#if] ><a class="sidebar-anchor dropdown-item menu-link" href="#group_${g}">${g}</a></li>
                                    [/#list]
                                [/#if]

                                [#if (nonMapped?size>0)]
                                    <li><a class="sidebar-anchor dropdown-item menu-link" href="#nonmapped">[@s.text name='manage.mapping.no.mapped.title'/]</a></li>
                                [/#if]

                                [#if (redundants?size>0)]
                                    <li><a class="sidebar-anchor dropdown-item menu-link" href="#redundant">[@s.text name='manage.mapping.redundant'/]</a></li>
                                [/#if]
                            </ul>
                        </li>

                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" id="filtersDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                [@s.text name='manage.mapping.filters'/]
                            </a>
                            <ul class="dropdown-menu dropdown-menu-light text-light" aria-labelledby="filtersDropdown">
                                <li><a id="toggleFields" class="dropdown-item menu-link" href="#">[@s.text name='manage.mapping.hideEmpty'/]</a></li>

                                [#if (redundants?size>0)]
                                    <li><a id="toggleGroups" class="dropdown-item menu-link" href="#">[@s.text name='manage.mapping.hideGroups'/]</a></li>
                                [/#if]
                            </ul>
                        </li>

                    </ul>

                    <div class="d-flex align-content-between">
                        <ul class="navbar-nav">
                            <li class="nav-item py-2 px-1">
                                [@s.submit cssClass="button btn btn-sm btn-outline-success" name="save" key="button.save"/]
                            </li>
                            <li class="nav-item py-2 px-1">
                                [@s.submit cssClass="confirm btn btn-sm btn-outline-danger" name="delete" key="button.delete"/]
                            </li>
                            <li class="nav-item py-2 px-1">
                                [@s.submit cssClass="button btn btn-sm btn-outline-secondary" name="cancel" key="button.back"/]
                            </li>
                        </ul>
                    </div>
                [/#if]
            </div>
        </nav>
    [/#if]
</header>

<div id="dialog-confirm" class="staticBackdrop" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true" style="display: none"></div>
