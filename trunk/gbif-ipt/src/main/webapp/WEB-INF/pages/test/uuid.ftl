[#ftl]
[#include "/WEB-INF/pages/inc/header.ftl"/]
 <title>UUID converter test</title>
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<h1>UUID converter test</h1>

<div>
<form action="${baseURL}/uuid.do" method="get">
  <div>
    <input type="text" size="60" name="org.key2" value="${org.key2!}"/>
  </div>
  <div>
    <input type="submit" value="Save"/>
  </div>
</form>
</div>


[#include "/WEB-INF/pages/inc/footer.ftl"/]
