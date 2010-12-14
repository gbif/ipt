[#ftl]
[#include "/WEB-INF/pages/inc/header.ftl"/]
<title>[@s.text name="error.header.title"/]</title>
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<h1>[@s.text name="error.title"/]</h1>
<p>[@s.text name="error.body"/]<p/>

<pre>
      [@s.property value="%{exception.message}"/]
</pre>
    
[#if adminRights]
<p><a href="${baseURL}/admin/logs.do">[@s.text name="error.view.logs"/]</a></p>
[/#if]
<p>
[@s.text name="error.report"/]
</p>

<p>
[@s.text name="error.thanks"/]
</p>

<hr/>
<h3>[@s.text name="error.details.title"/]</h3>
<p>
  [@s.property value="%{exceptionStack}"/]
</p>



[#include "/WEB-INF/pages/inc/footer.ftl"/]
