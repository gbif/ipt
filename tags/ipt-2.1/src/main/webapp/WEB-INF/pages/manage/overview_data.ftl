
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
        <p>
          <div>
            <img class="info" src="${baseURL}/images/info.gif"/>
            <em><@s.text name='manage.overview.source.description5'/></em>
          </div>
        </p>
      </#if>


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
    </div>
</div>

<div class="resourceOverview" id="mappings">
    <div class="titleOverview">
        <div class="head">
        <@s.text name='manage.overview.DwC.Mappings'/>
        </div>
        <div class="actions">
        <#if (potentialExtensions?size>0)>
            <form action='mapping.do' method='post'>
                <input name="r" type="hidden" value="${resource.shortname}"/>
                <select name="id" id="rowType" size="1">
                <#-- if core hasn't been selected yet add help text to help user choose core type -->
                  <#if !resource.coreType?has_content && (potentialExtensions?size > 1) >
                      <option><@s.text name='manage.overview.DwC.Mappings.select'/></option>
                  </#if>
                  <#list potentialExtensions as e>
                    <#if e?has_content>
                        <option value="${e.rowType}">${e.title}</option>
                    </#if>
                  </#list>
                </select>
              <@s.submit name="add" key="button.add"/>
            </form>
        </#if>
        </div>
    </div>
    <div class="bodyOverview">
        <p>
        <#if (potentialExtensions?size>0)>
        <@s.text name='manage.overview.DwC.Mappings.description'/>
      <#else>
          <@s.text name='manage.overview.DwC.Mappings.cantdo'/>
        </#if>
        </p>

    <#-- if core hasn't been selected yet add help text to help user understand how to choose core type -->
    <#if (potentialExtensions?size>1) && !resource.coreType?has_content >
        <p>
            <img class="info" src="${baseURL}/images/info.gif"/>
            <em><@s.text name='manage.overview.DwC.Mappings.coretype.description'/>
        </p>
    </#if>

        <div class="details">
            <table>
            <#list resource.coreMappings as m>
                <tr>
                    <th><#if m_index==0>${m.extension.title}</#if></th>
                    <td>${m.fields?size} <@s.text name='manage.overview.DwC.Mappings.terms'/> ${(m.source.name)!}</td>
                    <td>
                        <a class="button" href="mapping.do?r=${resource.shortname}&id=${m.extension.rowType}&mid=${m_index}">
                            <input class="button" type="button" value='<@s.text name='button.edit'/>'/>
                        </a>
                    </td>
                </tr>
            </#list>
            <#list resource.getMappedExtensions() as ext>
              <#if !ext.isCore()>
                <#list resource.getMappings(ext.rowType) as m>
                    <tr>
                        <th><#if m_index==0>${ext.title}</#if></th>
                        <td>${m.fields?size} <@s.text name='manage.overview.DwC.Mappings.terms'/> ${(m.source.name)!}</td>
                        <td>
                            <a class="button" href="mapping.do?r=${resource.shortname}&id=${ext.rowType}&mid=${m_index}">
                                <input class="button" type="button" value='<@s.text name='button.edit'/>'/>
                            </a>
                        </td>
                    </tr>
                </#list>
              </#if>
            </#list>
            </table>
        </div>
    </div>
</div>