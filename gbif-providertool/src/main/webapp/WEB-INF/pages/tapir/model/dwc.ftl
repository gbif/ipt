<#assign page=JspTaglibs["http://www.opensymphony.com/sitemesh/page"]>
<@page.applyDecorator name="tapir" title="search">
<@page.param name="tapir.content">
<records>
 <#list records rec>
 	
 </#list>
</records>
</@page.param>
</@page.applyDecorator>