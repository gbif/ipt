<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.OverviewAction" -->

<!-- Represents source data and mapping data sections on resource overview page -->
<span class="anchor anchor-home-resource-page" id="anchor-sources"></span>
<div class="py-5 border-bottom section" id="sources">
    <div class="titleOverview">
        <h5 class="pb-2 mb-4 text-gbif-header-2 fw-400">
            <#assign sourcesInfo>
                <@s.text name='manage.overview.source.description1'/>&nbsp;<@s.text name='manage.overview.source.description2'/>&nbsp;<@s.text name='manage.overview.source.description3'><@s.param><@s.text name='button.add'/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description4'><@s.param><@s.text name="button.connectDB"/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description5'/>
            </#assign>
            <@popoverTextInfo sourcesInfo/>

            <@s.text name='manage.overview.source.data'/>
        </h5>

        <div class="row">
            <div class="col-lg-3 border-lg-right border-lg-max py-lg-max-2 pe-lg-5 mb-4 rounded">
                <div class="">
                    <form action='addsource.do' method='post' enctype="multipart/form-data">
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <input name="validate" type="hidden" value="false"/>

                        <select id="sourceType" name="sourceType" class="form-select form-select-sm my-1">
                            <option value="source-sql" selected><@s.text name='manage.source.database'/></option>
                            <option value="source-file"><@s.text name='manage.source.file'/></option>
                            <option value="source-url"><@s.text name='manage.source.url'/></option>
                        </select>

                        <div class="row">
                            <div class="col-12">
                                <@s.file name="file" cssClass="form-control form-control-sm my-1" cssStyle="display: none;" key="manage.resource.create.file"/>
                                <input type="text" id="sourceName" name="sourceName" class="form-control form-control-sm my-1" placeholder="<@s.text name='source.name'/>" style="display: none">
                                <input type="url" id="url" name="url" class="form-control form-control-sm my-1" placeholder="URL" style="display: none">
                            </div>
                            <div class="col-12">
                                <@s.submit name="add" cssClass="btn btn-sm btn-outline-gbif-primary my-1" key="button.connect"/>
                                <@s.submit name="clear" cssClass="btn btn-sm btn-outline-secondary my-1" cssStyle="display: none" key="button.clear"/>
                            </div>
                        </div>
                    </form>
                    <form action='canceloverwrite.do' method='post'>
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <input name="validate" type="hidden" value="false"/>
                        <@s.submit name="canceloverwrite" key="button.cancel" cssStyle="display: none;" cssClass="btn btn-sm btn-outline-secondary my-1"/>
                    </form>
                </div>
            </div>

            <div class="col-lg-9 ps-lg-5">
                <div>
                    <p>
                        <#if dataSchemaBased>
                            <@s.text name='manage.overview.source.dataSchema.intro'/>
                        <#else>
                            <@s.text name='manage.overview.source.intro'/>
                        </#if>
                    </p>

                    <div class="details mb-3">
                        <#if sourcesModifiedSinceLastPublication>
                            <@s.text name='manage.home.last.modified'/> ${resource.getSourcesModified()?datetime?string.medium!}
                        <#elseif resource.lastPublished??>
                            <@s.text name="manage.overview.notModified"/>
                        </#if>
                    </div>

                    <#if (resource.sources?size>0)>
                        <div class="details">
                            <div class="table-responsive">
                                <table class="table table-sm table-borderless text-smaller">
                                    <#list resource.sources as src>
                                        <tr>
                                            <#if src.isFileSource()>
                                                <th class="col-4">${src.name} <@s.text name='manage.overview.source.file'/></th>
                                                <td>
                                                    ${src.fileSizeFormatted},&nbsp;${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>,&nbsp;${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/><br>
                                                    ${(src.lastModified?datetime?string.medium)!}<br>
                                                    <div>
                                                        <@s.text name='manage.source.readable'/>&nbsp;
                                                        <#if src.readable>
                                                            <svg class="icon-button-svg icon-material-check text-gbif-primary pb-1" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"></path>
                                                            </svg>
                                                        <#else>
                                                            <svg class="icon-button-svg icon-material-close text-gbif-danger pb-1" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"></path>
                                                            </svg>
                                                        </#if>
                                                    </div>
                                                </td>
                                            <#elseif src.isExcelSource()>
                                                <th class="col-4">${src.name} <@s.text name='manage.overview.source.excel'/></th>
                                                <td>
                                                    ${src.fileSizeFormatted},&nbsp;${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>,&nbsp;${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/><br>
                                                    ${(src.lastModified?datetime?string.medium)!}<br>
                                                    <@s.text name='manage.source.readable'/>&nbsp;
                                                    <#if src.readable>
                                                        <svg class="icon-button-svg icon-material-check text-gbif-primary pb-1" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"></path>
                                                        </svg>
                                                    <#else>
                                                        <svg class="icon-button-svg icon-material-close text-gbif-danger pb-1" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"></path>
                                                        </svg>
                                                    </#if>
                                                </td>
                                            <#elseif src.isUrlSource()>
                                                <th class="col-4">${src.name} <@s.text name='manage.overview.source.url'/></th>
                                                <td>
                                                    ${src.url!"..."}<br>
                                                    ${src.fileSizeFormatted},&nbsp;${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>,&nbsp;${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/><br>
                                                    ${(src.lastModified?datetime?string.medium)!}<br>
                                                    <@s.text name='manage.source.readable'/>&nbsp;
                                                    <#if src.readable>
                                                        <svg class="icon-button-svg icon-material-check text-gbif-primary pb-1" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"></path>
                                                        </svg>
                                                    <#else>
                                                        <svg class="icon-button-svg icon-material-close text-gbif-danger pb-1" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"></path>
                                                        </svg>
                                                    </#if>
                                                </td>
                                            <#else>
                                                <th class="col-4">${src.name} <@s.text name='manage.overview.source.sql'/></th>
                                                <td>
                                                    ${src.database!"..."}<br>
                                                    ${src.columns}&nbsp;<@s.text name='manage.overview.source.columns'/><br>
                                                    <@s.text name='manage.source.readable'/>&nbsp;
                                                    <#if src.readable>
                                                        <svg class="icon-button-svg icon-material-check text-gbif-primary pb-1" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"></path>
                                                        </svg>
                                                    <#else>
                                                        <svg class="icon-button-svg icon-material-close text-gbif-danger pb-1" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"></path>
                                                        </svg>
                                                    </#if>
                                                </td>
                                            </#if>
                                            <td class="d-flex justify-content-end py-0">
                                                <div>
                                                    <#if src.isFileSource() || src.isExcelSource()>
                                                        <a class="icon-button icon-button-sm" type="button" href="raw-source.do?r=${resource.shortname}&id=${src.name}" target="_blank">
                                                            <svg class="icon-button-svg icon-material-download" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                <path d="M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z"></path>
                                                            </svg>
                                                        </a>
                                                    </#if>
                                                    <a class="icon-button icon-button-sm" type="button" href="source.do?r=${resource.shortname}&id=${src.name}">
                                                        <svg class="icon-button-svg icon-material-edit" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                                        </svg>
                                                    </a>
                                                </div>
                                            </td>
                                        </tr>
                                    </#list>
                                </table>
                            </div>
                        </div>
                    </#if>
                </div>
            </div>
        </div>

    </div>

</div>

<span class="anchor anchor-home-resource-page" id="anchor-mappings"></span>
<div class="py-5 border-bottom section" id="mappings">
    <#if dataSchemaBased>
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
                        <p class="text-gbif-warning fst-italic">
                            <i class="bi bi-exclamation-triangle"></i>
                            <@s.text name="manage.overview.mappings.cantdo"/>
                        </p>
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
                                            <th class="col-4"><#if m_index==0>${m.dataSchema.title}</#if></th>
                                            <td>${m.fields?size} <@s.text name='manage.overview.mappings.fields.mapped'/> ${(m.source.name)!}.&nbsp;${(m.lastModified?datetime?string.medium)!}</td>
                                            <td class="d-flex justify-content-end py-0">
                                                <div>
                                                    <a class="icon-button icon-button-sm" type="button" href="schemaMapping.do?r=${resource.shortname}&id=${m.dataSchema.identifier?url}&mid=${m_index}">
                                                        <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24" data-testid="EditIcon" aria-label="fontSize large">
                                                            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                                        </svg>
                                                    </a>
                                                </div>
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
        <h5 class="pb-2 mb-4 text-gbif-header-2 fw-400">
            <#assign mappingsInfo>
                <@s.text name='manage.overview.DwC.Mappings.coretype.description1'/><br><br><@s.text name='manage.overview.DwC.Mappings.coretype.description2'/><br><br><@s.text name='manage.overview.DwC.Mappings.coretype.description3'/><br><br><@s.text name='manage.overview.DwC.Mappings.coretype.description4'/>
            </#assign>
            <@popoverTextInfo mappingsInfo/>

            <@s.text name='manage.overview.DwC.Mappings'/>
        </h5>

        <div class="row">
            <div class="col-lg-3 border-lg-right <#if (potentialCores?size>0)>border-lg-max py-lg-max-2 mb-4</#if> pe-lg-5 rounded">
                <div>
                    <#if (potentialCores?size>0)>
                        <form action='mapping.do' method='post'>
                            <input name="r" type="hidden" value="${resource.shortname}"/>
                            <select name="id" class="form-select form-select-sm my-1" id="rowType" size="1">
                                <optgroup label="<@s.text name='manage.overview.DwC.Mappings.cores.select'/>">
                                    <#list potentialCores as c>
                                        <#if c?has_content>
                                            <option value="${c.rowType}">${c.title}</option>
                                        </#if>
                                    </#list>
                                </optgroup>
                                <#if (potentialExtensions?size>0)>
                                    <optgroup label="<@s.text name='manage.overview.DwC.Mappings.extensions.select'/>">
                                        <#list potentialExtensions as e>
                                            <#if e?has_content>
                                                <option value="${e.rowType}">${e.title}</option>
                                            </#if>
                                        </#list>
                                    </optgroup>
                                </#if>
                            </select>
                            <@s.submit name="add" cssClass="btn btn-sm btn-outline-gbif-primary my-1" key="button.add"/>
                        </form>
                    </#if>
                </div>
            </div>

            <div class="col-lg-9 ps-lg-5">
                <div>
                    <p>
                        <@s.text name='manage.overview.DwC.Mappings.description'/>
                    </p>

                    <#if !(potentialCores?size>0)>
                        <p class="text-gbif-warning fst-italic">
                            <i class="bi bi-exclamation-triangle"></i>
                            <@s.text name="manage.overview.DwC.Mappings.cantdo"/>
                        </p>
                    </#if>

                    <div class="details mb-3">
                        <#if mappingsModifiedSinceLastPublication>
                            <@s.text name='manage.home.last.modified'/> ${resource.getMappingsModified()?datetime?string.medium!}
                        <#elseif resource.lastPublished??>
                            <@s.text name="manage.overview.notModified"/>
                        </#if>
                    </div>

                    <#if resource.coreRowType?has_content>
                        <div class="details">
                            <div class="mapping_head"><@s.text name='manage.overview.DwC.Mappings.cores.select'/></div>
                            <div class="table-responsive">
                                <table class="table table-sm table-borderless text-smaller">
                                    <#list resource.getMappings(resource.coreRowType) as m>
                                        <tr <#if m_index==0>class="mapping_row"</#if>>
                                            <th class="col-4"><#if m_index==0>${m.extension.title}</#if></th>
                                            <td>${m.fields?size} <@s.text name='manage.overview.DwC.Mappings.terms'/> ${(m.source.name)!}.&nbsp;${(m.lastModified?datetime?string.medium)!}</td>
                                            <td class="d-flex justify-content-end pt-0">
                                                <div>
                                                    <a class="icon-button icon-button-sm peekBtn me-1" type="button" href="mappingPeek.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                                        <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                        </svg>
                                                    </a>
                                                    <a class="icon-button icon-button-sm" type="button" href="mapping.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                                        <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                                        </svg>
                                                    </a>
                                                </div>
                                            </td>
                                        </tr>
                                    </#list>
                                </table>
                            </div>
                            <#if (resource.getMappedExtensions()?size > 1)>
                                <div class="mapping_head"><@s.text name='manage.overview.DwC.Mappings.extensions.select'/></div>
                                <div class="table-responsive">
                                    <table class="table table-sm table-borderless text-smaller">
                                        <#list resource.getMappedExtensions() as ext>
                                            <#if ext.rowType != resource.coreRowType>
                                                <#list resource.getMappings(ext.rowType) as m>
                                                    <tr <#if m_index==0>class="mapping_row"</#if>>
                                                        <th class="col-4"><#if m_index==0>${ext.title}</#if></th>
                                                        <td>${m.fields?size} <@s.text name='manage.overview.DwC.Mappings.terms'/> ${(m.source.name)!}.&nbsp;${(m.lastModified?datetime?string.medium)!}</td>
                                                        <td class="d-flex justify-content-end">
                                                            <div>
                                                                <a class="icon-button icon-button-sm peekBtn me-1" type="button" href="mappingPeek.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
                                                                    <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                        <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                                                    </svg>
                                                                </a>
                                                                <a class="icon-button icon-button-sm" type="button" href="mapping.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
                                                                    <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                                        <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"></path>
                                                                    </svg>
                                                                </a>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </#list>
                                            </#if>
                                        </#list>
                                    </table>
                                </div>
                            </#if>
                        </div>
                    </#if>
                </div>
            </div>
        </div>
    </#if>
</div>
