<!-- Represents source data and mapping data sections on resource overview page -->
<div class="my-3 p-3 bg-body rounded shadow-sm" id="sources">
    <div class="titleOverview">
        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-gbif-header">
            <#assign sourcesInfo>
                <@s.text name='manage.overview.source.description1'/>&nbsp;<@s.text name='manage.overview.source.description2'/>&nbsp;<@s.text name='manage.overview.source.description3'><@s.param><@s.text name='button.add'/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description4'><@s.param><@s.text name="button.connectDB"/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description5'/>
            </#assign>
            <@popoverTextInfo sourcesInfo/>

            <@s.text name='manage.overview.source.data'/>
        </h5>

        <div class="row">
            <div class="col-lg-9 order-lg-last">
                <div class="mx-md-4 mx-2">
                    <p>
                        <@s.text name='manage.overview.source.intro'/>
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
                                                <@s.text name='manage.source.readable'/>&nbsp;<#if src.readable><i class="bi bi-circle-fill text-gbif-primary"></i><#else><i class="bi bi-circle-fill text-gbif-danger"></i></#if>
                                            </td>
                                        <#elseif src.isExcelSource()>
                                            <th class="col-4">${src.name} <@s.text name='manage.overview.source.excel'/></th>
                                            <td>
                                                ${src.fileSizeFormatted},&nbsp;${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>,&nbsp;${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/><br>
                                                ${(src.lastModified?datetime?string.medium)!}<br>
                                                <@s.text name='manage.source.readable'/>&nbsp;<#if src.readable><i class="bi bi-circle-fill text-gbif-primary"></i><#else><i class="bi bi-circle-fill text-gbif-danger"></i></#if>
                                            </td>
                                        <#elseif src.isUrlSource()>
                                            <th class="col-4">${src.name} <@s.text name='manage.overview.source.url'/></th>
                                            <td>
                                                ${src.url!"..."}<br>
                                                ${src.fileSizeFormatted},&nbsp;${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>,&nbsp;${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/><br>
                                                ${(src.lastModified?datetime?string.medium)!}<br>
                                                <@s.text name='manage.source.readable'/>&nbsp;<#if src.readable><i class="bi bi-circle-fill text-gbif-primary"></i><#else><i class="bi bi-circle-fill text-gbif-danger"></i></#if>
                                            </td>
                                        <#else>
                                            <th class="col-4">${src.name} <@s.text name='manage.overview.source.sql'/></th>
                                            <td>
                                                ${src.database!"..."}<br>
                                                ${src.columns}&nbsp;<@s.text name='manage.overview.source.columns'/><br>
                                                <@s.text name='manage.source.readable'/>&nbsp;<#if src.readable><i class="bi bi-circle-fill text-gbif-primary"></i><#else><i class="bi bi-circle-fill text-gbif-danger"></i></#if>
                                            </td>
                                        </#if>
                                        <td class="d-flex justify-content-end">
                                          <div class="btn-group" role="group">
                                            <#if src.isFileSource() || src.isExcelSource()>
                                            <a class="btn btn-sm btn-outline-secondary" role="button" href="raw-source.do?r=${resource.shortname}&id=${src.name}" target="_blank" title="<@s.text name='manage.overview.source.download'/>">
                                              <i class="bi bi-download"></i>
                                            </a>
                                            </#if>
                                            <a class="btn btn-sm btn-outline-secondary" role="button" href="source.do?r=${resource.shortname}&id=${src.name}">
                                              <@s.text name='button.edit'/>
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

            <div class="col-lg-3 border-lg-right">
                <div class="mx-md-4 mx-2">
                    <form action='addsource.do' method='post' enctype="multipart/form-data">
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <input name="validate" type="hidden" value="false"/>

                        <select id="sourceType" name="sourceType" class="form-select form-select-sm">
                            <option value="source-sql" selected><@s.text name='manage.source.database'/></option>
                            <option value="source-file"><@s.text name='manage.source.file'/></option>
                            <option value="source-url"><@s.text name='manage.source.url'/></option>
                        </select>

                        <div class="row">
                            <div class="col-12">
                                <@s.file name="file" cssClass="form-control form-control-sm my-1" cssStyle="display: none;" key="manage.resource.create.file"/>
                                <input type="url" id="url" name="url" class="form-control form-control-sm my-1" style="display: none">
                            </div>
                            <div class="col-12">
                                <@s.submit name="add" cssClass="btn btn-sm btn-outline-gbif-primary my-1" key="button.connect"/>
                                <@s.submit name="clear" cssClass="btn btn-sm btn-outline-secondary my-1" cssStyle="display: none" key="button.clear"/>
                            </div>
                        </div>
                    </form>
                    <form action='canceloverwrite.do' method='post'>
                        <div style="display: none;">
                            <@s.submit name="canceloverwrite" key="button.cancel" cssClass="btn btn-sm btn-outline-secondary my-1"/>
                        </div>
                    </form>
                </div>
            </div>
        </div>

    </div>

</div>

<div class="my-3 p-3 bg-body rounded shadow-sm" id="mappings">
    <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-gbif-header">
        <#assign mappingsInfo>
            <@s.text name='manage.overview.source.description1'/>&nbsp;<@s.text name='manage.overview.source.description2'/>&nbsp;<@s.text name='manage.overview.source.description3'><@s.param><@s.text name='button.add'/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description4'><@s.param><@s.text name="button.connectDB"/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description5'/><@s.text name='manage.overview.DwC.Mappings.coretype.description1'/></br></br><@s.text name='manage.overview.DwC.Mappings.coretype.description2'/></br></br><@s.text name='manage.overview.DwC.Mappings.coretype.description3'/></br></br><@s.text name='manage.overview.DwC.Mappings.coretype.description4'/>
        </#assign>
        <@popoverTextInfo mappingsInfo/>

        <@s.text name='manage.overview.DwC.Mappings'/>
    </h5>

    <div class="row">
        <div class="col-lg-9 order-lg-last">
            <div class="mx-md-4 mx-2">
                <p>
                    <@s.text name='manage.overview.DwC.Mappings.description'/>
                </p>

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
                                    <td class="d-flex justify-content-end">
                                        <div class="btn-group" role="group">
                                            <a class="btn btn-sm btn-outline-secondary peekBtn" role="button" href="mappingPeek.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                                <i class="bi bi-eye"></i>
                                            </a>
                                            <a class="btn btn-sm btn-outline-secondary" role="button" href="mapping.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                                <@s.text name='button.edit'/>
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
                                                    <div class="btn-group" role="group">
                                                        <a class="btn btn-sm btn-outline-secondary peekBtn" role="button" href="mappingPeek.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
                                                            <i class="bi bi-eye"></i>
                                                        </a>
                                                        <a class="btn btn-sm btn-outline-secondary" role="button" href="mapping.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
                                                            <@s.text name='button.edit'/>
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

        <div class="col-lg-3 border-lg-right">
            <div class="mx-md-4 mx-2">
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
                <#else>
                    <div class="d-flex justify-content-start">
                        <div class="input-group">
                            <button type="button" class="btn btn-sm btn-outline-warning" data-bs-trigger="focus" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="<@s.text name="manage.overview.DwC.Mappings.cantdo" escapeHtml=true/>">
                                <i class="bi bi-exclamation-triangle"></i>
                            </button>
                            <select class="form-select form-select-sm">
                                <option value=""></option>
                            </select>
                        </div>
                    </div>
                </#if>
            </div>
        </div>
    </div>
</div>
