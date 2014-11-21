<#macro publish resource>
<form action='publish.do' method='post'>
  <input name="r" type="hidden" value="${resource.shortname}"/>

  <#-- Auto-publishing mode-->
  <#assign puMo="">
    <#if resource.publicationMode??>
    <#assign puMo=resource.publicationMode>
  </#if>
  <input id="currPubMode" name="currPubMode" type="hidden" value="${puMo}"/>
  <input id="pubMode" name="pubMode" type="hidden" value=""/>

  <#-- Auto-publishing frequency-->
  <#assign upFr="">
  <#if resource.updateFrequency??>
    <#assign upFr=resource.updateFrequency.identifier>
  </#if>
  <input id="currPubFreq" name="currPubFreq" type="hidden" value="${upFr}"/>
  <input id="pubFreq" name="pubFreq" type="hidden" value=""/>
  <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

    <!-- resources cannot be published if the mandatory metadata is missing -->
  <#if missingMetadata>
    <@s.submit id="publishButton" name="publish" key="button.publish" disabled="true"/>
    <img class="infoImg" src="${baseURL}/images/warning.gif" />
    <div class="info autop">
      <@s.text name="manage.overview.published.missing.metadata"/>
    </div>
  <!-- resources without a DOI, with a DOI reserved, or that haven't been registered yet can be republished whenever by any manager -->
  <#elseif (resource.identifierStatus == "UNRESERVED" || resource.identifierStatus == "RESERVED") && resource.status != "REGISTERED">
    <@s.submit id="publishButton" name="publish" key="button.publish"/>
  <!-- resources with an existing DOI or registered with GBIF can only be republished by managers with registration rights -->
  <#elseif (resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" && alreadyAssignedDoi)
          || (resource.identifierStatus == "PUBLIC" && alreadyAssignedDoi)
          || resource.status == "REGISTERED">
    <!-- the user must have registration rights -->
    <#if !currentUser.hasRegistrationRights()>
      <@s.submit id="publishButton" name="publish" key="button.publish" disabled="true"/>
      <img class="infoImg" src="${baseURL}/images/warning.gif" />
      <div class="info autop">
        <@s.text name="manage.resource.status.publication.forbidden"/>&nbsp;<@s.text name="manage.resource.role.change"/>
      </div>
    <!-- an organisation with DOI account activated must exist -->
    <#elseif ((resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" && alreadyAssignedDoi)
          || (resource.identifierStatus == "PUBLIC" && alreadyAssignedDoi))
          && !organisationWithPrimaryDoiAccount??>
      <@s.submit id="publishButton" name="publish" key="button.publish" disabled="true"/>
      <img class="infoImg" src="${baseURL}/images/warning.gif" />
      <div class="info autop">
        <@s.text name="manage.resource.status.publication.forbidden.account.missing"/>
      </div>
    <!-- publishing a new major version -->
    <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" && alreadyAssignedDoi>
      <@s.submit cssClass="confirmPublishMajorVersion" id="publishButton" name="publish" key="button.publish"/>
    <!-- publishing a new minor version -->
    <#elseif resource.identifierStatus == "PUBLIC" && alreadyAssignedDoi>
      <@s.submit cssClass="confirmPublishMinorVersion" id="publishButton" name="publish" key="button.publish"/>
    <!-- publishing a new version registered with GBIF -->
    <#elseif resource.status == "REGISTERED">
      <@s.submit id="publishButton" name="publish" key="button.publish"/>
    </#if>
  <!-- otherwise prevent publication from happening just to be safe -->
  <#else>
    <@s.submit id="publishButton" name="publish" key="button.publish" disabled="true"/>
    ${alreadyAssignedDoi?string}
  </#if>

  <br/>
  <br/>
  <div id="actions" class="autop">
      <label for="autopublish"><@s.text name="autopublish"/></label>
      <select id="autopublish" name="autopublish" size="1">
        <#list frequencies?keys as val>
            <option value="${val}" <#if (upFr!"")==val> selected="selected"</#if>>
              <@s.text name="${frequencies.get(val)}"/>
            </option>
        </#list>
      </select>
      <img class="infoImg" src="${baseURL}/images/info.gif" />
      <div class="info">
        <@s.text name="autopublish.help"/>
      </div>
  </div>
</form>
</#macro>