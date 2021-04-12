<#setting url_escaping_charset="UTF-8">
<#if report??>
  <div id="preview-report">
    <#if report.completed>
      <#if !report.hasException() >
          <p class="actionMessage"><@s.text name='mapping.preview.success'/></p>
      <#else>
          <p class="errorMessage"><@s.text name='mapping.preview.failed'/></p>
      </#if>
    </#if>
      <strong><@s.text name='manage.report.logMessage'/></strong>
      <ul class="simple">
        <#list report.messages as msg>
            <li>${msg.message} <span class="small">${msg.date?time?string}</span></li>
        </#list>
      </ul>
    <#if cfg.debug() && report.hasException()>
        <br/>
        <strong><@s.text name='manage.report.exception'/></strong>: ${report.exceptionMessage!}
        <ul class="simple">
          <#list report.exceptionStacktrace as msg>
              <li>${msg}</li>
          </#list>
        </ul>
    </#if>
  </div>
</#if>
<table class="simple">
 <tr>
   <#list columns as col><th>${col}</th></#list>
 </tr>
 <#list peek as row><#if row??>
   <tr<#if (row_index % 2) == 0> class="even"</#if>>
     <#list row as col><td>${col!"<em>null</em>"}</td></#list>
   </tr>
   </#if></#list>
</table>