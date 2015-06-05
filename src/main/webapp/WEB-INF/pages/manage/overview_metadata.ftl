<!-- Represents metadata section on resource overview page -->
<div class="resourceOverview" id="metadata">
  <div class="titleOverview">
    <div class="head">
      <img class="infoImg" src="${baseURL}/images/info.gif" />
      <div class="info autop">
        <@s.text name='manage.metadata.basic.required.message'/>
        <#if resource.coreType?has_content && resource.coreType==metadataType>
          </br></br>
          <@s.text name='manage.overview.source.hidden'><@s.param><a href="${baseURL}/manage/metadata-basic.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.basic"/></a></@s.param></@s.text>
        </#if>
      </div>
      <@s.text name='manage.overview.metadata'/>
    </div>
    <div class="actions">
      <form action='metadata-basic.do' method='get'>
        <input name="r" type="hidden" value="${resource.shortname}"/>
        <#if organisations?size==0>
            <@s.submit name="edit" key="button.edit" disabled="true"/>
            <img class="infoImg" src="${baseURL}/images/warning.gif"/>
            <div class="info autop">
              <@s.text name="manage.metadata.no.organisations"/>
            </div>
        <#elseif missingMetadata>
            <@s.submit name="edit" key="button.edit"/>
            <img class="infoImg" src="${baseURL}/images/warning.gif"/>
            <div class="info autop">
              <@s.text name="manage.overview.missing.metadata"/>
            </div>
        <#else>
          <@s.submit name="edit" key="button.edit"/>
        </#if>
      </form>
    </div>
  </div>
  <div class="bodyOverview">

      <p>
        <@s.text name="manage.overview.metadata.description"/>
      </p>

      <div class="details">
        <table>
          <tr>
            <#if metadataModifiedSinceLastPublication>
              <@s.text name='manage.home.last.modified'/> ${resource.getMetadataModified()?date?string.medium!}
            <#elseif resource.lastPublished??>
              <@s.text name="manage.overview.notModified"/>
            </#if>
          </tr>
        </table>
      </div>

  </div>
</div>