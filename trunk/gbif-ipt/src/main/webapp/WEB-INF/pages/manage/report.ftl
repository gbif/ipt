<#setting url_escaping_charset="UTF-8">

<h2>Publishing Status</h2>
<span class="small">${now?datetime?string}</span>
<div<#if report?? && report.completed> class="completed"</#if>>
 <#if report??>
  <p class="green">${report.state}</p>
  <#if report.completed>
    <p>Continue to <a href="resource.do?r=${resource.shortname}">resource overview</a>.</p>
  </#if> 
  <ul class="simple">
   <li><strong>Log Messages</strong></li>
   <#list report.messages as msg>
   <li>${msg.message} <span class="small">${msg.date?time?string}</span></li>
   </#list>
  </ul>
 <#else>
  <h4>Finished<h4>
  <p>Continue to <a href="resource.do?r=${resource.shortname}">resource overview</a>.</p>
 </#if>
</div>
