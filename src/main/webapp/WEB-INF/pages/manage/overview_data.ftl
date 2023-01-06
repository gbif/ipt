<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->

<!-- Represents source data and mapping data sections on resource overview page -->
<span class="anchor anchor-home-resource-page" id="anchor-sources"></span>
<div class="py-5 border-bottom section" id="sources">
    <div class="titleOverview">
        <div class="d-flex justify-content-between">
            <div class="d-flex">
                <h5 class="my-auto text-gbif-header-2 fw-400">
                    <#assign sourcesInfo>
                        <@s.text name='manage.overview.source.description1'/>&nbsp;<@s.text name='manage.overview.source.description2'/>&nbsp;<@s.text name='manage.overview.source.description3'><@s.param><@s.text name='button.add'/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description4'><@s.param><@s.text name="button.connectDB"/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description5'/>
                    </#assign>
                    <@popoverTextInfo sourcesInfo/>

                    <@s.text name='manage.overview.source.data'/>
                </h5>
            </div>

            <div class="d-flex justify-content-end">
                <div class="dropdown">
                    <a class="icon-button icon-material-actions overview-action-button source-action" type="button" href="#" id="dropdown-source-actions" data-bs-toggle="dropdown" aria-expanded="false">
                        <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                            <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                        </svg>
                    </a>

                    <ul class="dropdown-menu" aria-labelledby="dropdown-source-actions">
                        <li>
                            <a id="add-source-button" class="dropdown-item action-link" type="button" href="#">
                                <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                    <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                </svg>
                                <@s.text name="button.add"/>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>

        <#if sourcesModifiedSinceLastPublication || resource.lastPublished??>
        <div class="text-smaller">
            <small>
                <span>
                    <#if sourcesModifiedSinceLastPublication>
                        <@s.text name='manage.home.last.modified'/> ${resource.getSourcesModified()?datetime?string.medium!}
                    <#elseif resource.lastPublished??>
                        <@s.text name="manage.overview.notModified"/>
                    </#if>
                </span>
            </small>
        </div>
        </#if>

        <div class="mt-4">
            <p class="mb-0">
                <@s.text name='manage.overview.source.intro'/>
            </p>

            <#if (resource.sources?size>0)>
                <div class="details mt-3">
                    <div class="row g-2">
                        <#assign lastModifiedNotSet><@s.text name='basic.lastModified.not.set'/></#assign>

                        <#list resource.sources as src>
                            <div class="col-xl-6">
                                <div class="d-flex justify-content-between border rounded-2 mx-1 p-1 py-2 source-item">
                                    <#if src.isFileSource()>
                                        <div class="text-smaller source-item-link ps-2" data-ipt-resource="${resource.shortname}" data-ipt-source="${src.name}">
                                            <#if src.readable>
                                                <i class="bi bi-file-text me-1 text-gbif-primary"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.file'/><br><span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br>"></i>
                                            <#else>
                                                <i class="bi bi-file-text me-1 text-gbif-danger"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.file'/><br><span class='text-gbif-danger'><@s.text name='manage.source.notReadable'/><span><br>"></i>
                                            </#if>
                                            <span class="fw-bold overflow-wrap">${src.name!}</span><br>
                                            <small>
                                                ${src.fileSizeFormatted} <span class="fw-bold">|</span>
                                                ${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>/${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/> <span class="fw-bold">|</span>
                                            </small>
                                            <small>
                                                ${(src.lastModified?datetime?string.medium)!lastModifiedNotSet}
                                            </small>
                                        </div>

                                    <#elseif src.isExcelSource()>
                                        <div class="text-smaller source-item-link ps-2" data-ipt-resource="${resource.shortname}" data-ipt-source="${src.name}">
                                            <#if src.readable>
                                                <i class="bi bi-file-excel me-1 text-gbif-primary"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.excel'/><br><span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br>"></i>
                                            <#else>
                                                <i class="bi bi-file-excel me-1 text-gbif-danger"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.excel'/><br><span class='text-gbif-danger'><@s.text name='manage.source.notReadable'/><span><br>"></i>
                                            </#if>
                                            <span class="fw-bold overflow-wrap">${src.name}</span><br>
                                            <small>
                                                ${src.fileSizeFormatted} <span class="fw-bold">|</span>
                                                ${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>/${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/> <span class="fw-bold">|</span>
                                            </small>
                                            <small>
                                                ${(src.lastModified?datetime?string.medium)!lastModifiedNotSet}
                                            </small>
                                        </div>

                                    <#elseif src.isUrlSource()>
                                        <div class="text-smaller source-item-link ps-2" data-ipt-resource="${resource.shortname}" data-ipt-source="${src.name}">
                                            <#if src.readable>
                                                <i class="bi bi-link me-1 text-gbif-primary"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.url'/><br><span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br>"></i>
                                            <#else>
                                                <i class="bi bi-link me-1 text-gbif-danger"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.url'/><br><span class='text-gbif-danger'><@s.text name='manage.source.notReadable'/><span><br>"></i>
                                            </#if>
                                            <span class="fw-bold overflow-wrap">${src.name}</span><br>
                                            <small>
                                                ${src.fileSizeFormatted} <span class="fw-bold">|</span>
                                                ${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>/${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/> <span class="fw-bold">|</span>
                                            </small>
                                            <small>
                                                ${(src.lastModified?datetime?string.medium)!lastModifiedNotSet}
                                            </small>
                                        </div>

                                    <#else>
                                        <div class="text-smaller source-item-link ps-2" data-ipt-resource="${resource.shortname}" data-ipt-source="${src.name}">
                                            <#if src.readable>
                                                <i class="me-1 text-gbif-primary"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.sql'/><br><span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br>">
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-database" viewBox="0 0 16 16">
                                                        <path d="M4.318 2.687C5.234 2.271 6.536 2 8 2s2.766.27 3.682.687C12.644 3.125 13 3.627 13 4c0 .374-.356.875-1.318 1.313C10.766 5.729 9.464 6 8 6s-2.766-.27-3.682-.687C3.356 4.875 3 4.373 3 4c0-.374.356-.875 1.318-1.313ZM13 5.698V7c0 .374-.356.875-1.318 1.313C10.766 8.729 9.464 9 8 9s-2.766-.27-3.682-.687C3.356 7.875 3 7.373 3 7V5.698c.271.202.58.378.904.525C4.978 6.711 6.427 7 8 7s3.022-.289 4.096-.777A4.92 4.92 0 0 0 13 5.698ZM14 4c0-1.007-.875-1.755-1.904-2.223C11.022 1.289 9.573 1 8 1s-3.022.289-4.096.777C2.875 2.245 2 2.993 2 4v9c0 1.007.875 1.755 1.904 2.223C4.978 15.71 6.427 16 8 16s3.022-.289 4.096-.777C13.125 14.755 14 14.007 14 13V4Zm-1 4.698V10c0 .374-.356.875-1.318 1.313C10.766 11.729 9.464 12 8 12s-2.766-.27-3.682-.687C3.356 10.875 3 10.373 3 10V8.698c.271.202.58.378.904.525C4.978 9.71 6.427 10 8 10s3.022-.289 4.096-.777A4.92 4.92 0 0 0 13 8.698Zm0 3V13c0 .374-.356.875-1.318 1.313C10.766 14.729 9.464 15 8 15s-2.766-.27-3.682-.687C3.356 13.875 3 13.373 3 13v-1.302c.271.202.58.378.904.525C4.978 12.71 6.427 13 8 13s3.022-.289 4.096-.777c.324-.147.633-.323.904-.525Z"/>
                                                    </svg>
                                                </i>
                                            <#else>
                                                <i class="me-1 text-gbif-danger"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.sql'/><br><span class='text-gbif-danger'><@s.text name='manage.source.notReadable'/><span><br>">
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-database" viewBox="0 0 16 16">
                                                        <path d="M4.318 2.687C5.234 2.271 6.536 2 8 2s2.766.27 3.682.687C12.644 3.125 13 3.627 13 4c0 .374-.356.875-1.318 1.313C10.766 5.729 9.464 6 8 6s-2.766-.27-3.682-.687C3.356 4.875 3 4.373 3 4c0-.374.356-.875 1.318-1.313ZM13 5.698V7c0 .374-.356.875-1.318 1.313C10.766 8.729 9.464 9 8 9s-2.766-.27-3.682-.687C3.356 7.875 3 7.373 3 7V5.698c.271.202.58.378.904.525C4.978 6.711 6.427 7 8 7s3.022-.289 4.096-.777A4.92 4.92 0 0 0 13 5.698ZM14 4c0-1.007-.875-1.755-1.904-2.223C11.022 1.289 9.573 1 8 1s-3.022.289-4.096.777C2.875 2.245 2 2.993 2 4v9c0 1.007.875 1.755 1.904 2.223C4.978 15.71 6.427 16 8 16s3.022-.289 4.096-.777C13.125 14.755 14 14.007 14 13V4Zm-1 4.698V10c0 .374-.356.875-1.318 1.313C10.766 11.729 9.464 12 8 12s-2.766-.27-3.682-.687C3.356 10.875 3 10.373 3 10V8.698c.271.202.58.378.904.525C4.978 9.71 6.427 10 8 10s3.022-.289 4.096-.777A4.92 4.92 0 0 0 13 8.698Zm0 3V13c0 .374-.356.875-1.318 1.313C10.766 14.729 9.464 15 8 15s-2.766-.27-3.682-.687C3.356 13.875 3 13.373 3 13v-1.302c.271.202.58.378.904.525C4.978 12.71 6.427 13 8 13s3.022-.289 4.096-.777c.324-.147.633-.323.904-.525Z"/>
                                                    </svg>
                                                </i>
                                            </#if>
                                            <span class="fw-bold overflow-wrap">
                                                ${src.name}
                                            </span><br>
                                            <small>
                                                ${src.columns}&nbsp;<@s.text name='manage.overview.source.columns'/> <span class="fw-bold">|</span>
                                            </small>
                                            <small>
                                                ${(src.lastModified?datetime?string.medium)!lastModifiedNotSet}
                                            </small>
                                        </div>

                                    </#if>
                                    <div class="d-flex justify-content-end my-auto source-item-actions">
                                        <div class="dropdown">
                                            <a class="icon-button icon-material-actions source-item-action" type="button" href="#" id="dropdown-source-item-actions-${src_index}" data-bs-toggle="dropdown" aria-expanded="false">
                                                <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                    <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                                                </svg>
                                            </a>

                                            <ul class="dropdown-menu" aria-labelledby="dropdown-source-item-actions-${src_index}">
                                                <#if src.isFileSource() || src.isExcelSource()>
                                                    <li>
                                                        <a class="dropdown-item action-link" type="button" href="raw-source.do?r=${resource.shortname}&id=${src.name}" target="_blank">
                                                            <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z"></path>
                                                            </svg>
                                                            <@s.text name="button.download"/>
                                                        </a>
                                                    </li>
                                                </#if>
                                                <li>
                                                    <a class="delete-source source-item-action dropdown-item action-link" type="button" href="delete-source.do?r=${resource.shortname}&id=${src.name}">
                                                        <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                        </svg>
                                                        <@s.text name="button.delete"/>
                                                    </a>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </#list>
                    </div>
                </div>
            </#if>
        </div>
    </div>

</div>

<span class="anchor anchor-home-resource-page" id="anchor-mappings"></span>
<div class="py-5 border-bottom section" id="mappings">
    <div class="d-flex justify-content-between">
        <div class="d-flex">
            <h5 class="my-auto text-gbif-header-2 fw-400">
                <#assign mappingsInfo>
                    <@s.text name='manage.overview.DwC.Mappings.coretype.description1'/><br><br><@s.text name='manage.overview.DwC.Mappings.coretype.description2'/><br><br><@s.text name='manage.overview.DwC.Mappings.coretype.description3'/><br><br><@s.text name='manage.overview.DwC.Mappings.coretype.description4'/>
                </#assign>
                <@popoverTextInfo mappingsInfo/>

                <@s.text name='manage.overview.DwC.Mappings'/>
            </h5>
        </div>

        <div class="d-flex justify-content-end">
            <div class="dropdown">
                <a class="icon-button icon-material-actions overview-action-button mapping-action" type="button" href="#" id="dropdown-mapping-actions" data-bs-toggle="dropdown" aria-expanded="false">
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                    </svg>
                </a>

                <ul class="dropdown-menu" aria-labelledby="dropdown-mapping-actions">
                    <li>
                        <a id="add-mapping-button" class="dropdown-item action-link" type="button" href="#">
                            <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                            </svg>
                            <@s.text name="button.add"/>
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </div>

    <#if mappingsModifiedSinceLastPublication || resource.lastPublished??>
        <div class="text-smaller">
            <small>
                <#if mappingsModifiedSinceLastPublication>
                    <@s.text name='manage.home.last.modified'/> ${resource.getMappingsModified()?datetime?string.medium!}
                <#elseif resource.lastPublished??>
                    <@s.text name="manage.overview.notModified"/>
                </#if>
            </small>
        </div>
    </#if>

    <div class="mt-4">
        <p class="mb-0">
            <@s.text name='manage.overview.DwC.Mappings.description'/>
        </p>

        <#if resource.coreRowType?has_content>
            <div class="details mt-3">
                <div class="mapping_head"><@s.text name='manage.overview.DwC.Mappings.cores.select'/></div>
                <div class="row g-2">
                    <#list resource.getMappings(resource.coreRowType) as m>
                        <div class="col-xl-6">
                            <div class="d-flex justify-content-between border rounded-2 mx-1 p-1 py-2 mapping-item text-smaller">
                                <div class="mapping-item-link ps-2" data-ipt-resource="${resource.shortname}" data-ipt-extension="${m.extension.rowType?url}" data-ipt-mapping="${m_index}">
                                    <strong>${(m.source.name)!}</strong>
                                    <i class="bi bi-arrow-right"></i>
                                    <strong>${m.extension.title}</strong>
                                    <br>
                                    <small>${m.fields?size} terms | ${(m.lastModified?datetime?string.medium)!}</small>
                                </div>
                                <div class="my-auto d-flex justify-content-end pt-0">
                                    <div class="dropdown">
                                        <a class="icon-button icon-material-actions mapping-item-action" type="button" href="#" id="dropdown-mapping-item-actions-${m_index}" data-bs-toggle="dropdown" aria-expanded="false">
                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                                            </svg>
                                        </a>

                                        <ul class="dropdown-menu" aria-labelledby="dropdown-mapping-item-actions-${m_index}">
                                            <li>
                                                <a class="dropdown-item action-link peekBtn" type="button" href="mappingPeek.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                                    <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                        <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                    </svg>
                                                    <@s.text name="button.preview"/>
                                                </a>
                                            </li>
                                            <li>
                                                <a class="dropdown-item action-link delete-mapping" type="button" href="delete-mapping.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                                    <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                    <@s.text name="button.delete"/>
                                                </a>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </#list>
                </div>
                <#if (resource.getMappedExtensions()?size > 1)>
                    <div class="mapping_head mt-3"><@s.text name='manage.overview.DwC.Mappings.extensions.select'/></div>
                    <div class="row g-2">
                        <#list resource.getMappedExtensions() as ext>
                            <#if ext.rowType != resource.coreRowType>
                                <#list resource.getMappings(ext.rowType) as m>
                                    <div class="col-xl-6">
                                        <div class="d-flex justify-content-between border rounded-2 mx-1 p-1 py-2 mapping-item text-smaller">
                                            <div class="mapping-item-link ps-2" data-ipt-resource="${resource.shortname}" data-ipt-extension="${ext.rowType?url}" data-ipt-mapping="${m_index}">
                                                <strong>${(m.source.name)!}</strong>
                                                <i class="bi bi-arrow-right"></i>
                                                <strong>${ext.title}</strong>
                                                <br>
                                                <small>${m.fields?size} terms | ${(m.lastModified?datetime?string.medium)!}</small>
                                            </div>
                                            <div class="my-auto d-flex justify-content-end mapping-item-actions">
                                                <div class="dropdown">
                                                    <a class="icon-button icon-material-actions mapping-item-action" type="button" href="#" id="dropdown-mapping-item-actions-${m_index}" data-bs-toggle="dropdown" aria-expanded="false">
                                                        <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                                                        </svg>
                                                    </a>

                                                    <ul class="dropdown-menu" aria-labelledby="dropdown-mapping-item-actions-${m_index}">
                                                        <li>
                                                            <a class="dropdown-item action-link peekBtn" type="button" href="mappingPeek.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
                                                                <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                                </svg>
                                                                <@s.text name="button.preview"/>
                                                            </a>
                                                        </li>
                                                        <li>
                                                            <a class="dropdown-item action-link delete-mapping" type="button" href="delete-mapping.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
                                                                <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                                </svg>
                                                                <@s.text name="button.delete"/>
                                                            </a>
                                                        </li>
                                                    </ul>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </#list>
                            </#if>
                        </#list>
                    </div>
                </#if>
            </div>
        </#if>
    </div>
</div>
