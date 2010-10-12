<#macro pager records link page=1 pagesize=25>
    <div class="pager">
  [#if pagerRecords?size==ps || page>1]
     page ${page} 
    [#if page > 1]<a class="btn" href="${baseURL}/${link}page=1">first</a>[/#if]
    [#if page > 2]<a class="btn" href="${baseURL}/${link}page=${page-1}">previous</a>[/#if]
    [#if records?size==pagesize]<a class="btn" href="${baseURL}/${link}p=${page+1}">next</a>[/#if]
  [/#if] 
    </div>
</#macro> 
