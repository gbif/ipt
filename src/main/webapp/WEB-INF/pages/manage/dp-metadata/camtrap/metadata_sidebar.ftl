<#ftl output_format="HTML">
<#-- left column of the Camtrap DP metadata pages, as manage/eml/eml_sidebar.ftl (styles/eml.css): "On this page" lists
     the page's parts marked data-toc (js/eml-sidebar.js, hidden when there's only one), "Metadata pages" lists all
     pages in order with the current one highlighted, then Save and Back to the resource overview. Goes inside the form. -->
<nav id="sidebar-content" class="eml-side">
    <div id="eml-toc" hidden>
        <div class="eml-side-title"><@s.text name="manage.overview.onThisPage"/></div>
        <ul id="eml-toc-list"></ul>
    </div>

    <div id="eml-other">
        <div class="eml-side-title"><@s.text name="manage.metadata.pages"/></div>
        <ul>
            <#list ["basic", "geographic", "taxonomic", "temporal", "keywords", "project", "citation", "other"] as it>
                <#if it == currentMetadataPage>
                    <li><a class="current" aria-current="page" href="camtrap-metadata-${it}.do?r=${resource.shortname!r!}"><@s.text name="submenu.datapackagemetadata.camtrap.${it}"/></a></li>
                <#else>
                    <li><a href="camtrap-metadata-${it}.do?r=${resource.shortname!r!}"><@s.text name="submenu.datapackagemetadata.camtrap.${it}"/></a></li>
                </#if>
            </#list>
        </ul>
    </div>

    <div class="eml-side-save">
        <button type="submit" id="side-save" class="btn btn-sm btn-gbif-solid" name="save" value="save"><@icon name="save"/> <@s.text name="button.save"/></button>
        <a class="btn btn-sm btn-outline-secondary" href="resource.do?r=${resource.shortname}"><@s.text name="button.back"/></a>
    </div>
</nav>
<script src="${baseURL}/js/eml-sidebar.js"></script>
