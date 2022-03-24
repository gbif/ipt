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
                <img src="${baseURL}/images/gbif-logo-L.svg" alt="IPT" class="gbif-logo"/>
            </a>
            <!-- Languages -->
            <div id="navbarNavDropdown">
                [#include "/WEB-INF/pages/inc/languages.ftl"/]
            </div>
        </div>
    </nav>

</header>
