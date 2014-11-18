<!-- Represents metadata section on resource overview page -->
<div class="resourceOverview" id="metadata">
  <div class="titleOverview">
    <div class="head">
      <@s.text name='manage.overview.metadata'/>
    </div>
    <div class="actions">
      <form action='metadata-basic.do' method='get'>
        <input name="r" type="hidden" value="${resource.shortname}"/>
        <@s.submit name="edit" key="button.edit" disabled="${(organisations?size==0)?string}"/>
      </form>
    </div>
  </div>
  <div class="bodyOverview">
    <#if organisations?size==0>
      <div>
          <img class="info" src="${baseURL}/images/warning.gif"/>
          <em><@s.text name="manage.metadata.no.organisations"/></em>
      </div>
    <#elseif missingMetadata>
      <div>
        <img class="info" src="${baseURL}/images/warning.gif"/>
        <em><@s.text name="manage.overview.missing.metadata"/></em>
      </div>
    <#else>
      <p>
        <@s.text name="manage.overview.metadata.description"/>
        <#if resource.coreType?has_content && resource.coreType==metadataType>
          <@s.text name='manage.overview.source.hidden'><@s.param><a href="${baseURL}/manage/metadata-basic.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.basic"/></a></@s.param></@s.text>
        </#if>
      </p>

      <div class="details">
        <table>
          <tr>
            <#if metadataModifiedSinceLastPublication>
              <th><@s.text name='basic.lastModified'/>:</th>
              <td>${resource.getMetadataModified()?date?string.medium!}</td>
            <#elseif resource.lastPublished??>
              <th><@s.text name="manage.overview.notModified"/></th>
            </#if>
          </tr>
        </table>
      </div>
    </#if>
  </div>
</div>