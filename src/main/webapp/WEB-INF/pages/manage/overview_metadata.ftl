<!-- Represents metadata section on resource overview page -->
<span class="anchor anchor-home-resource-page" id="anchor-metadata"></span>
<div class="py-5 border-bottom section" id="metadata">
    <div class="d-flex justify-content-between">
        <div class="d-flex">
            <h5 class="my-auto text-gbif-header-2 fw-400">
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

        <div class="d-flex justify-content-end">
            <div class="dropdown">
                <a class="icon-button icon-material-actions overview-action-button metadata-action" type="button" href="#" id="dropdown-metadata-actions" data-bs-toggle="dropdown" aria-expanded="false">
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                    </svg>
                </a>

                <ul class="dropdown-menu" aria-labelledby="dropdown-metadata-actions">
                    <li>
                        <a id="edit-metadata-button" class="dropdown-item action-link" type="button" href="metadata-basic.do?r=${resource.shortname}">
                            <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                            </svg>
                            <@s.text name="button.edit"/>
                        </a>
                    </li>
                    <li>
                        <a id="upload-metadata-button" class="dropdown-item action-link" type="button" href="#">
                            <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                    <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                                </svg>
                            </svg>
                            <@s.text name="button.upload"/>
                        </a>
                    </li>
                </ul>
            </div>
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
