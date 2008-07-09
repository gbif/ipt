<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:property value="extension.name"/> Extension/></title>
    <meta name="heading" content="<s:property value="extension.name"/> Extension"/>
</head>

<s:form action="extensions">
	<s:label key="extension.name"/>
	<s:label key="extension.namespace"/>
	<li class="wwgrp">
	    <div class="wwlbl">
			<label class="desc"><s:text name='extension.link'/></label>
        </div> 
		<div class="wwctrl">
			<label><a href="<s:property value="extension.link"/>"><s:property value="extension.link"/></a></label>
		</div>
	</li>
	<s:label key="extension.properties" value="" name=""/>
	<ul class="subform">
	<s:iterator value="extension.properties" status="stat">
	  <div class="subentry">
		<s:property value="name"/>
		<a href="<s:property value="link"/>">(info)</a>
	  </div>
	    <c:if test="${not empty terms}">
			<div class="terms subform">
				<s:text name='extension.property.terms'/>:
				<s:iterator value="terms" status="termstat">
					<s:label name="terms[#termstat.index]"/>
				</s:iterator>
			</div>
		</c:if>
	</s:iterator>
	</ul>
	<br/>
	<br/>
    <s:submit cssClass="button" key="button.done" theme="simple"/>
</s:form>

<br/>
