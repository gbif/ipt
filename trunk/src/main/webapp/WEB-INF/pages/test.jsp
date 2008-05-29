<%@ include file="/common/taglibs.jsp"%>

<head>
    <title>Playmate Test</title>
    <meta name="heading" content="<fmt:message key='mainMenu.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
</head>

<div class="separator"></div>

<h3>Answer: <s:property value="name"/> </h3>

<h3>Leave data</h3>
<s:form action="test" namespace="/manage">
<s:textfield required="true" key="A Name" name="name" />
<s:textarea required="true" rows="4" cols="36" key="Your Message" name="message" />
<s:submit />
</s:form>

<div class="separator"></div>
<s:property value="counter" />

