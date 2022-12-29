<!-- Represents metadata section on resource overview page -->
<span class="anchor anchor-home-resource-page" id="anchor-metadata"></span>
<div class="py-5 border-bottom section" id="metadata">
    <div class="row">
        <div class="col-9">
            <h5 class="mb-0 text-gbif-header-2 fw-400">
                <#assign metadataHeaderInfo>
                    <@s.text name='manage.metadata.description'/>
                    <#if resource.coreType?has_content && resource.coreType==metadataType>
                        <br><br>
                        <@s.text name='manage.overview.source.hidden'>
                            <@s.param><a href="${baseURL}/manage/metadata-basic.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.basic"/></a></@s.param>
                        </@s.text>
                    </#if>
                </#assign>
                <@popoverTextInfo metadataHeaderInfo/>

                <@s.text name='manage.overview.metadata'/>
            </h5>
        </div>

        <div class="col-3 d-flex justify-content-end">
            <button id="edit-metadata-button" class="btn btn-sm overview-action-button">
                <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                </svg>
                <@s.text name='button.edit'/>
            </button>
        </div>
    </div>

    <#if metadataModifiedSinceLastPublication || resource.lastPublished??>
        <div class="text-smaller">
            <small>
                <#if metadataModifiedSinceLastPublication>
                    <@s.text name='manage.home.last.modified'/> ${resource.getMetadataModified()?datetime?string.medium!}
                <#elseif resource.lastPublished??>
                    <@s.text name="manage.overview.notModified"/>
                </#if>
            </small>
        </div>
    </#if>

    <div class="row mt-4">
        <p class="mb-0">
            <#if missingMetadata>
                <span title="<@s.text name='manage.overview.missing.metadata'/>" class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill metadata-incomplete"><@s.text name="manage.overview.metadata.incomplete"/></span>
            <#else>
                <span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill metadata-complete"><@s.text name="manage.overview.metadata.complete"/></span>
            </#if>
            <@s.text name="manage.overview.metadata.description"/>
        </p>
    </div>
</div>
