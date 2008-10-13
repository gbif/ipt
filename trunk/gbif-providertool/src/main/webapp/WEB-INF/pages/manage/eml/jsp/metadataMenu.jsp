<%@ include file="/common/taglibs.jsp"%>
<head>
    <title><s:text name="metadataMenu.title"/></title>
    <meta name="heading" content="<s:text name='metadataMenu.title'/>"/>
    <meta name="menu" content="MetdataMenu"/>
</head>
<p><s:text name="metadataMenu.message"/></p>
<div class="separator"></div>
<ul class="glassList">    
	<li>
        <a href="${pageContext.request.contextPath}/<s:url action="createNew" namespace="metadata"/>"><s:text name="metadataMenu.create.new"/></a>
    </li>
    <li>
    	<a href="${pageContext.request.contextPath}/<s:url action="import" namespace="metadata" />"><s:text name="metadataMenu.import"/></a>
    </li>
    <li>
    	<a href="${pageContext.request.contextPath}/<s:url action="documentHistory" namespace="metadata" />"><s:text name="document.history"/></a>
    </li>    
    <c:if test="${not empty sessionScope['currentDoc']}">
    	<c:set var="currentDoc" value="${sessionScope['currentDoc']}"/>
    	<c:set var="metadataDirectoryPath" value="/metadata"/>	
		<li style="width:100%;">
			 <s:text name="current.document"/> <b>${eml.dataset.title}</b><br/>
             <s:text name="current.document.location"/> ${currentDoc.path}
			<ul>
				<li><a href="${pageContext.request.contextPath}${metadataDirectoryPath}/${currentDoc.name}"><s:text name="view"/></a></li>
				<li><a href="${pageContext.request.contextPath}/<s:url action="dataset" namespace="metadata"/>"><s:text name="edit"/></a></li>
                <li><a href="${pageContext.request.contextPath}/<s:url action="closeDocument" namespace="metadata"/>"><s:text name="close"/></a></li>
			</ul>				
	    </li>
    </c:if>
</ul>