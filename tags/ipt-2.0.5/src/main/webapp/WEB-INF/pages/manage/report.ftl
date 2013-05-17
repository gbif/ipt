<#setting url_escaping_charset="UTF-8">

<h2><@s.text name='manage.report.title'/></h2>
    
<span class="small">${now?datetime?string}</span>

<#if report??>

  <div<#if report.completed>class="completed"</#if>>

  <#if report.completed>
    <#if !report.hasException() >
        <p class="actionMessage">${report.state}</p>
    <#else>
        <p class="errorMessage">${report.state}</p>
    </#if>
    <p><@s.text name='manage.report.continueTo'><@s.param>${resource.shortname}</@s.param></@s.text></p>
    <p><@s.text name='portal.publication.download.log'/> <a href="${baseURL}/publicationlog.do?r=${resource.shortname}"><@s.text name='portal.publication.log'/></a></p>
  <#else>
      <p><@s.text name="manage.locked"><@s.param>${baseURL}/manage/cancel.do?id=${resource.shortname}</@s.param></@s.text></p>
      <p class="warnMessage">${report.state}</p>
      <p><a href="cancel.do?r=${resource.shortname}"><@s.text name="button.cancel"/></a> <@s.text name="manage.overview.publishing"/>.</p>
  </#if>

  <strong><@s.text name='manage.report.logMessage'/></strong>
  <ul class="simple">
    <#list report.messages as msg>
      <li>${msg.message} <span class="small">${msg.date?time?string}</span></li>
    </#list>
  </ul>

  <#if report.hasException()>
    <br/>
    <strong><@s.text name='manage.report.exception'/></strong>: ${report.exceptionMessage!}
    <ul class="simple">
      <#list report.exceptionStacktrace as msg>
        <li>${msg}</li>
      </#list>
    </ul>
  </#if>

</div>

<#else>
  <h4><@s.text name='manage.report.finished'/><h4>
  <p><@s.text name='manage.report.continueTo'><@s.param>${resource.shortname}</@s.param></@s.text></p>
</#if>


