[#ftl]
[#include "/WEB-INF/pages/inc/header.ftl"/]
 <title>[@s.text name="401.title"/]</title>
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<h1>[@s.text name="401.title"/]</h1>
<p>[@s.text name="401.body"][@s.param]${baseURL}/login[/@s.param][/@s.text]</p>

[#include "/WEB-INF/pages/inc/footer.ftl"/]
