[#ftl output_format="HTML"]
<!DOCTYPE html>
<html lang="en" class="h-100">
<head>
    <meta name="copyright" lang="en" content="GBIF" />
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>[@s.text name="admin.config.setup.title"/]</title>

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" type="text/css" href="${baseURL}/styles/bootstrap/bootstrap.min.css">

    <!-- Bootstrap icons -->
    <link rel="stylesheet" type="text/css" href="${baseURL}/styles/bootstrap-icons/font/bootstrap-icons.css" />

    <!-- IPT CSS -->
    <style>
        :root {
            --color-gbif-primary: ${primaryColor};
            --color-gbif-danger: ${dangerColor};
            --color-gbif-secondary: ${secondaryColor};
            --color-gbif-warning: ${warningColor};
            --navbar-color: ${navbarColor};
            --navbar-link-color: ${navbarLinkColor};
            --link-color: ${linkColor};
        }
    </style>
    <link rel="stylesheet" type="text/css" href="${baseURL}/styles/main.css" />

    <!-- Custom CSS for customizations -->
    <link rel="stylesheet" type="text/css" href="${baseURL}/styles/custom.css" />

    <link rel="shortcut icon" href="${baseURL}/images/icons/favicon-16x16.png" type="image/x-icon" />

    <!-- for support of old browsers, like IE8. See http://modernizr.com/docs/#html5inie -->
    <script src="${baseURL}/js/modernizr.js"></script>
    <script src="${baseURL}/js/jquery/jquery-3.5.1.min.js"></script>
    <script src="${baseURL}/js/global.js"></script>

</head>
<body class="bg-body d-flex flex-column h-100">

<header>

    <nav class="navbar navbar-expand-lg navbar-dark fixed-top bg-gbif-main-navbar py-1 shadow-sm border-bottom">
        <div class="container">
            <a href="${baseURL}/" rel="home" title="Logo" class="navbar-brand" >
                <svg version="1.1" id="gbif-logo" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="0 0 539.7 523.9" style="enable-background:new 0 0 539.7 523.9;" xml:space="preserve">
                    <path class="ipt-icon-piece" d="M230.7,255.5c0-102.2,49.9-190.7,198.4-190.7C429.1,167.2,361.7,255.5,230.7,255.5"/>
                    <path class="ipt-icon-piece" d="M468.6,523.9c27.8,0,49.2-4,71.1-12c0-80.9-48.3-138.7-133.5-180.4c-65.2-32.7-145.5-49.7-218.8-49.7C219.5,185.4,196.1,65.7,165,0c-34.5,68.8-56,186.8-22.9,282.8C77,287.6,25.4,315.9,3.6,353.3c-1.6,2.8-5,8.9-3,10c1.6,0.8,4.1-1.7,5.6-3.1c23.5-21.8,54.6-32.4,84.5-32.4c69.1,0,117.8,57.3,152.3,91.7C317.1,493.5,389.4,524.1,468.6,523.9"/>
                </svg>
            </a>
            <!-- Languages -->
            <div id="navbarNavDropdown">
                [#include "/WEB-INF/pages/inc/languages.ftl"/]
            </div>
        </div>
    </nav>

</header>
