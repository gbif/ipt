<#assign display=JspTaglibs["http://displaytag.sf.net"]/>

<h3>Displaying users</h3>
<ul>
<#list users as user>
        <li>${taxa.name}</li>
</#list>
</ul>

<hr>

<@display.table name="${taxa}">
    <@display.column property="name"/>
</@display.table>

<hr>
