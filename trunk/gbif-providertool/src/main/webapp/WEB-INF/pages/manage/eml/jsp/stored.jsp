<%@ include file="/common/taglibs.jsp"%>

<s:set name="resourceId" value="resourceId"/>
<s:set name="currentDoc" value="currentDoc"/>
<s:set name="metadataDirectoryPath" value="metadataDirectoryPath"/>

<h1>Document saved to file</h1>

<p>
Eml document written to :<br/>
${currentDoc.path}
</p>

<ul class="glassList">
        <li><a href="${pageContext.request.contextPath}/metadata/${currentDoc.name}">View EML</a></li>
        <li><a href="${pageContext.request.contextPath}/<s:url action="dataset" namespace="metadata"/>">Make changes</a>
        <li><a href="${pageContext.request.contextPath}/manage/resource.html?resource_id=${resourceId}">Back to resource</a>
</ul>
