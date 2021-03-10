<#ftl output_format="HTML">

<div class="col-lg-3 order-lg-last p-3 bg-body rounded shadow-sm">
    <#if sideMenuEml!false>
        <h5 class="border-bottom pb-2 mb-0 mx-md-4 mx-2 pt-2 text-success text-center">
            <@s.text name='manage.metadata.section' />
        </h5>
        <ul class="sidebar list-group-flush px-3 mb-0">
            <#list ["basic", "geocoverage", "taxcoverage","tempcoverage", "keywords", "parties", "project", "methods", "citations", "collections", "physical", "additional"] as it>
                <li<#if currentSideMenu?? && currentSideMenu==it> class="current list-group-item"<#else> class="sidebar list-group-item"</#if>>
                    <a href="metadata-${it}.do?r=${resource.shortname!r!}" style="color: black !important; text-decoration: none !important;">
                        <@s.text name="submenu.${it}"/>
                    </a>
                </li>
            </#list>
        </ul>
    </#if>
</div>
