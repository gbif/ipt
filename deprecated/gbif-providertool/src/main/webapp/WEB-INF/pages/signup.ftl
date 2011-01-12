<#include "/WEB-INF/pages/inc/globalVars.ftl">  

<head>
    <title><@s.text name="signup.title"/></title>
    <meta name="menu" content="Login"/> 
    <meta name="heading" content="<@s.text name='signup.heading'/>"/>
</head>

<body id="signup"/>

<@s.form name="signupForm" action="signup" method="post" validate="true">
	<p><@s.text name="signup.instructions"/> <i>${cfg.contactName!""} &lt;${cfg.contactEmail!""}&gt;</i>
	</p>
	
    <@s.textfield key="user.username" cssClass="text large" required="true"/>
    <@s.textfield key="user.email" cssClass="text large" required="true" />

    <li>
        <div>
            <div class="left">
                <@s.textfield key="user.firstName" theme="xhtml" required="true" cssClass="text medium"/>
            </div>
            <div>
                <@s.textfield key="user.lastName" theme="xhtml" required="true" cssClass="text medium"/>
            </div>
        </div>
    </li>

    <li>
        <div>
            <div class="left">
                <@s.password key="user.password" showPassword="true" theme="xhtml" required="true" 
                    cssClass="text medium"/>
            </div>
            <div>
                <@s.password key="user.confirmPassword" theme="xhtml" required="true" 
                    showPassword="true" cssClass="text medium"/>
            </div>
        </div>
    </li>

    <@s.textfield key="user.passwordHint" required="true" cssClass="text large"/>

  
    <li class="buttonBar bottom">
        <@s.submit key="button.register" cssClass="button"/>
        <@s.submit key="button.cancel" name="cancel" cssClass="button"/>
    </li>
</@s.form>

<script type="text/javascript">
    Form.focusFirstElement(document.forms["signupForm"]);
</script>