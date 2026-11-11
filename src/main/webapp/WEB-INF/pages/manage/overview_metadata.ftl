<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#-- Metadata section of the resource overview: validity, edit and the metadata file actions. Needs overview_status.ftl -->
<#if isDataPackage && !isDwcDp>
    <#if resource.coreType == "camtrap-dp">
        <#assign viewOrEditElementId = "edit-metadata-button"/>
        <#assign viewOrEditAction = "camtrap-metadata-basic.do?r=${resource.shortname}"/>
        <#assign viewOrEditKey = "button.edit"/>
    <#else>
        <#-- ColDP metadata isn't edited in the IPT, only viewed -->
        <#assign viewOrEditElementId = "view-metadata-button"/>
        <#assign viewOrEditAction = "#"/>
        <#assign viewOrEditKey = "button.view"/>
    </#if>
<#else>
    <#assign viewOrEditElementId = "edit-metadata-button"/>
    <#assign viewOrEditAction = "metadata-basic.do?r=${resource.shortname}"/>
    <#assign viewOrEditKey = "button.edit"/>
</#if>
<#assign hasValidationReport = !isDataPackage || isDwcDp || isColDp>

<span class="anchor anchor-overview-page" id="anchor-metadata"></span>
<section class="ov-card section" id="metadata">
    <div class="ov-ch">
        <h2>
            <#assign metadataHeaderInfo>
                <@s.text name='manage.metadata.description1'/><br><br><#if isDataPackage><@s.text name='manage.metadata.description2.dp'/><#else><@s.text name='manage.metadata.description2.dwc'/></#if>
                <#if isMetadataOnly>
                    <br><br>
                    <@s.text name='manage.overview.source.hidden'>
                        <@s.param><a href="${baseURL}/manage/metadata-basic.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.basic"/></a></@s.param>
                    </@s.text>
                </#if>
            </#assign>
            <@popoverTextInfo metadataHeaderInfo/>
            <@s.text name='manage.overview.metadata'/>
        </h2>
        <#if missingBasicMetadata>
            <span class="ov-pill bad show-metadata-validation-result" role="button"><@s.text name="manage.overview.metadata.incomplete"/></span>
        <#elseif !validMetadata>
            <span class="ov-pill bad<#if hasValidationReport> show-metadata-validation-result</#if>" role="button" title="<@s.text name='manage.overview.metadata.invalid'/>"><@s.text name="manage.overview.metadata.invalid.logo"/></span>
        <#else>
            <span class="ov-pill ok<#if hasValidationReport> show-metadata-validation-result</#if>" role="button"><@s.text name="manage.overview.metadata.valid.logo"/></span>
        </#if>
        <#if metadataModifiedSinceLastPublication && isPublished>
            <span class="ov-pill warn"><@s.text name="manage.overview.data.status.changed"/></span>
        </#if>
        <span class="ov-sp"></span>
        <a id="${viewOrEditElementId}" class="ov-hact" href="${viewOrEditAction}"><@icon (viewOrEditKey == "button.view")?then("visibility", "edit")/> <@s.text name=viewOrEditKey/></a>
        <div class="dropdown">
            <button class="ov-hmenu" type="button" id="metadata-menu" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.metadata.actions'/>" aria-label="<@s.text name='manage.overview.metadata.actions'/>">
                <@icon name="more_horiz"/>
            </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="metadata-menu">
                <#if hasValidationReport>
                    <li><a class="dropdown-item show-metadata-validation-result" href="#"><@s.text name="manage.overview.metadata.modal.validation.report"/></a></li>
                </#if>
                <li>
                    <a class="dropdown-item" id="download-metadata-button" href="${baseURL}/manage/eml.do?r=${resource.shortname}" download>
                        <#if isEmlBased><@s.text name="manage.overview.metadata.downloadEml"/><#else><@s.text name="manage.overview.metadata.download"/></#if>
                    </a>
                </li>
                <li>
                    <a class="dropdown-item" id="upload-metadata-button" href="#anchor-metadata">
                        <#if isEmlBased><@s.text name="manage.overview.metadata.uploadEml"/><#else><@s.text name="manage.overview.metadata.upload"/></#if>
                    </a>
                </li>
            </ul>
        </div>
    </div>

    <div class="ov-sub">
        <#if metadataModifiedSinceLastPublication && resource.metadataModified??>
            <@s.text name='manage.home.last.modified'/> ${resource.metadataModified?datetime?string.medium}
        <#elseif isPublished>
            <@s.text name="manage.overview.notModified"/>
        <#elseif isColDp>
            <@s.text name="manage.overview.metadata.coldp.description"/>
        <#else>
            <@s.text name="manage.overview.metadata.description"/>
        </#if>
    </div>

</section>
