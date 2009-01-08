 <diagnostics>
 <#list diagnostics as d>
  <diagnostics severity="${d.severity}" time="${d.time?datetime?string(xmlDateFormat)}">${d.text}</diagnostics>
 </#list>
 </diagnostics>
</response>