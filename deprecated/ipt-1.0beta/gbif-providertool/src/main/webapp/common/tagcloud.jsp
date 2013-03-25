<%@ include file="/common/taglibs.jsp"%>
<div id="tagcloud">
	<s:if test="tagcloud">
	<label>Keyword Cloud</label>
	<div class="tagCloud">
      <s:iterator value="tagcloud">
		<s:url id="tagLink" action="metaSearch" namespace="/" includeParams="none">
			<s:param name="keyword" value="%{key}" />
		</s:url>
	    <a rel="tag" class="tag cloud<s:property value="value"/>" href="<s:property value="tagLink"/>"><s:property value="key"/></a>
      </s:iterator>
	</div>
	</s:if>
</div>		
