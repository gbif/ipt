<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->

<!-- Represents source data and mapping data sections on resource overview page -->
<span class="anchor anchor-home-resource-page" id="anchor-sources"></span>
<div class="py-5 border-bottom section" id="sources">
    <div class="titleOverview">
        <div class="row">
            <div class="col-9">
                <h5 class="pb-2 mb-0 text-gbif-header-2 fw-400">
                    <#assign sourcesInfo>
                        <@s.text name='manage.overview.source.description1'/>&nbsp;<@s.text name='manage.overview.source.description2'/>&nbsp;<@s.text name='manage.overview.source.description3'><@s.param><@s.text name='button.add'/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description4'><@s.param><@s.text name="button.connectDB"/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description5'/>
                    </#assign>
                    <@popoverTextInfo sourcesInfo/>

                    <@s.text name='manage.overview.source.data'/>
                </h5>
            </div>

            <div class="col-3 d-flex justify-content-end">
                <button id="add-source-button" class="btn btn-sm overview-action-button">
                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                    </svg>
                    <@s.text name='button.add'/>
                </button>
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

        <div class="row mt-4">
            <div class="col-12">
                <div>
                    <p>
                        <@s.text name='manage.overview.source.intro'/>
                    </p>

                    <#if (resource.sources?size>0)>
                        <div class="details">
                            <#list resource.sources as src>
                                <div class="row border rounded-2 mx-1 my-2 p-1 py-2 source-item">
                                    <#if src.isFileSource()>
                                        <div class="col-auto my-auto text-smaller">
                                            <small>
                                                <#if src.readable>
                                                <i class="bi bi-circle-fill text-gbif-primary" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br> "></i>
                                            <#else>
                                                <i class="bi bi-circle-fill text-gbif-danger" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<span class='text-gbif-danger'><@s.text name='manage.source.notReadable'/><span><br> "></i>
                                                </#if>
                                            </small>
                                        </div>
                                        <div class="col-8 col-md-9 px-0 my-auto text-smaller source-item-link" data-ipt-resource="${resource.shortname}" data-ipt-source="${src.name}">
                                            <span class="fw-bold overflow-wrap">
                                                <i class="bi bi-file-text" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.file'/>"></i>&nbsp;${src.name!}
                                            </span><br>
                                            <small>
                                                ${src.fileSizeFormatted} <span class="fw-bold">|</span>
                                                ${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>/${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/> <span class="fw-bold">|</span>
                                            </small>
                                            <small>
                                                ${(src.lastModified?datetime?string.medium)!}
                                            </small>
                                        </div>

                                    <#elseif src.isExcelSource()>
                                        <div class="col-auto my-auto text-smaller source-item-readable">
                                            <small>
                                                <#if src.readable>
                                                    <i class="bi bi-circle-fill text-gbif-primary" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br> "></i>
                                                <#else>
                                                    <i class="bi bi-circle-fill text-gbif-danger" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<span class='text-gbif-danger'><@s.text name='manage.source.notReadable'/><span><br> "></i>
                                                </#if>
                                            </small>
                                        </div>
                                        <div class="col-8 col-md-9 px-0 text-smaller source-item-link" data-ipt-resource="${resource.shortname}" data-ipt-source="${src.name}">
                                            <i class="bi bi-file-excel" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.excel'/>"></i>
                                            <span class="fw-bold">${src.name}</span><br>
                                            <small>
                                                ${src.fileSizeFormatted} <span class="fw-bold">|</span>
                                                ${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>/${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/> <span class="fw-bold">|</span>
                                            </small>
                                            <small>
                                                ${(src.lastModified?datetime?string.medium)!}
                                            </small>
                                        </div>

                                    <#elseif src.isUrlSource()>
                                        <div class="col-auto my-auto text-smaller">
                                            <small>
                                                <#if src.readable>
                                                    <i class="bi bi-circle-fill text-gbif-primary" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br> "></i>
                                                <#else>
                                                    <i class="bi bi-circle-fill text-gbif-danger" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<span class='text-gbif-danger'><@s.text name='manage.source.notReadable'/><span><br> "></i>
                                                </#if>
                                            </small>
                                        </div>
                                        <div class="col-8 col-md-9 px-0 text-smaller source-item-link" data-ipt-resource="${resource.shortname}" data-ipt-source="${src.name}">
                                            <i class="bi bi-link" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.url'/>"></i>
                                            <span class="fw-bold">${src.name}</span><br>
                                            <small>
                                                ${src.fileSizeFormatted} <span class="fw-bold">|</span>
                                                ${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>/${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/> <span class="fw-bold">|</span>
                                            </small>
                                            <small>
                                                ${(src.lastModified?datetime?string.medium)!}
                                            </small>
                                        </div>

                                    <#else>
                                        <div class="col-auto my-auto text-smaller">
                                            <small>
                                                <#if src.readable>
                                                    <i class="bi bi-circle-fill text-gbif-primary" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<span class='text-gbif-primary'><@s.text name='manage.source.readable'/><span><br> "></i>
                                                <#else>
                                                    <i class="bi bi-circle-fill text-gbif-danger" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<span class='text-gbif-danger'><@s.text name='manage.source.notReadable'/><span><br> "></i>
                                                </#if>
                                            </small>
                                        </div>
                                        <div class="col-8 col-md-9 px-0 text-smaller source-item-link" data-ipt-resource="${resource.shortname}" data-ipt-source="${src.name}">
                                            <i class="bi bi-server" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-html="true" title="<@s.text name='manage.overview.source.sql'/>"></i>
                                            <span class="fw-bold">${src.name}</span><br>
                                            <small>
                                                ${src.columns}&nbsp;<@s.text name='manage.overview.source.columns'/>
                                            </small>
                                        </div>

                                    </#if>
                                    <div class="col-2 ms-auto d-flex justify-content-end my-auto source-item-actions">
                                        <#if src.isFileSource() || src.isExcelSource()>
                                            <a class="icon-button icon-button-sm source-item-action" type="button" href="raw-source.do?r=${resource.shortname}&id=${src.name}" target="_blank">
                                                <svg class="icon-button-svg icon-material-download" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                    <path d="M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z"></path>
                                                </svg>
                                            </a>
                                        </#if>
                                        <a class="icon-button icon-button-sm icon-material-delete delete-source source-item-action" type="button" href="delete-source.do?r=${resource.shortname}&id=${src.name}">
                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                            </svg>
                                        </a>
                                    </div>

                                </div>
                            </#list>
                        </div>
                    </#if>
                </div>
            </div>
        </div>

    </div>

</div>

<span class="anchor anchor-home-resource-page" id="anchor-mappings"></span>
<div class="py-5 border-bottom section" id="mappings">
    <div class="row">
        <div class="col-9">
            <h5 class="pb-2 mb-0 text-gbif-header-2 fw-400">
                <#assign mappingsInfo>
                    <@s.text name='manage.overview.DwC.Mappings.coretype.description1'/><br><br><@s.text name='manage.overview.DwC.Mappings.coretype.description2'/><br><br><@s.text name='manage.overview.DwC.Mappings.coretype.description3'/><br><br><@s.text name='manage.overview.DwC.Mappings.coretype.description4'/>
                </#assign>
                <@popoverTextInfo mappingsInfo/>

                <@s.text name='manage.overview.DwC.Mappings'/>
            </h5>
        </div>

        <div class="col-3 d-flex justify-content-end">
            <button id="add-mapping-button" class="btn btn-sm overview-action-button">
                <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                    <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                </svg>
                <@s.text name='button.add'/>
            </button>
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

    <div class="row mt-4">
        <div class="col-12">
            <div>
                <p>
                    <@s.text name='manage.overview.DwC.Mappings.description'/>
                </p>

                <#if !(potentialCores?size>0)>
                    <div class="callout callout-warning text-smaller">
                        <@s.text name="manage.overview.DwC.Mappings.cantdo"/>
                    </div>
                </#if>

                <#if resource.coreRowType?has_content>
                    <div class="details">
                        <div class="mapping_head"><@s.text name='manage.overview.DwC.Mappings.cores.select'/></div>
                            <#list resource.getMappings(resource.coreRowType) as m>
                                <div class="row border rounded-2 mx-1 my-2 p-1 py-2 mapping-item text-smaller">
                                    <div class="col-10 mapping-item-link" data-ipt-resource="${resource.shortname}" data-ipt-extension="${m.extension.rowType?url}" data-ipt-mapping="${m_index}">
                                        <strong>${(m.source.name)!}</strong>
                                        <i class="bi bi-arrow-right"></i>
                                        <strong>${m.extension.title}</strong>
                                        <br>
                                        <small>${m.fields?size} terms | ${(m.lastModified?datetime?string.medium)!}</small>
                                    </div>
                                    <div class="col-2 my-auto d-flex justify-content-end pt-0">
                                        <a class="icon-button icon-button-sm peekBtn" type="button" href="mappingPeek.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                            </svg>
                                        </a>
                                        <a class="icon-button icon-button-sm icon-material-delete delete-mapping" type="button" href="delete-mapping.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                            </svg>
                                        </a>
                                    </div>
                                </div>
                            </#list>
                        <#if (resource.getMappedExtensions()?size > 1)>
                            <div class="mapping_head mt-3"><@s.text name='manage.overview.DwC.Mappings.extensions.select'/></div>
                            <#list resource.getMappedExtensions() as ext>
                                <#if ext.rowType != resource.coreRowType>
                                    <#list resource.getMappings(ext.rowType) as m>
                                        <div class="row border rounded-2 mx-1 my-2 p-1 py-2 mapping-item text-smaller">
                                            <div class="col-10 mapping-item-link" data-ipt-resource="${resource.shortname}" data-ipt-extension="${ext.rowType?url}" data-ipt-mapping="${m_index}">
                                                <strong>${(m.source.name)!}</strong>
                                                <i class="bi bi-arrow-right"></i>
                                                <strong>${ext.title}</strong>
                                                <br>
                                                <small>${m.fields?size} terms | ${(m.lastModified?datetime?string.medium)!}</small>
                                            </div>
                                            <div class="col-2 my-auto d-flex justify-content-end">
                                                <a class="icon-button icon-button-sm peekBtn" type="button" href="mappingPeek.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
                                                    <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                        <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                    </svg>
                                                </a>
                                                <a class="icon-button icon-button-sm icon-material-delete delete-mapping" type="button" href="delete-mapping.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
                                                    <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </a>
                                            </div>
                                        </div>
                                    </#list>
                                </#if>
                            </#list>
                        </#if>
                    </div>
                </#if>
            </div>
        </div>
    </div>
</div>
