[#ftl]
[#include "/WEB-INF/pages/inc/header.ftl"/]
 <title>IPT Account</title>
[#include "/WEB-INF/pages/inc/menu.ftl"/]

<h2>Edit Your IPT Account</h2>


<div>

<p>You can modify your account settings here. Be aware that if you modify your email address you will have to verify that address again. 
To change your password please provide the new one twice or leave it blank to keep it as it is.</p>

<form action="${baseURL}/account/save" method="post">
  <p>
    <label for="email">Email</label> <input type="text" size="30" name="user.email" id="email"/>
  </p>
  <p>
    <label for="realname">Realname</label> <input type="text" size="30" name="user.name" id="name"/>
  </p>
  <p>
    <label for="password">Password</label> <input type="password" size="30" name="user.password" id="password"/>
  </p>
  <p>
    <label for="password2">Retype Password</label> <input type="password" size="30" name="password2" id="password2"/>
  </p>

  <p>
  	<label>&nbsp;</label>
    <input type="submit" class="submitBtn" value="&nbsp;Join&nbsp;"/>
  </p>
</form>

</div>

[#include "/WEB-INF/pages/inc/footer.ftl"/]
