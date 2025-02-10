<!-- Represents metadata section on resource overview page -->
<span class="anchor anchor-overview-page" id="anchor-metadata"></span>
<div class="py-5 border-bottom section" id="metadata">
    <div class="d-flex justify-content-between">
        <div class="d-flex">
            <h5 class="my-auto text-gbif-header-2 fw-400">
                <#assign metadataHeaderInfo>
                    <@s.text name='manage.metadata.description1'/><br><br><#if resource.dataPackage??><@s.text name='manage.metadata.description2.dp'/><#else><@s.text name='manage.metadata.description2.dwc'/></#if>
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
            <#if dataPackageResource && resource.dataPackageIdentifier?contains("extended-occurrence-dp")>
                <a id="download-metadata-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button" href="${baseURL}/manage/eml.do?r=${resource.shortname}" onClick="_gaq.push(['_trackEvent', 'EML', 'Download', '${resource.shortname}']);" download>
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M5 20h14v-2H5zM19 9h-4V3H9v6H5l7 7z"></path>
                    </svg>
                    <@s.text name="button.download"/>
                </a>

                <a id="upload-metadata-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button" href="#">
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M5 20h14v-2H5v2zm0-10h4v6h6v-6h4l-7-7-7 7z"></path>
                    </svg>
                    <@s.text name="button.upload"/>
                </a>

                <a id="edit-metadata-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button me-2" type="button" href="metadata-basic.do?r=${resource.shortname}">
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                    </svg>
                    Edit EML
                </a>
                <a id="edit-metadata-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button me-2" type="button" href="datapackage-metadata-basic.do?r=${resource.shortname}">
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                    </svg>
                    Edit datapackage metadata
                </a>
            <#elseif dataPackageResource>
                <a id="download-metadata-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button" href="${baseURL}/manage/eml.do?r=${resource.shortname}" onClick="_gaq.push(['_trackEvent', 'EML', 'Download', '${resource.shortname}']);" download>
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M5 20h14v-2H5zM19 9h-4V3H9v6H5l7 7z"></path>
                    </svg>
                    <@s.text name="button.download"/>
                </a>

                <a id="upload-metadata-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button" href="#">
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M5 20h14v-2H5v2zm0-10h4v6h6v-6h4l-7-7-7 7z"></path>
                    </svg>
                    <@s.text name="button.upload"/>
                </a>

                <#if resource.coreType?has_content && resource.coreType == "camtrap-dp">
                    <a id="edit-metadata-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button me-2" type="button" href="camtrap-metadata-basic.do?r=${resource.shortname}">
                        <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                          <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                        </svg>
                        <@s.text name="button.edit"/>
                    </a>
                <#elseif resource.coreType?has_content && resource.coreType == "coldp">
                    <a id="view-metadata-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button me-2" type="button" href="#">
                        <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                        </svg>
                        <@s.text name="button.view"/>
                    </a>
                <#elseif resource.coreType?has_content && resource.coreType != "coldp">
                    <a id="edit-metadata-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button me-2" type="button" href="datapackage-metadata-basic.do?r=${resource.shortname}">
                        <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                          <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                        </svg>
                        <@s.text name="button.edit"/>
                    </a>
                </#if>
            <#else>
                <a id="download-metadata-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button" href="${baseURL}/manage/eml.do?r=${resource.shortname}" onClick="_gaq.push(['_trackEvent', 'EML', 'Download', '${resource.shortname}']);" download>
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M5 20h14v-2H5zM19 9h-4V3H9v6H5l7 7z"></path>
                    </svg>
                    <@s.text name="button.download"/>
                </a>

                <a id="upload-metadata-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button" type="button" href="#">
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M5 20h14v-2H5v2zm0-10h4v6h6v-6h4l-7-7-7 7z"></path>
                    </svg>
                    <@s.text name="button.upload"/>
                </a>

                <a id="edit-metadata-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button me-2" type="button" href="metadata-basic.do?r=${resource.shortname}">
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                    </svg>
                    <@s.text name="button.edit"/>
                </a>
            </#if>
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
            <#if resource.coreType?has_content && resource.coreType == "coldp">
                <@s.text name="manage.overview.metadata.coldp.description"/>
            <#else>
                <@s.text name="manage.overview.metadata.description"/>
            </#if>
        </p>
    </div>
</div>
