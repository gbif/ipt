<#setting url_escaping_charset="UTF-8">
<h1<#if allCompleted> id="allReportsCompleted"</#if>>Background Process Status</h1>
<span class="small">${now?datetime?string}</span>

<#if reports?size==0>
 <h2>No Active Resource Processes</h2>
</#if>

<#list reports?keys as res>
<div<#if reports[res]?? && reports[res].completed> class="completed"</#if>>
 <h2>Resource ${res}</h2>
 <#if reports[res]??>
  <#assign status=reports[res]/>
  <p class="green">${status.state}</p>
  <ul class="simple">
   <li><strong>Log Messages</strong></li>
   <#list status.messages as msg>
   <li>${msg.message} <span class="small">${msg.date?time?string}</span></li>
   </#list>
  </ul>
 <#else>
  <h4>Finished<h4>
 </#if>
</div>
 
</#list>
