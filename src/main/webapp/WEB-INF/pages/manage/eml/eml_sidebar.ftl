<#ftl output_format="HTML">
<#-- left column of the EML metadata pages: "On this page" lists the page's parts (the elements marked data-toc, filled
     in by js/eml-sidebar.js, hidden when the page has only one), "Metadata pages" lists all pages in order, the current
     one highlighted, then Save and Back to the resource overview (the column is sticky, so they stay in view; the heading
     has them too, the save bar has Save). Goes inside the page's form. -->
<nav id="sidebar-content" class="eml-side">
    <div id="eml-toc" hidden>
        <div class="eml-side-title"><@s.text name="manage.overview.onThisPage"/></div>
        <ul id="eml-toc-list"></ul>
    </div>

    <div id="eml-other">
        <div class="eml-side-title"><@s.text name="manage.metadata.pages"/></div>
        <ul>
            <#list ["basic", "contacts", "coverage", "project", "methods", "citations", "collections"] as it>
                <#if it == currentMetadataPage>
                    <li><a class="current" aria-current="page" href="metadata-${it}.do?r=${resource.shortname!r!}"><@s.text name="submenu.${it}"/></a></li>
                <#else>
                    <li><a href="metadata-${it}.do?r=${resource.shortname!r!}"><@s.text name="submenu.${it}"/></a></li>
                </#if>
            </#list>
        </ul>
    </div>

    <div class="eml-side-save">
        <#-- id side-save: coverage.ftl checks the coordinates before submitting -->
        <button type="submit" id="side-save" class="btn btn-sm btn-gbif-solid" name="save" value="save"><@icon name="save"/> <@s.text name="button.save"/></button>
        <a class="btn btn-sm btn-outline-secondary" href="resource.do?r=${resource.shortname}"><@s.text name="button.back"/></a>
    </div>
</nav>
<script src="${baseURL}/js/eml-sidebar.js"></script>
