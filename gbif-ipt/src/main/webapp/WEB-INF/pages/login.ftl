[#ftl]
[#include "/WEB-INF/pages/inc/header.ftl"/]
 <title>[@s.text name="login.title"/]</title>
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<h1>[@s.text name="login.title"/]</h1>

[#if email?exists]
<p>[@s.text name="login.forgottenPassword"/]</p>
[/#if]

<div id="login">
<form id="loginFull" action="${baseURL}/login" method="post">
  <div>
    <input class="form-reset" type="text" size="30" name="email" id="user" value="${email!"admin"}"/>
  </div>
  <div>
    <input class="form-reset"  type="password" size="30" name="password" id="pass" value="${password!"admin"}"/>
  </div>
  <div>
    <input type="submit" value="Login" id="login-submit"/>
  </div>
</form>
</div>


[#include "/WEB-INF/pages/inc/footer.ftl"/]
