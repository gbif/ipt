<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#-- Metadata section of the resource overview: validity and the fields that publishing and registering depend on. Needs overview_status.ftl -->
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
        <a id="${viewOrEditElementId}" class="btn btn-sm btn-outline-gbif-primary" href="${viewOrEditAction}"><@s.text name=viewOrEditKey/></a>
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

    <div class="ov-rows">
        <div class="ov-r">
            <span><@s.text name="manage.overview.metadata.row.title"/></span>
            <span><#if resource.title?has_content>${resource.title}<#else><i class="text-muted"><@s.text name="manage.overview.metadata.shortnameUsed"/></i></#if></span>
        </div>
        <#if isEmlBased>
            <div class="ov-r">
                <span><@s.text name="eml.intellectualRights.license"/></span>
                <span><#if (resource.eml.parseLicenseUrl())?has_content><@shortLicense resource.eml.parseLicenseUrl()/><#else><span class="ov-missing"><@s.text name="manage.overview.metadata.notSet"/></span></#if></span>
            </div>
            <div class="ov-r">
                <span><@s.text name="manage.overview.metadata.row.contacts"/></span>
                <span>
                    <#assign contactCount = (resource.eml.contacts![])?size>
                    <#if contactCount == 0>
                        <span class="ov-missing"><@s.text name="manage.overview.metadata.none"/></span>
                    <#else>
                        <@s.text name="manage.overview.metadata.contactsCount"><@s.param>${contactCount}</@s.param><@s.param>${(resource.eml.creators![])?size}</@s.param></@s.text>
                    </#if>
                </span>
            </div>
            <#assign coverages = []>
            <#if (resource.eml.geospatialCoverages![])?has_content><#assign c><@s.text name="submenu.geocoverage"/></#assign><#assign coverages = coverages + [c]></#if>
            <#if (resource.eml.taxonomicCoverages![])?has_content><#assign c><@s.text name="submenu.taxcoverage"/></#assign><#assign coverages = coverages + [c]></#if>
            <#if (resource.eml.temporalCoverages![])?has_content><#assign c><@s.text name="submenu.tempcoverage"/></#assign><#assign coverages = coverages + [c]></#if>
            <div class="ov-r">
                <span><@s.text name="manage.overview.metadata.row.coverage"/></span>
                <span><#if coverages?has_content><#list coverages as c>${c}<#sep> · </#sep></#list><#else>—</#if></span>
            </div>
        </#if>
    </div>

    <div class="ov-links">
        <#if hasValidationReport>
            <a href="#" class="show-metadata-validation-result"><@s.text name="manage.overview.metadata.modal.validation.report"/></a>
        </#if>
        <a id="download-metadata-button" href="${baseURL}/manage/eml.do?r=${resource.shortname}" download>
            <#if isEmlBased><@s.text name="manage.overview.metadata.downloadEml"/><#else><@s.text name="manage.overview.metadata.download"/></#if>
        </a>
        <a id="upload-metadata-button" href="#anchor-metadata">
            <#if isEmlBased><@s.text name="manage.overview.metadata.uploadEml"/><#else><@s.text name="manage.overview.metadata.upload"/></#if>
        </a>
    </div>
</section>
