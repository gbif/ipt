<#ftl output_format="HTML">

<nav id="sidebar-content">
    <ul>
        <#list ["basic", "geographic", "taxonomic", "temporal", "keywords", "project", "other"] as it>
            <li>
                <a class="sidebar-navigation-link <#if it == currentMetadataPage>active</#if>" href="datapackage-metadata-${it}.do?r=${resource.shortname!r!}">
                    <@s.text name="submenu.datapackagemetadata.${it}"/>
                </a>
            </li>
        </#list>
    </ul>

    <div class="d-flex align-content-between" style="margin-left: -10px;">
        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary me-1" name="save" key="button.save"/>
        <@s.submit cssClass="button btn btn-sm btn-outline-secondary" name="cancel" key="button.back"/>
    </div>
</nav>
