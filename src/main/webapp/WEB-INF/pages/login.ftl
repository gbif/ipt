<#include "/WEB-INF/pages/inc/header.ftl"/>
 <title><@s.text name="login.title"/></title>
 <#assign currentMenu = "account"/>
<#include "/WEB-INF/pages/inc/menu.ftl"/>

<h1><@s.text name="login.title"/></h1>
<p>
<@s.text name="login.intro"/>: 
<br/>
<a href="mailto:${admin.email}?subject=${lostPswdEmailSubject?url}&Body=${lostPswdEmailBody?url}">${admin.getNameWithEmail()?xml}</a>
</p>

<#include "/WEB-INF/pages/macros/forms.ftl">
<@s.form cssClass="topForm half" action="login.do" method="post">
	<@input name="email" />  
	<@input name="password" type="password" value=""/>
	
<#if email?exists>
<p><@s.text name="login.forgottenpassword"/></p>
</#if>

  <div class="buttons">
 	<@s.submit cssClass="button" name="login-submit" key="portal.login"/>
  </div>	
</@s.form>


<#--
<div id="login">
<form id="loginFull" action="" method="post">
  <div>
    <input class="form-reset" type="text" size="30" name="email" id="user" value="${email!"admin"}"/>
  </div>
  <div>
    <input class="form-reset"  type="password" size="30" name="password" id="pass" value="${password!"admin"}"/>
  </div>
  <div>
    <@s.submit key="portal.login" name=""/>
  </div>
</form>
</div>
-->

<#include "/WEB-INF/pages/inc/footer.ftl"/>
