<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->
<#assign lastModifiedNotSet><@s.text name='basic.lastModified.not.set'/></#assign>

<!-- Represents source data section on resource overview page -->
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
                <a id="add-source-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button source-action" type="button" href="#">
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                    </svg>
                    <@s.text name="button.add"/>
                </a>
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
                <#if dataPackageResource>
                    <@s.text name='manage.overview.source.dataSchema.intro'/>
                <#else>
                    <@s.text name='manage.overview.source.intro'/>
                </#if>
            </p>

            <#if (resource.sources?size>0)>
                <div class="details mt-3">
                    <div class="row g-2">
                        <#list resource.sources as src>
                            <div class="col-xl-6">
                                <div class="d-flex justify-content-between border rounded-2 mx-1 p-1 py-2 source-item">
                                    <#if src.isFileSource()>
                                        <div class="fs-smaller-2 source-item-link ps-2" data-ipt-resource="${resource.shortname}" data-ipt-source="${src.name}">
                                            <#if src.readable>
                                                <i class="bi bi-file-text me-1 text-gbif-primary"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.file'/><br><span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br>"></i>
                                            <#else>
                                                <i class="bi bi-file-text me-1 text-gbif-danger"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.file'/><br><span class='text-gbif-danger'><@s.text name='manage.source.notReadable'/><span><br>"></i>
                                            </#if>
                                            <span class="fs-smaller fw-bold overflow-wrap">${src.name!}</span><br>
                                            <small>
                                                ${src.fileSizeFormatted} <span class="fw-bold">|</span>
                                                ${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>/${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/> <span class="fw-bold">|</span>
                                            </small>
                                            <small>
                                                ${(src.lastModified?datetime?string.medium)!lastModifiedNotSet}
                                            </small>
                                        </div>

                                    <#elseif src.isExcelSource()>
                                        <div class="fs-smaller-2 source-item-link ps-2" data-ipt-resource="${resource.shortname}" data-ipt-source="${src.name}">
                                            <#if src.readable>
                                                <i class="bi bi-file-excel me-1 text-gbif-primary"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.excel'/><br><span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br>"></i>
                                            <#else>
                                                <i class="bi bi-file-excel me-1 text-gbif-danger"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.excel'/><br><span class='text-gbif-danger'><@s.text name='manage.source.notReadable'/><span><br>"></i>
                                            </#if>
                                            <span class="fs-smaller fw-bold overflow-wrap">${src.name}</span><br>
                                            <small>
                                                ${src.fileSizeFormatted} <span class="fw-bold">|</span>
                                                ${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>/${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/> <span class="fw-bold">|</span>
                                            </small>
                                            <small>
                                                ${(src.lastModified?datetime?string.medium)!lastModifiedNotSet}
                                            </small>
                                        </div>

                                    <#elseif src.isUrlSource()>
                                        <div class="fs-smaller-2 source-item-link ps-2" data-ipt-resource="${resource.shortname}" data-ipt-source="${src.name}">
                                            <#if src.readable>
                                                <i class="bi bi-link me-1 text-gbif-primary"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.url'/><br><span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br>"></i>
                                            <#else>
                                                <i class="bi bi-link me-1 text-gbif-danger"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.url'/><br><span class='text-gbif-danger'><@s.text name='manage.source.notReadable'/><span><br>"></i>
                                            </#if>
                                            <span class="fs-smaller fw-bold overflow-wrap">${src.name}</span><br>
                                            <small>
                                                ${src.fileSizeFormatted} <span class="fw-bold">|</span>
                                                ${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>/${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/> <span class="fw-bold">|</span>
                                            </small>
                                            <small>
                                                ${(src.lastModified?datetime?string.medium)!lastModifiedNotSet}
                                            </small>
                                        </div>

                                    <#else>
                                        <div class="fs-smaller-2 source-item-link ps-2" data-ipt-resource="${resource.shortname}" data-ipt-source="${src.name}">
                                            <#if src.readable>
                                                <i class="bi bi-database me-1 text-gbif-primary"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.sql'/><br><span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br>"></i>
                                            <#else>
                                                <i class="bi bi-database me-1 text-gbif-danger"  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.sql'/><br><span class='text-gbif-danger'><@s.text name='manage.source.notReadable'/><span><br>"></i>
                                            </#if>
                                            <span class="fs-smaller fw-bold overflow-wrap">
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
                                        <a title="<@s.text name="button.edit"/>" class="icon-button icon-material-actions source-item-action fs-smaller-2 d-sm-max-none" type="button" href="source.do?r=${resource.shortname}&id=${src.name}">
                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                            </svg>
                                        </a>
                                        <#if src.isFileSource() || src.isExcelSource()>
                                            <a title="<@s.text name="button.download"/>" class="icon-button icon-material-actions source-item-action fs-smaller-2 d-sm-max-none" type="button" href="raw-source.do?r=${resource.shortname}&id=${src.name}">
                                                <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                    <path d="M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z"></path>
                                                </svg>
                                            </a>
                                        </#if>
                                        <a title="<@s.text name="button.delete"/>" class="delete-source icon-button icon-material-actions source-item-action fs-smaller-2 d-sm-max-none" type="button" href="delete-source.do?r=${resource.shortname}&id=${src.name}">
                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                            </svg>
                                        </a>
                                        <div class="dropdown d-sm-none">
                                            <a class="icon-button icon-material-actions source-item-action" type="button" href="#" id="dropdown-source-item-actions-${src_index}" data-bs-toggle="dropdown" aria-expanded="false">
                                                <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                    <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path>
                                                </svg>
                                            </a>

                                            <ul class="dropdown-menu" aria-labelledby="dropdown-source-item-actions-${src_index}">
                                                <#if src.isFileSource() || src.isExcelSource()>
                                                    <li>
                                                        <a class="dropdown-item action-link" type="button" href="raw-source.do?r=${resource.shortname}&id=${src.name}" target="_blank">
                                                            <svg class="overview-item-dropdown-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z"></path>
                                                            </svg>
                                                            <@s.text name="button.download"/>
                                                        </a>
                                                    </li>
                                                </#if>
                                                <li>
                                                    <a class="delete-source source-item-action dropdown-item action-link" type="button" href="delete-source.do?r=${resource.shortname}&id=${src.name}">
                                                        <svg class="overview-item-dropdown-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
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

<!-- Represents mapping data section on resource overview page -->
<span class="anchor anchor-home-resource-page" id="anchor-mappings"></span>
<div class="py-5 border-bottom section" id="mappings">
    <#if dataPackageResource>
        <h5 class="pb-2 mb-4 text-gbif-header-2 fw-400">
            <#assign mappingsInfo>
                <@s.text name='manage.overview.mappings.info'/>
            </#assign>
            <@popoverTextInfo mappingsInfo/>

            <@s.text name='manage.overview.mappings'/>
        </h5>

        <div class="row">
            <div class="col-lg-9 ps-lg-5 order-lg-last">
                <div>
                    <p>
                        <@s.text name='manage.overview.mappings.description'/>
                    </p>

                    <#if !(potentialCores?size>0)>
                        <div class="callout callout-warning text-smaller">
                            <@s.text name="manage.overview.mappings.cantdo"/>
                        </div>
                    </#if>

                    <div class="details mb-3">
                        <#if mappingsModifiedSinceLastPublication>
                            <@s.text name='manage.home.last.modified'/> ${resource.getMappingsModified()?datetime?string.medium!}
                        <#elseif resource.lastPublished??>
                            <@s.text name="manage.overview.notModified"/>
                        </#if>
                    </div>

                    <#if resource.schemaIdentifier?has_content>
                        <div class="details">
                            <div class="table-responsive">
                                <table class="table table-sm table-borderless text-smaller">
                                    <#list resource.getDataSchemaMappings() as m>
                                        <tr <#if m_index==0>class="mapping_row"</#if>>
                                            <th class="col-4">${m.dataSchemaFile!}</th>
                                            <td>
                                                ${m.fieldsMapped!} <@s.text name='manage.overview.mappings.fields.mapped'/> <a class="fw-bold" style="color:#4e565f !important;" href="source.do?r=${resource.shortname}&id=${m.source.name}">${(m.source.name)!}</a><br>
                                                ${(m.lastModified?datetime?string.medium)!}
                                            </td>
                                            <td class="d-flex justify-content-end py-0">
                                                <a class="icon-button icon-button-sm" type="button" href="schemaMapping.do?r=${resource.shortname}&id=${m.dataSchema.identifier?url}&mid=${m_index}">
                                                    <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24" data-testid="EditIcon" aria-label="fontSize large">
                                                        <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                                    </svg>
                                                </a>
                                                <a class="icon-button icon-button-sm icon-material-delete delete-mapping" type="button" href="delete-schemaMapping.do?r=${resource.shortname}&id=${m.dataSchema.identifier?url}&mid=${m_index}">
                                                    <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </a>
                                            </td>
                                        </tr>
                                    </#list>
                                </table>
                            </div>
                        </div>
                    </#if>
                </div>
            </div>

            <div class="col-lg-3 border-lg-right pe-lg-5 rounded">
                <div>
                    <form action='schemaMapping.do' method='post'>
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <input name="id" type="hidden" value="${resource.schemaIdentifier}">
                        <@s.submit name="add" cssClass="btn btn-sm btn-outline-gbif-primary my-1" key="button.add"/>
                    </form>
                </div>
            </div>
        </div>
    <#else>
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
                <a id="add-mapping-button" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button mapping-action" type="button" href="#">
                    <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                    </svg>
                    <@s.text name="button.add"/>
                </a>
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
                                    <div class="fs-smaller-2 mapping-item-link ps-2" data-ipt-resource="${resource.shortname}" data-ipt-extension="${m.extension.rowType?url}" data-ipt-mapping="${m_index}">
                                        <strong class="fs-smaller">${(m.source.name)!}</strong>
                                        <i class="bi bi-arrow-right"></i>
                                        <strong class="fs-smaller">${m.extension.title}</strong>
                                        <br>
                                        <small>${m.fields?size} terms | ${(m.lastModified?datetime?string.medium)!lastModifiedNotSet}</small>
                                    </div>
                                    <div class="my-auto d-flex justify-content-end pt-0">
                                        <a title="<@s.text name="button.edit"/>" class="icon-button icon-material-actions mapping-item-action fs-smaller-2 d-sm-max-none" type="button" href="mapping.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                            </svg>
                                        </a>
                                        <a title="<@s.text name="button.preview"/>" class="peekBtn icon-button icon-material-actions mapping-item-action fs-smaller-2 d-sm-max-none" type="button" href="mappingPeek.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                            <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                            </svg>
                                        </a>
                                        <a title="<@s.text name="button.delete"/>" class="delete-source icon-button icon-material-actions mapping-item-action fs-smaller-2 d-sm-max-none" type="button" href="delete-mapping.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                            </svg>
                                        </a>

                                        <div class="dropdown d-sm-none">
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
                                                <div class="fs-smaller-2 mapping-item-link ps-2" data-ipt-resource="${resource.shortname}" data-ipt-extension="${ext.rowType?url}" data-ipt-mapping="${m_index}">
                                                    <strong class="fs-smaller">${(m.source.name)!}</strong>
                                                    <i class="bi bi-arrow-right"></i>
                                                    <strong class="fs-smaller">${ext.title}</strong>
                                                    <br>
                                                    <small>${m.fields?size} terms | ${(m.lastModified?datetime?string.medium)!lastModifiedNotSet}</small>
                                                </div>
                                                <div class="my-auto d-flex justify-content-end mapping-item-actions">
                                                    <a title="<@s.text name="button.edit"/>" class="icon-button icon-material-actions mapping-item-action fs-smaller-2 d-sm-max-none" type="button" href="mapping.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
                                                        <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                                        </svg>
                                                    </a>
                                                    <a title="<@s.text name="button.preview"/>" class="peekBtn icon-button icon-material-actions mapping-item-action fs-smaller-2 d-sm-max-none" type="button" href="mappingPeek.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
                                                        <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <svg class="overview-item-action-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                            </svg>
                                                        </svg>
                                                    </a>
                                                    <a title="<@s.text name="button.delete"/>" class="delete-mapping icon-button icon-material-actions mapping-item-action fs-smaller-2 d-sm-max-none" type="button" href="delete-mapping.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
                                                        <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                        </svg>
                                                    </a>

                                                    <div class="dropdown d-sm-none">
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
    </#if>
</div>
