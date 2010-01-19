<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="login.title"/></title>
    <meta name="menu" content="Login"/> 
	<%@ include file="/scripts/login.js"%>
</head>

<style>
</style>

<body id="login">
<h1><fmt:message key='login.heading'/></h1>
<div class="horizontal_dotted_line_xlarge_foo"></div>
<div id="mainfull" style=" margin-top: 15px; margin-bottom: 0px;"> 
<div style="float: left;">
	<form method="post" id="loginForm" action="<c:url value='/j_security_check'/>"
	    onsubmit="saveUsername(this);return validateForm(this)">
	<fieldset>
	<ul>
	<c:if test="${param.error != null}">
	    <li class="error">
	        <img src="${ctx}/images/iconWarning.gif" alt="<fmt:message key='icon.warning'/>" class="icon"/>
	        <fmt:message key="errors.password.mismatch"/>
	        <%--${sessionScope.SPRING_SECURITY_LAST_EXCEPTION_KEY.message}--%>
	    </li>
	</c:if>
	    <li>
	       <label for="j_username" class="required desc">
	            <fmt:message key="label.username"/> <span class="req">*</span>
	        </label>
	        <input type="text" class="text medium" name="j_username" id="j_username" tabindex="1" />
	    </li>
	
	    <li>
	        <label for="j_password" class="required desc">
	            <fmt:message key="label.password"/> <span class="req">*</span>
	        </label>
	        <input type="password" class="text medium" name="j_password" id="j_password" tabindex="2" />
	    </li>
	
	    <li>
	        <input type="checkbox" class="checkbox" name="_spring_security_remember_me" id="rememberMe" tabindex="3"/>
	        <label for="rememberMe" class="choice"><fmt:message key="login.rememberMe"/></label>
	    </li>
	    <li>
	        <input type="submit" class="button" name="login" value="<fmt:message key='button.login'/>" tabindex="4" />
	        <p style="padding-left: 3px;">
	            <fmt:message key="login.passwordHint"/></p>
	        </p>
	    </li>
	</ul>
	</fieldset>
	</form>
</div>
<div style="float: right; width: 420px; border-left: #999999 1px dotted; padding-left: 30px; height: 200px; text-align: center;" >
	<a href="<c:url value='/signup.html'/>"><img src="<s:url value='/images/private.jpg'/>" style="margin-bottom: 4px;" /></a>
	<br />
    <fmt:message key="login.signup">
        <fmt:param><c:url value="/signup.html"/></fmt:param>
    </fmt:message>
</div>
<div class="break" style="clear: both;"></div>
</div>

</body>