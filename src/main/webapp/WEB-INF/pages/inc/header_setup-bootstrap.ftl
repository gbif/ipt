[#ftl output_format="HTML"]
<!DOCTYPE html>
<html lang="en" class="h-100">
<head>
    <meta name="copyright" lang="en" content="GBIF" />
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>[@s.text name="admin.config.setup.title"/]</title>

    <!-- Bootstrap core CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl" crossorigin="anonymous">

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
            padding-top: 20px;
        }

        .logo {
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

        .cc-logo {
            display: block;
            height: 31px;
            width: 88px;
        }

        a {
            color: #008959;
        }

        /* Copy of .invalid-feedback, but without 'display: none' */
        .invalid-feedback-display {
            width: 100%;
            margin-top: 0.25rem;
            font-size: 0.875em;
            color: #dc3545;
        }
    </style>

</head>
<body class="bg-light d-flex flex-column h-100">

<header>

    <nav class="navbar navbar-expand-lg navbar-dark fixed-top bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="${baseURL}/">
                <svg class="logo" viewBox="90 239.1 539.7 523.9" xmlns="http://www.w3.org/2000/svg">
                    <path className="gbif-logo-svg"
                          d="M325.5,495.4c0-89.7,43.8-167.4,174.2-167.4C499.6,417.9,440.5,495.4,325.5,495.4"/>
                    <path className="gbif-logo-svg" d="M534.3,731c24.4,0,43.2-3.5,62.4-10.5c0-71-42.4-121.8-117.2-158.4c-57.2-28.7-127.7-43.6-192.1-43.6c28.2-84.6,7.6-189.7-19.7-247.4c-30.3,60.4-49.2,164-20.1,248.3c-57.1,4.2-102.4,29.1-121.6,61.9c-1.4,2.5-4.4,7.8-2.6,8.8c1.4,0.7,3.6-1.5,4.9-2.7c20.6-19.1,47.9-28.4,74.2-28.4c60.7,0,103.4,50.3,133.7,80.5C401.3,704.3,464.8,731.2,534.3,731"/>
                </svg>
                GBIF IPT
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavDarkDropdown" aria-controls="navbarNavDarkDropdown" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNavDarkDropdown">
                <div class="navbar-nav me-auto mb-2 mb-lg-0"></div>
                <div class="d-flex">
                    [#include "/WEB-INF/pages/inc/languages-bootstrap.ftl"/]
                </div>
            </div>
        </div>
    </nav>

</header>
