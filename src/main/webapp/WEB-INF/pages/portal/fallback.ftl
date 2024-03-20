[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header.ftl"/]
<title>[@s.text name="error.header.title"/]</title>

<style>
    body {
        font-family: Roboto, 'Helvetica Neue', Helvetica, Arial, sans-serif !important;
        padding-top: 15px;
        color: #575757;
        margin: 0;
    }

    @media (min-width: 992px) {
        .container {
            max-width: 960px;
        }
    }

    @media (min-width: 768px) {
        .container {
            max-width: 720px;
        }
    }

    @media (min-width: 576px) {
        .container {
            max-width: 540px;
        }
    }

    .container, .container-fluid {
        width: 100%;
        padding-right: 0;
        padding-left: 0;
        margin-right: auto;
        margin-left: auto;
    }

    .bg-body{background-color:#fff!important}

    .border{border:1px solid #dee2e6!important}
    .rounded-2{border-radius:.25rem!important}
    .border-bottom {
        border-bottom: 1px solid #dee2e6 !important;
    }

    .my-3{margin-top:1rem!important;margin-bottom:1rem!important}
    .p-3{padding:1rem!important}
    .mb-4{margin-bottom:1.5rem!important}
    .pb-2{padding-bottom:.5rem!important}
    .mb-0{margin-bottom:0!important}
    .pt-1{padding-top:.25rem!important}
    .pt-2{padding-top:.5rem!important}

    .text-center{text-align:center!important}
    .fs-smaller {
        font-size: 0.875rem !important;
    }
    .fs-2{font-size:2rem!important}
    .text-gbif-header {
        color: #4E565F;
    }
    .fw-normal{font-weight:400!important}

    .d-flex{display:flex!important}
    .flex-column{flex-direction:column!important}

    a {
        color: rgb(75, 162, 206) !important;
        text-decoration: none !important;
    }

    a:hover {
        filter: brightness(0.85);
        text-decoration: underline !important;
    }
</style>

<body class="bg-body d-flex flex-column h-100">
<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body rounded-2 mb-4">
        <div class="container my-3">
            <div class="text-center fs-smaller">
                [@s.text name="basic.error"/]
            </div>

            <div class="text-center">
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    IPT Public URL not available
                </h1>
            </div>
        </div>
    </div>
</div>

<main class="container main-content-container">
    <div class="my-3 p-3">
        <p class="text-center">
            Failed to load IPT files from ${baseURL}. Please make sure the IPT public URL is properly configured.
            See <a href="https://ipt.gbif.org/manual/en/ipt/latest/">IPT documentation</a>
        </p>
    </div>
</main>

</body>

[#--[#include "/WEB-INF/pages/inc/footer.ftl"/]--]
