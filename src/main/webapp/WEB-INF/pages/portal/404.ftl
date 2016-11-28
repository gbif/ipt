[#ftl output_format="HTML"]
[#include "/WEB-INF/pages/inc/header.ftl"/]
<title>[@s.text name="manage.404.title"/]</title>
[#assign currentMenu = "manage"/]
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<h1>[@s.text name="manage.404.title"/]</h1>
<p>We are sorry, but the requested resource [#if version?has_content && (version > 0)]with version #${version} [/#if]does not exist</p>

</div>

[#include "/WEB-INF/pages/inc/footer.ftl"/]