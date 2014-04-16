<#setting url_escaping_charset="UTF-8">
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