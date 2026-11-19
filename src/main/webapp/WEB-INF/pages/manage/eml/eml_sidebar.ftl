<#ftl output_format="HTML">
<#-- left column of the EML metadata pages: "On this page" lists the page's parts (the elements marked data-toc,
     filled in by js/eml-sidebar.js, hidden when the page has only one), "Metadata pages" lists all pages in order, the
     current one highlighted. Save and Back are in the heading (eml_heading.ftl) and the save bar (eml_savebar.ftl). -->
<nav id="sidebar-content" class="eml-side">
    <div id="eml-toc" hidden>
        <div class="eml-side-title"><@s.text name="manage.overview.onThisPage"/></div>
        <ul id="eml-toc-list"></ul>
    </div>

    <div id="eml-other">
        <div class="eml-side-title"><@s.text name="manage.metadata.pages"/></div>
        <ul>
            <#list ["basic", "contacts", "geocoverage", "project", "methods", "citations", "collections"] as it>
                <#if it == currentMetadataPage>
                    <li><a class="current" aria-current="page" href="metadata-${it}.do?r=${resource.shortname!r!}"><@s.text name="submenu.${it}"/></a></li>
                <#else>
                    <li><a href="metadata-${it}.do?r=${resource.shortname!r!}"><@s.text name="submenu.${it}"/></a></li>
                </#if>
            </#list>
        </ul>
    </div>
</nav>
<script src="${baseURL}/js/eml-sidebar.js"></script>
