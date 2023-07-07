[#ftl output_format="HTML"]
[#setting date_format="yyyy-MM-dd"]
[#setting time_format="HH:mm:ss"]
[#setting datetime_format="iso"]
[#setting locale="${locale}"]
[#setting url_escaping_charset="UTF-8"]
<!DOCTYPE html>
<html lang="en" class="h-100">
<head>

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" type="text/css" href="${baseURL}/styles/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="${baseURL}/styles/bootstrap/bootstrap-borders.css" />
    <link rel="stylesheet" type="text/css" href="${baseURL}/styles/dataTables/jquery.dataTables-1.13.1.min.css" />

    <!-- Bootstrap icons -->
    <link rel="stylesheet" type="text/css" href="${baseURL}/styles/bootstrap-icons/font/bootstrap-icons.css" />

    <!-- Google fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&display=swap" rel="stylesheet">

    <!-- IPT CSS -->
    <style>
        :root {
            --color-gbif-primary: ${primaryColor!"97, 168, 97"};
            --color-gbif-danger: ${"227, 99, 112"};
            --color-gbif-secondary: ${"78, 86, 95"};
            --color-gbif-warning: ${"255, 193, 8"};
            --navbar-color: ${navbarColor!"120, 181, 120"};
            --navbar-link-color: ${navbarLinkColor!"255, 255, 255"};
            --link-color: ${linkColor!"75, 162, 206"};
        }
    </style>
    <link rel="stylesheet" type="text/css" href="${baseURL}/styles/main.css" />

    <!-- Custom CSS for customizations -->
    <link rel="stylesheet" type="text/css" href="${baseURL}/styles/custom.css" />

[#--    <link rel="shortcut icon" href="${baseURL}/appLogo.do" type="image/x-icon" />--]
    <link rel="shortcut icon" href="${baseURL}/images/icons/favicon-16x16.png" type="image/x-icon" />
    <link href="${baseURL}/rss.do" title="Latest Resources" rel="alternate" type="application/rss+xml" />

    <!-- for support of old browsers, like IE8. See http://modernizr.com/docs/#html5inie -->
    <script src="${baseURL}/js/modernizr.js"></script>
    <script src="${baseURL}/js/jquery/jquery-3.7.0.min.js"></script>
    <script src="${baseURL}/js/jquery/jquery-ui.min-1.12.1.js"></script>
    <script src="${baseURL}/js/global.js"></script>


    [#-- GOOGLE ANALYTICS - asynchroneous: https://support.google.com/analytics/answer/10271001?hl=en#zippy=%2Cin-this-article --]
    [#if (cfg.analyticsKey!"")?length>1]
        <script async src="https://www.googletagmanager.com/gtag/js?id=${cfg.analyticsKey}"></script>
        <script>
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', '${cfg.analyticsKey}');
        </script>
    [/#if]

    [#-- If not logged, refresh page periodically to avoid CSRF token expiration --]
    [#if !loggedIn]
      <script>
        setTimeout(function(){
          window.location.reload();
        }, ${cfg.getCsrfPageRefreshDelay()?c});
      </script>
    [/#if]

    [#-- Metadata used by browsers (title in browser toolbar, bookmark when added to favorites), search engines (keywords) --]
    [#assign metaKeywords = "GBIF, Global Biodiversity Information Facility, IPT, Integrated Publishing Toolkit, checklist, occurrence, metadata, DwC-A, Darwin Core, Darwin Core Archive, biodiversity data, data paper, EML" /]
    [#assign registeredIpt = action.getRegisteredIpt()!""/]
    [#if resource?? && eml??]
        <meta name="description" content="${eml.description!}" />
        [#if eml.subject?has_content]
            <meta name="keywords" content="${eml.subject?replace(";", ",")}" />
        [/#if]
        <meta name="foaf:topic" content="${cfg.getResourceUri(resource.shortname)}/#dataset"/>
        <meta name="foaf:isPrimaryTopicOf" content="${cfg.getResourceUri(resource.shortname)}">
    [#elseif registeredIpt?has_content]
        <meta name="description" content="${registeredIpt.description!}" />
        <meta name="keywords" content="${metaKeywords}" />
    [#else]
        <meta name="description" content="The Integrated Publishing Toolkit (IPT) is a tool developed by the Global Biodiversity Information Facility (GBIF) to provide an easy and efficient way of publishing biodiversity data." />
        <meta name="keywords" content="${metaKeywords}" />
    [/#if]
    <meta name="generator" content="IPT ${cfg.version!}" />
    <meta name="inventory" content="${baseURL}/inventory/dataset"/>
    <meta name="foaf:seeAlso" content="${baseURL}/dcat"/>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1">

    [#assign currentMenu = "home"]
