<!-- Represents source data and mapping data sections on resource overview page -->
<div class="my-3 p-3 bg-body rounded shadow-sm" id="sources">
    <div class="titleOverview">
        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-success">
            <#assign sourcesInfo>
                <@s.text name='manage.overview.source.description1'/>&nbsp;<@s.text name='manage.overview.source.description2'/>&nbsp;<@s.text name='manage.overview.source.description3'><@s.param><@s.text name='button.add'/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description4'><@s.param><@s.text name="button.connectDB"/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description5'/>
            </#assign>
            <@popoverTextInfo sourcesInfo/>

            <@s.text name='manage.overview.source.data'/>
        </h5>

        <div class="row">
            <div class="col-xl-9 order-xl-last">
                <div class="mx-md-4 mx-2">
                    <p class="text-muted">
                        <@s.text name='manage.overview.source.intro'/>
                    </p>
                    <div class="details twenty_bottom">
                        <#if sourcesModifiedSinceLastPublication>
                            <@s.text name='manage.home.last.modified'/> ${resource.getSourcesModified()?date?string.medium!}
                        <#elseif resource.lastPublished??>
                            <@s.text name="manage.overview.notModified"/>
                        </#if>
                    </div>

                    <#if (resource.sources?size>0)>
                        <div class="details">
                            <div class="table-responsive">
                                <table class="table table-sm table-borderless" style="font-size: 0.875em;">
                                <#list resource.sources as src>
                                    <tr>
                                        <#if src.isFileSource()>
                                            <th class="col-4">${src.name} <@s.text name='manage.overview.source.file'/></th>
                                            <td>${src.fileSizeFormatted},&nbsp;${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>,&nbsp;${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/>.&nbsp;${(src.lastModified?date?string.medium)!}<#if !src.readable>&nbsp;<i class="bi bi-exclamation-triangle-fill text-warning"></#if></td>
                                        <#elseif src.isExcelSource()>
                                            <th class="col-4">${src.name} <@s.text name='manage.overview.source.excel'/></th>
                                            <td>${src.fileSizeFormatted},&nbsp;${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>,&nbsp;${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/>.&nbsp;${(src.lastModified?date?string.medium)!}<#if !src.readable>&nbsp;<i class="bi bi-exclamation-triangle-fill text-warning"></#if></td>
                                        <#else>
                                            <th class="col-4">${src.name} <@s.text name='manage.overview.source.sql'/></th>
                                            <td>db=${src.database!"..."},&nbsp;${src.columns}&nbsp;<@s.text name='manage.overview.source.columns'/>.<#if !src.readable>&nbsp;<i class="bi bi-exclamation-triangle-fill text-warning"></#if></td>
                                        </#if>
                                        <td class="d-flex justify-content-end">
                                            <a class="btn btn-sm btn-outline-success ignore-link-color" role="button" href="source.do?r=${resource.shortname}&id=${src.name}">
                                                <@s.text name='button.edit'/>
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

            <div class="col-xl-3 border-xl-right">
                <div class="mx-md-4 mx-2">
                    <form action='addsource.do' method='post' enctype="multipart/form-data">
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <input name="validate" type="hidden" value="false"/>

                        <div class="row">
                            <div class="col-12">
                                <@s.file name="file" cssClass="form-control form-control-sm my-1" key="manage.resource.create.file"/>
                            </div>
                            <div class="col-12">
                                <@s.submit name="add" cssClass="btn btn-sm btn-outline-success my-1" key="button.connectDB"/>
                                <@s.submit name="clear" cssClass="btn btn-sm btn-outline-secondary my-1" key="button.clear"/>
                                <@s.submit name="cancel" cssClass="btn btn-sm btn-outline-secondary my-1" cssStyle="display: none" key="button.cancel" method="cancelOverwrite"/>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>

    </div>

</div>

<div class="my-3 p-3 bg-body rounded shadow-sm" id="mappings">
    <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-success">
        <#assign mappingsInfo>
            <@s.text name='manage.overview.source.description1'/>&nbsp;<@s.text name='manage.overview.source.description2'/>&nbsp;<@s.text name='manage.overview.source.description3'><@s.param><@s.text name='button.add'/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description4'><@s.param><@s.text name="button.connectDB"/></@s.param></@s.text></br></br><@s.text name='manage.overview.source.description5'/><@s.text name='manage.overview.DwC.Mappings.coretype.description1'/></br></br><@s.text name='manage.overview.DwC.Mappings.coretype.description2'/></br></br><@s.text name='manage.overview.DwC.Mappings.coretype.description3'/></br></br><@s.text name='manage.overview.DwC.Mappings.coretype.description4'/>
        </#assign>
        <@popoverTextInfo mappingsInfo/>

        <@s.text name='manage.overview.DwC.Mappings'/>
    </h5>

    <div class="row">
        <div class="col-lg-9 order-lg-last">
            <div class="mx-md-4 mx-2">
                <p class="text-muted">
                    <@s.text name='manage.overview.DwC.Mappings.description'/>
                </p>

                <div class="details mb-3">
                    <#if mappingsModifiedSinceLastPublication>
                        <@s.text name='manage.home.last.modified'/> ${resource.getMappingsModified()?date?string.medium!}
                    <#elseif resource.lastPublished??>
                        <@s.text name="manage.overview.notModified"/>
                    </#if>
                </div>

                <#if resource.coreRowType?has_content>
                    <div class="details">
                        <div class="mapping_head"><@s.text name='manage.overview.DwC.Mappings.cores.select'/></div>
                        <div class="table-responsive">
                            <table class="table table-sm table-borderless" style="font-size: 0.875em;">
                            <#list resource.getMappings(resource.coreRowType) as m>
                                <tr <#if m_index==0>class="mapping_row"</#if>>
                                    <th class="col-4"><#if m_index==0>${m.extension.title}</#if></th>
                                    <td>${m.fields?size} <@s.text name='manage.overview.DwC.Mappings.terms'/> ${(m.source.name)!}.&nbsp;${(m.lastModified?date?string.medium)!}</td>
                                    <td class="d-flex justify-content-end">
                                        <div class="btn-group" role="group">
                                            <a class="btn btn-sm btn-outline-secondary ignore-link-color peekBtn" role="button" href="mappingPeek.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                                <i class="bi bi-eye"></i>
                                            </a>
                                            <a class="btn btn-sm btn-outline-success ignore-link-color" role="button" href="mapping.do?r=${resource.shortname}&id=${m.extension.rowType?url}&mid=${m_index}">
                                                <@s.text name='button.edit'/>
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </#list>
                        </table>
                        </div>
                        <#if (resource.getMappedExtensions()?size > 1)>
                            <div class="mapping_head twenty_top"><@s.text name='manage.overview.DwC.Mappings.extensions.select'/></div>
                            <div class="table-responsive">
                                <table class="table table-sm table-borderless" style="font-size: 0.875em;">
                                <#list resource.getMappedExtensions() as ext>
                                    <#if ext.rowType != resource.coreRowType>
                                        <#list resource.getMappings(ext.rowType) as m>
                                            <tr <#if m_index==0>class="mapping_row"</#if>>
                                                <th class="col-4"><#if m_index==0>${ext.title}</#if></th>
                                                <td>${m.fields?size} <@s.text name='manage.overview.DwC.Mappings.terms'/> ${(m.source.name)!}.&nbsp;${(m.lastModified?date?string.medium)!}</td>
                                                <td class="d-flex justify-content-end">
                                                    <div class="btn-group" role="group">
                                                        <a class="btn btn-sm btn-outline-secondary ignore-link-color peekBtn" role="button" href="mappingPeek.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
                                                            <i class="bi bi-eye"></i>
                                                        </a>
                                                        <a class="btn btn-sm btn-outline-success ignore-link-color" role="button" href="mapping.do?r=${resource.shortname}&id=${ext.rowType?url}&mid=${m_index}">
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
                        <@s.submit name="add" cssClass="btn btn-sm btn-outline-success my-1" key="button.add"/>
                    </form>
                <#else>
                    <div class="d-flex justify-content-start">
                        <div class="input-group">
                            <button type="button" class="btn btn-sm btn-outline-warning" data-bs-container="body" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="<@s.text name="manage.overview.DwC.Mappings.cantdo" escapeHtml=true/>">
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
