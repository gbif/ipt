<#escape x as x?xml>
 <diagnostics>
 <#list diagnostics as d>
  <diagnostics severity="${d.severity}" time="${d.time?datetime?string(xmlDateFormat)}">${d.text}</diagnostics>
 </#list>
 </diagnostics>
</#escape>
</response>