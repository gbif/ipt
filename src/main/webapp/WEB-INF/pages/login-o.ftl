<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl"/>
<title><@s.text name="login.title"/></title>
<#assign currentMenu = "account"/>
<#include "/WEB-INF/pages/inc/menu.ftl"/>
<#include "/WEB-INF/pages/macros/forms.ftl">


  <h1><@s.text name="login.title"/></h1>
  <div class="grid_18 suffix_6">
  <p>
    <@s.text name="login.intro"><@s.param>${admin.email}</@s.param></@s.text>
  </p>
    <form id="newuser" class="topForm half" action="${baseURL}/login.do" method="post">
      <@input name="email" i18nkey="user.email" value="${email!}"/>
      <@input name="password" i18nkey="user.password" type="password" value="${password!}"/>
      <input name="csrfToken" type="hidden" value="${newCsrfToken!}">
      <#if email?has_content>
        <p>
          <@s.text name="login.forgottenpassword"><@s.param>${admin.email}</@s.param></@s.text>
        </p>
      </#if>
      <div class="userManageButtons">
        <@s.submit cssClass="button" key="portal.login"/>
      </div>
    </form>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl"/>