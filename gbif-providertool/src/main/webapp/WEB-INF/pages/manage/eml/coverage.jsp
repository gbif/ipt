<%@ include file="/common/taglibs.jsp"%>
<header>
<title><s:text name="eml.dataset"/> - <s:text name="eml.dataset.coverage.temp.taxa.title"/></title>
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.5.2/build/fonts/fonts-min.css" />
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.5.2/build/autocomplete/assets/skins/sam/autocomplete.css" />
<script type="text/javascript" src="http://yui.yahooapis.com/2.5.2/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.5.2/build/animation/animation-min.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.5.2/build/connection/connection-min.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.5.2/build/json/json-min.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.5.2/build/autocomplete/autocomplete-min.js"></script>
</header>

<h1><s:text name="eml.dataset"/> - <s:text name="eml.dataset.coverage.temp.taxa.title"/></h1>

<s:form id="editCoverageForm" action="initCoverage" method="post" validate="false" onsubmit="prepareForm()">

<s:set name="eml" value="eml" scope="request"/>

<jsp:include page="temporalCoverage.jsp"/>
<jsp:include page="taxonomicCoverage.jsp"/>

<li class="buttonBar bottom">
<s:submit cssClass="button" key="button.cancel" theme="simple" method="cancel"/>
<s:submit cssClass="button" key="button.back" theme="simple" method="back"/>
<s:submit cssClass="button" key="button.save" theme="simple"/>
</li>
</s:form>