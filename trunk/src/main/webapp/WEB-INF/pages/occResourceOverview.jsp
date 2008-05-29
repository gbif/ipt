<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="datasourceDetail.title"/></title>
    <meta name="heading" content="<fmt:message key='datasourceDetail.heading'/>"/>
	<s:head theme="ajax" debug="true"/>
</head>

<s:form id="datasourceForm" action="saveDatasource" method="post" validate="true">
    <li style="display: none">
        <s:hidden key="datasource.id"/>
    </li>

    <!-- The DatasoureMetadata forms -->
	<li>
        <label class="desc"><fmt:message key="resourceMetadata"/></label>
        <div class="group">
            <div>
                <s:textfield key="datasource.metadata.title" theme="xhtml" required="true" cssClass="text medium" labelposition="bottom"/>
            </div>
            <div>
                <s:textarea key="datasource.metadata.description" theme="xhtml" cssClass="text large" labelposition="bottom"/>
            </div>
        </div>
    </li>

<!-- URL link to struts action-->
<s:url id="ajaxServiceName" action="DatasourceAction" method="suggestServiceName" />
 
<!-- Div where content will be displayed -->
<pre>
<s:div theme="ajax" id="weather" href="ajaxServiceName">
    loading content...
</s:div>
</pre>

    <s:textfield key="datasource.sourceJdbcConnection" required="false" maxlength="120" cssClass="text large"/>
    <s:textfield key="datasource.serviceName" required="false" maxlength="16" cssClass="text medium"/>
    <s:textfield key="datasource.modified" cssClass="text medium" disabled="true"/>

    <li class="buttonBar bottom">
        <s:submit cssClass="button" method="save" key="button.save" theme="simple"/>
        <c:if test="${not empty datasource.id}">
            <s:submit cssClass="button" method="delete" key="button.delete"
                onclick="return confirmDelete('Datasource')" theme="simple"/>
        </c:if>
        <s:submit cssClass="button" method="cancel" key="button.cancel" theme="simple"/>
    </li>
</s:form>

<script type="text/javascript">
    Form.focusFirstElement($("datasourceForm"));
</script>
