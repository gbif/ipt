<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/resourcesTable.ftl"/>
<script type="text/javascript" language="javascript" src="${baseURL}/js/jquery/jquery-3.2.1.min.js"></script>
<script type="text/javascript" language="javascript" src="${baseURL}/js/jquery/jquery.dataTables.js"></script>
<@resourcesTable shownPublicly=true numResourcesShown=20 sEmptyTable="dataTables.sEmptyTable.resources" columnToSortOn=1 sortOrder="asc" />
<h1 class="rtableTitle"><@s.text name="portal.home.title"/></h1>
<div id="tableContainer"></div>

<!-- RSS Feed shown if there are resources -->
<#if (resources?size>0)>
    <p>
        <@s.text name="portal.home.feed"><@s.param>${baseURL}/rss.do</@s.param></@s.text> <img id="rssImage" src="${baseURL}/images/rss.png"/>.
    </p>
<#else>
    <p><@s.text name="portal.home.no.public"/></p>
</#if>

<#include "/WEB-INF/pages/inc/footer.ftl">
