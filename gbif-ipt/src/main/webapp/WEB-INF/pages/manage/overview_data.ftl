<!-- Represents source data and mapping data sections on resource overview page -->
<div class="resourceOverview" id="sources">
  <div class="titleOverview">
    <div class="head">
      <@s.text name='manage.overview.source.data'/>
    </div>
    <div class="actions">
      <form action='addsource.do' method='post' enctype="multipart/form-data">
        <input name="r" type="hidden" value="${resource.shortname}"/>
        <input name="validate" type="hidden" value="false"/>
        <@s.file name="file" key="manage.resource.create.file"/>
        <@s.submit name="add" key="button.connectDB"/>
        <@s.submit name="clear" key="button.clear"/>
        <div style="display: none;">
          <@s.submit name="cancel" key="button.cancel" method="cancelOverwrite"/>
        </div>
      </form>
    </div>
  </div>
  <div class="bodyOverview">
    <p>
      <@s.text name='manage.overview.source.description1'/>&nbsp;<@s.text name='manage.overview.source.description2'/>&nbsp;<@s.text name='manage.overview.source.description3'><@s.param><@s.text name="button.add"/></@s.param></@s.text>
    </p>
    <p>
      <@s.text name='manage.overview.source.description4'><@s.param><@s.text name="button.connectDB"/></@s.param></@s.text>
    </p>
    <!-- Warn users they can't update a resource by uploading a DwC-A until they have uploaded their first source file -->
    <#if (resource.sources?size == 0) >
      <div>
        <img class="info" src="${baseURL}/images/info.gif"/>
        <em><@s.text name='manage.overview.source.description5'/></em>
      </div>
    </#if>

    <#if (resource.sources?size>0)>
      <div class="details">
        <table>
          <#list resource.sources as src>
            <tr>
              <#if src.isFileSource()>
                <th>${src.name} <@s.text name='manage.overview.source.file'/></th>
                <td>${src.fileSizeFormatted},&nbsp;${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>,&nbsp;${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/>.&nbsp;${(src.lastModified?datetime?string)!}<#if !src.readable>&nbsp;<img src="${baseURL}/images/warning.gif"/></#if></td>
              <#elseif src.isExcelSource()>
                <th>${src.name} <@s.text name='manage.overview.source.excel'/></th>
                <td>${src.fileSizeFormatted},&nbsp;${src.rows}&nbsp;<@s.text name='manage.overview.source.rows'/>,&nbsp;${src.getColumns()}&nbsp;<@s.text name='manage.overview.source.columns'/>.&nbsp;${(src.lastModified?datetime?string)!}<#if !src.readable>&nbsp;<img src="${baseURL}/images/warning.gif"/></#if></td>
              <#else>
                <th>${src.name} <@s.text name='manage.overview.source.sql'/></th>
                <td>db=${src.database!"..."},&nbsp;${src.columns}&nbsp;<@s.text name='manage.overview.source.columns'/>.<#if !src.readable>&nbsp;<img src="${baseURL}/images/warning.gif"/></#if></td>
              </#if>
              <td>
                <a class="button" href="source.do?r=${resource.shortname}&id=${src.name}">
                  <input class="button" type="button" value='<@s.text name='button.edit'/>'/>
                </a>
              </td>
            </tr>
          </#list>
        </table>
      </div>
    </#if>
  </div>
</div>

<div class="resourceOverview" id="mappings">
  <div class="titleOverview">
    <div class="head">
      <@s.text name='manage.overview.DwC.Mappings'/>
    </div> 
    <div class="actions">
    <#if (potentialCores?size>0)>
        <form action='mapping.do' method='post'>
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <select name="id" id="rowType" size="1">
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
          <@s.submit name="add" key="button.add"/>
        </form>
    </#if>
    </div>
  </div>
  <div class="bodyOverview">
    <#if (potentialCores?size>0)>
      <p>
        <@s.text name='manage.overview.DwC.Mappings.description'/>
      </p>

      <div class="details twenty_bottom">
        <table>
          <tr>
            <#if mappingsModifiedSinceLastPublication>
              <th><@s.text name='basic.lastModified'/>:</th>
              <td>${resource.getMappingsModified()?date?string.medium!}</td>
              <#elseif resource.lastPublished??>
                <th><@s.text name="manage.overview.notModified"/></th>
              </#if>
            </tr>
          </table>
        </div>
    <#else>
      <div>
        <img class="info" src="${baseURL}/images/info.gif"/>
        <em><@s.text name="manage.overview.DwC.Mappings.cantdo"/></em>
      </div>
    </#if>

    <#-- if core hasn't been selected yet add help text to help user understand how to choose core type -->
    <#if (potentialCores?size>1) && !resource.coreType?has_content >
      <div>
        <img class="info" src="${baseURL}/images/info.gif"/>
        <em><@s.text name='manage.overview.DwC.Mappings.coretype.description'/></em>
      </div>
    </#if>

    <#if resource.coreRowType?has_content>
        <div class="details">
            <div class="mapping_head"><@s.text name='manage.overview.DwC.Mappings.cores.select'/></div>
            <table>
              <#list resource.getMappings(resource.coreRowType) as m>
                  <tr <#if m_index==0>class="mapping_row"</#if>>
                      <th><#if m_index==0>${m.extension.title}</#if></th>
                      <td>${m.fields?size} <@s.text name='manage.overview.DwC.Mappings.terms'/> ${(m.source.name)!}</td>
                      <td>
                          <a class="button" href="mapping.do?r=${resource.shortname}&id=${m.extension.rowType}&mid=${m_index}">
                              <input class="button" type="button" value='<@s.text name='button.edit'/>'/>
                          </a>
                          <a href="mappingPeek.do?r=${resource.shortname}&id=${m.extension.rowType}&mid=${m_index}" class="icon icon-preview peekBtn"/>
                      </td>
                  </tr>
              </#list>
            </table>
          <#if (resource.getMappedExtensions()?size > 1)>
              <div class="mapping_head twenty_top"><@s.text name='manage.overview.DwC.Mappings.extensions.select'/></div>
              <table>
                <#list resource.getMappedExtensions() as ext>
                  <#if ext.rowType != resource.coreRowType>
                    <#list resource.getMappings(ext.rowType) as m>
                        <tr <#if m_index==0>class="mapping_row"</#if>>
                            <th><#if m_index==0>${ext.title}</#if></th>
                            <td>${m.fields?size} <@s.text name='manage.overview.DwC.Mappings.terms'/> ${(m.source.name)!}</td>
                            <td>
                                <a class="button" href="mapping.do?r=${resource.shortname}&id=${ext.rowType}&mid=${m_index}">
                                    <input class="button" type="button" value='<@s.text name='button.edit'/>'/>
                                </a>
                                <a href="mappingPeek.do?r=${resource.shortname}&id=${ext.rowType}&mid=${m_index}" class="icon icon-preview peekBtn"/>
                            </td>
                        </tr>
                    </#list>
                  </#if>
                </#list>
              </table>
          </#if>
        </div>
    </#if>
  </div>
</div>
