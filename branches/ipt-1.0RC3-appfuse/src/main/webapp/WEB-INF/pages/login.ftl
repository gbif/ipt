<#include "/WEB-INF/pages/inc/globalVars.ftl">  

<head>
    <title><@s.text name="login.title"/></title>
    <meta name="menu" content="Login"/> 
	<#include "/scripts/login.js">
</head>

<style>
</style>

<body id="login">
<h1><@s.text name='login.heading'/></h1>
<div class="horizontal_dotted_line_xlarge_foo"></div>
<div id="mainfull" style=" margin-top: 15px; margin-bottom: 0px;"> 
<div style="float: left;">
	<form method="post" id="loginForm" action="<@s.url value='/j_security_check'/>"
	    onsubmit="saveUsername(this);return validateForm(this)">
	<fieldset>
	<ul>
	<#-- 
<c:if test="${param.error != null}">
    <li class="error">
        <img src="${ctx}/images/iconWarning.gif" alt="<@s.text name='icon.warning'/>" class="icon"/>
        <@s.text name="errors.password.mismatch"/>
    </li>
</c:if>
-->
	    <li>
	       <label for="j_username" class="required desc">
	            <@s.text name="label.username"/> <span class="req">*</span>
	        </label>
	        <input type="text" class="text medium" name="j_username" id="j_username" tabindex="1" />
	    </li>
	
	    <li>
	        <label for="j_password" class="required desc">
	            <@s.text name="label.password"/> <span class="req">*</span>
	        </label>
	        <input type="password" class="text medium" name="j_password" id="j_password" tabindex="2" />
	    </li>
	
	    <li>
	        <input type="checkbox" class="checkbox" name="_spring_security_remember_me" id="rememberMe" tabindex="3"/>
	        <label for="rememberMe" class="choice"><@s.text name="login.rememberMe"/></label>
	    </li>
	    <li>
	        <input type="submit" class="button" name="login" value="<@s.text name='button.login'/>" tabindex="4" />
	        <p style="padding-left: 3px;">
	            <@s.text name="login.passwordHint"/></p>
	        </p>
	    </li>
	</ul>
	</fieldset>
	</form>
</div>
<div style="float: right; width: 420px; border-left: #999999 1px dotted; padding-left: 30px; height: 200px; text-align: center;" >
	<a href="<@s.url value='/signup.html'/>"><img src="<@s.url value='/images/private.jpg'/>" style="margin-bottom: 4px;" /></a>
	<br />
    <@s.text name="login.signup">
        <@s.param><@s.url value="/signup.html"/></@s.param>
    </@s.text>
</div>
<div class="break" style="clear: both;"></div>
</div>

</body>