<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="jobList.title"/></title>
    <meta name="heading" content="<s:text name='jobList.heading'/>"/>
    <meta name="menu" content="JobMenu"/>
</head>


<c:set var="buttons">
  <s:form>
	<s:submit action="editJob" key="button.add" theme="simple" cssClass="button"/>
	<s:submit action="mainMenu" key="button.done" theme="simple" cssClass="button"/>
  </s:form>
 </c:set>

<c:out value="${buttons}" escapeXml="false" />

<display:table name="jobs" class="table" requestURI="" id="jobList" export="true" pagesize="25">
    <display:column property="id" sortable="true" href="editJob.html" media="html"
        paramId="id" paramProperty="id" titleKey="job.id"/>
    <display:column property="id" media="csv excel xml pdf" titleKey="job.id"/>
    <display:column property="name" sortable="true" titleKey="job.name"/>
    <display:column property="description" sortable="true" titleKey="job.description"/>
    <display:column property="jobGroup" sortable="true" titleKey="job.jobGroup"/>
    <display:column property="runningGroup" sortable="true" titleKey="job.runningGroup"/>
    <display:column property="jobClassName" sortable="true" titleKey="job.jobClassName"/>
    <display:column property="dataAsJSON" sortable="true" titleKey="job.dataAsJSON"/>
    <display:column sortProperty="created" sortable="true" titleKey="job.created">
         <fmt:formatDate value="${jobList.created}" pattern="${datePattern}"/>
    </display:column>
    <display:column sortProperty="nextFireTime" sortable="true" titleKey="job.nextFireTime">
         <fmt:formatDate value="${jobList.nextFireTime}" pattern="${datePattern}"/>
    </display:column>
    <display:column sortProperty="started" sortable="true" titleKey="job.started">
         <fmt:formatDate value="${jobList.started}" pattern="${datePattern}"/>
    </display:column>
    <display:column property="instanceId" sortable="true" titleKey="job.instanceId"/>

    <display:setProperty name="paging.banner.item_name"><s:text name="jobList.job"/></display:setProperty>
    <display:setProperty name="paging.banner.items_name"><s:text name="jobList.jobs"/></display:setProperty>

    <display:setProperty name="export.excel.filename"><s:text name="jobList.title"/>.xls</display:setProperty>
    <display:setProperty name="export.csv.filename"><s:text name="jobList.title"/>.csv</display:setProperty>
    <display:setProperty name="export.pdf.filename"><s:text name="jobList.title"/>.pdf</display:setProperty>
</display:table>

<c:out value="${buttons}" escapeXml="false" />

<script type="text/javascript">
    highlightTableRows("jobList");
</script>
