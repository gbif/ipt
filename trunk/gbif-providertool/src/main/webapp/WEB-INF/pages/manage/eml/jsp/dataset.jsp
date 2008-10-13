<%@ include file="/common/taglibs.jsp"%>
<header>
	<title><s:text name="eml.dataset"/> - <s:text name="eml.dataset.basic.details"/></title>
</header>

<h1><s:text name="eml.dataset"/> - <s:text name="eml.dataset.basic.details"/></h1>

<s:form id="editDatasetForm" action="initDataset" method="post" validate="false">
<fieldset id="dataset" class="metadata">
<legend><s:text name="eml.dataset.basic.details"/></legend>
<s:set name="eml" value="eml" scope="request"/>
<s:textfield key="eml.dataset.title" required="true" cssClass="text large"/>
<s:textarea key="eml.dataset.abstract.para" required="true" cssClass="text large"/>

<li id="wwgrp_editDatasetForm_eml_dataset_keywordset" class="wwgrp"> 
	<div  id="wwlbl_editDatasetForm_eml_dataset_keywordset" class="wwlbl">
		<label for="editDatasetForm_eml_dataset_keywordset" class="desc">
			<s:text name="eml.dataset.keywordSet"/>
		</label>        
	</div> 
	<div id="wwctrl_editDatasetForm_eml_dataset_keywordset" class="wwctrl">
		<table>
			<tr>
			<c:forEach begin="1" end="15" varStatus="cellStatus">
				<td>
					<c:set var="currentIndex" value="${cellStatus.index}" scope="request"/>
<%
	org.gbif.metadata.eml.Dataset dataset = (org.gbif.metadata.eml.Dataset) request.getAttribute("dataset");
	Integer currentIndex = (Integer)request.getAttribute("currentIndex");
	org.gbif.metadata.eml.KeywordSet keywordSet = null;
	if(dataset.sizeKeywordSetList()>0 && currentIndex<dataset.sizeKeywordSetList()-1){
		keywordSet = dataset.getKeywordSet(currentIndex-1);
		pageContext.setAttribute("keywordSet", keywordSet);
	} else {
		pageContext.setAttribute("keywordSet", null);
	}
%>		
					<input type="text" name="keyword" value="${keywordSet.keyword.base}"/>
				</td>
				<c:if test="${cellStatus.index mod 3 == 0 && cellStatus.index>0 && cellStatus.index<15}"></tr><tr></c:if>
			</c:forEach>
			</tr>
		</table>
	</div>
</li>

<s:if test="%{eml.dataset.pubDate==null}">
	<s:set name="eml.dataset.pubDate" value="now"/>	
</s:if>
<s:textfield key="eml.dataset.pubDate" required="false" cssClass="text medium"/>

<li id="wwgrp_editDatasetForm_eml_dataset_language" class="wwgrp"> 
	<s:set name="isoLanguageI18nCodeMap" value="isoLanguageI18nCodeMap" scope="request"/>
	<s:select key="eml.dataset.language" list="isoLanguageI18nCodeMap" cssClass="text medium" value="defaultDatasetLanguage"/>
</li>

</fieldset>

<li class="buttonBar bottom">
	<s:submit cssClass="button" key="button.cancel" theme="simple" method="cancel"/>
	<s:submit cssClass="button" key="button.next" theme="simple"/>
</li>
</s:form>