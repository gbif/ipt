 <diagnostics>
 <#list diagnostics as d>
  <diagnostics severity="${d.severity}" time="${d.time}">${d.text}</diagnostics>
 </#list>
 </diagnostics>
</response>