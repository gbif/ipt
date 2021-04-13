[#ftl output_format="HTML"]
<!DOCTYPE html>
<html lang="en" class="h-100">
<head>
    <meta name="copyright" lang="en" content="GBIF" />
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>[@s.text name="admin.config.setup.title"/]</title>

    <!-- Bootstrap core CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-eOJMYsd53ii+scO/bJGFsiCZc+5NDVN2yr8+0RDqr0Ql0h+rP48ckxlpbzKgwra6" crossorigin="anonymous">

    <link rel="shortcut icon" href="${baseURL}/images/icons/favicon-16x16.png" type="image/x-icon" />

    <!-- for support of old browsers, like IE8. See http://modernizr.com/docs/#html5inie -->
    <script type="text/javascript" src="${baseURL}/js/modernizr.js"></script>
    <script type="text/javascript" src="${baseURL}/js/jquery/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="${baseURL}/js/global.js"></script>

    <style>
        html,
        body {
            overflow-x: hidden; /* Prevent scroll on narrow devices */
        }

        body {
            min-height: 75rem;
            padding-top: 4.5rem; /* Separates nav and main */
        }

        .bg-gbif-green-gradient {
            background: linear-gradient(#78b578, #71b171) !important;
        }

        .gbif-logo {
            height: 32px;
            position: relative;
            line-height: 32px;
            padding-right: 8px;
            padding-left: 8px;
            transition: all .3s;
            overflow: hidden;
            vertical-align: middle;
            fill: #008959;
        }

        a {
            color: #008959;
        }

        .ipt-footer-item:after {
            content: '|';
            margin: 0 5px;
        }

        footer a {
            text-decoration: none !important;
        }

        header, .dropdown-menu {
            font-size: 0.875rem !important;
        }
    </style>

</head>
<body class="bg-light d-flex flex-column h-100">

<header>

    <nav class="navbar navbar-expand-lg navbar-dark fixed-top bg-gbif-green-gradient py-1 shadow-sm">
        <div class="container">
            <a href="${baseURL}/" rel="home" title="GBIF Logo" class="navbar-brand" >
                <img src="${baseURL}/images/gbif-logo-L.svg" alt="GBIF IPT" class="gbif-logo"/>
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNavDropdown">
                <div class="navbar-nav me-auto mb-2 mb-lg-0"></div>
                <div class="d-flex">
                    [#include "/WEB-INF/pages/inc/languages-bootstrap.ftl"/]
                </div>
            </div>
        </div>
    </nav>

</header>
