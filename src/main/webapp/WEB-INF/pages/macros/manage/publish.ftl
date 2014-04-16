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

  <input type="submit" id="publishButton" value="<@s.text name='button.publish'/>"/>
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
    <#if resource.usesAutoPublishing()>
      <p>
        <#if resource.nextPublished??><@s.text name='manage.home.next.publication'/>: <em class="green">${resource.nextPublished?date?string("MMM d, yyyy, HH:mm:ss")}</#if></em>
      </p>
    </#if>
  </div>
</form>
</#macro>