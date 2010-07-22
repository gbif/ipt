[#ftl]
[#include "/WEB-INF/pages/inc/header.ftl"/]
 <title>Missing</title>
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<h1>Oops</h1>
<p>It seems you have discovered an unforseen error:<p/>

<pre>
${exception.message!"unknown error, please see your logs"}
</pre>

[#if adminRights]
<p><a href="${baseURL}/admin/logs.do">View IPT logs</a></p>
[/#if]
<p>
If you think this shouldnt have happened, would you mind helping us to improve the IPT and 
<a href="http://code.google.com/p/gbif-providertoolkit/issues/entry?summary=IPT%20error">file a small bug report</a> for this exception?
</p>

<p>
Thanks a million, the IPT development team.
</p>




[#include "/WEB-INF/pages/inc/footer.ftl"/]
