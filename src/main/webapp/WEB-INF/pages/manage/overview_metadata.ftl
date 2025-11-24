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

        <#if dataPackageResource>
            <#if resource.coreType?has_content && resource.coreType == "camtrap-dp">
                <#assign viewOrEditElementId="edit-metadata-button"/>
                <#assign viewOrEditButtonText>
                    <@s.text name="button.edit"/>
                </#assign>
                <#assign viewOrEditAction="camtrap-metadata-basic.do?r=${resource.shortname}"/>
                <#assign viewOrEditIcon>
                    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                </#assign>
            <#elseif resource.coreType?has_content && resource.coreType == "coldp">
                <#assign viewOrEditElementId="view-metadata-button"/>
                <#assign viewOrEditButtonText>
                    <@s.text name="button.view"/>
                </#assign>
                <#assign viewOrEditAction="#"/>
                <#assign viewOrEditIcon>
                    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                </#assign>
            <#elseif resource.coreType?has_content && resource.coreType != "coldp">
                <#assign viewOrEditElementId="edit-metadata-button"/>
                <#assign viewOrEditButtonText>
                    <@s.text name="button.edit"/>
                </#assign>
                <#assign viewOrEditAction="datapackage-metadata-basic.do?r=${resource.shortname}"/>
                <#assign viewOrEditIcon>
                    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                </#assign>
            </#if>
        <#else>
            <#assign viewOrEditElementId="edit-metadata-button"/>
            <#assign viewOrEditButtonText>
                <@s.text name="button.edit"/>
            </#assign>
            <#assign viewOrEditAction="metadata-basic.do"/>
            <#assign viewOrEditIcon>
                <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
            </#assign>
        </#if>

        <div class="d-flex justify-content-end align-items-center">
            <a id="${viewOrEditElementId}" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button me-2" type="button" href="${viewOrEditAction}">
                <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                    ${viewOrEditIcon}
                </svg>
                ${viewOrEditButtonText}
            </a>

            <div class="dropdown me-2">
                <button class="icon-button icon-material-actions overview-action-button dropdown-toggle d-flex align-items-center" type="button" id="metadataDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2m0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2m0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2"></path>
                    </svg>
                    <@s.text name="button.options"/>
                </button>

                <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="metadataDropdown">
                    <li>
                        <a class="btn btn-sm btn-outline-gbif-primary w-100 dropdown-button" id="download-metadata-button"
                           href="${baseURL}/manage/eml.do?r=${resource.shortname}"
                           onclick="_gaq.push(['_trackEvent', 'EML', 'Download', 'test-empty-organization']);"
                           download>
                            <@s.text name="button.download"/>
                        </a>
                    </li>
                    <li>
                        <a class="btn btn-sm btn-outline-gbif-primary w-100 dropdown-button" id="upload-metadata-button" href="#">
                            <@s.text name="button.upload"/>
                        </a>
                    </li>
                    <#if !resource.isDataPackage()>
                    <li>
                        <a class="show-metadata-validation-result btn btn-sm btn-outline-gbif-primary w-100 dropdown-button" href="#">
                            <@s.text name="manage.overview.metadata.modal.validation.report"/>
                        </a>
                    </li>
                    </#if>
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
            <#if !validMetadata>
                <span title="<@s.text name='manage.overview.metadata.invalid'/>" class="show-metadata-validation-result fs-smaller-2 text-nowrap dt-content-link dt-content-pill metadata-incomplete"><@s.text name="manage.overview.metadata.invalid.logo"/></span>
            <#else>
                <span class="show-metadata-validation-result fs-smaller-2 text-nowrap dt-content-link dt-content-pill metadata-complete"><@s.text name="manage.overview.metadata.valid.logo"/></span>
            </#if>
            <#if resource.coreType?has_content && resource.coreType == "coldp">
                <@s.text name="manage.overview.metadata.coldp.description"/>
            <#else>
                <@s.text name="manage.overview.metadata.description"/>
            </#if>
        </p>
    </div>
</div>
