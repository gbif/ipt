<#macro publish resource>
<form action='publish.do' method='post'>
    <input name="r" type="hidden" value="${resource.shortname}"/>
    <input id="pubMode" name="pubMode" type="hidden" value=""/>
  <#if action.qualifiesForAutoPublishing()>
    <@s.submit cssClass="confirmAutoPublish" name="publish" key="button.publish" disabled="false"/>
  <#else>
    <@s.submit name="publish" key="button.publish" disabled="false"/>
  </#if>
</form>
<br/>
<br/>
<#-- Auto-publishing section-->
<form action='resource-autoPublicationOff.do' method='post'>
    <input name="r" type="hidden" value="${resource.shortname}"/>
  <#if resource.usesAutoPublishing() || resource.hasDisabledAutoPublishing()>
      <div class="head">
        <@s.text name='autopublish'/>
        <#if resource.usesAutoPublishing()>
            <em class="green"><@s.text name="autopublish.status.on"/></em>
          <@s.submit name="publish" key="autopublish.off" disabled="false"/>
            <p>
              <#if resource.nextPublished??><@s.text name='manage.home.next.publication'/>
                  : ${resource.nextPublished?date?string.medium}</#if>
            </p>
        <#else>
            <em class="warn"><@s.text name="autopublish.status.disabled"/></em>
          <@s.submit name="publish" key="autopublish.undo" disabled="false"/>
        </#if>
      </div>
  </#if>
</form>
</#macro>