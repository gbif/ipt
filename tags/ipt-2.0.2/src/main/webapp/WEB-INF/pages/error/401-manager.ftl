[#ftl]
[#include "/WEB-INF/pages/inc/header.ftl"/]
 <title>Login Required</title>
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<h1>[@s.text name="401.manager.title"/]</h1>
<p>[@s.text name="401.manager.body"][@s.param]${baseURL}/login[/@s.param][/@s.text]</p>
[#include "/WEB-INF/pages/inc/footer.ftl"/]
