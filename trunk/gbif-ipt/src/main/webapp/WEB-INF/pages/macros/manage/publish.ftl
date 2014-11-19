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
   <#if resource.identifierStatus == "UNRESERVED"
   || (resource.identifierStatus == "RESERVED")
   || (resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" && resource.status != "PUBLIC" && resource.status != "REGISTERED" && currentUser.hasRegistrationRights() && organisationWithPrimaryDoiAccount??) >
     <@s.submit id="publishButton" name="publish" key="button.publish" disabled="${missingMetadata?string}"/>
   <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" && alreadyAssignedDoi && currentUser.hasRegistrationRights() && organisationWithPrimaryDoiAccount??>
     <@s.submit cssClass="confirmPublishMajorVersion" id="publishButton" name="publish" key="button.publish" disabled="${missingMetadata?string}"/>
   <#elseif resource.identifierStatus == "PUBLIC" && currentUser.hasRegistrationRights() && organisationWithPrimaryDoiAccount??>
     <@s.submit cssClass="confirmPublishMinorVersion" id="publishButton" name="publish" key="button.publish" disabled="${missingMetadata?string}"/>
   <#else>
     <@s.submit id="publishButton" name="publish" key="button.publish" disabled="true"/>
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