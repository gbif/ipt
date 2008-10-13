<%@ include file="/common/taglibs.jsp"%>

<h1>Document saved to file</h1>

<p>
Eml document written to :<br/>
<s:set name="currentDoc" value="currentDoc"/>
<s:set name="metadataDirectoryPath" value="metadataDirectoryPath"/>
<a href="${pageContext.request.contextPath}/metadata/${currentDoc.name}">${currentDoc.path}</a>
</p>
<ul class="glassList">
        <li><a href="${pageContext.request.contextPath}/metadata/${currentDoc.name}">View (TODO stylesheet)</a></li>
        <li><a href="${pageContext.request.contextPath}/<s:url action="dataset" namespace="metadata"/>">Make changes</a>
</ul>
