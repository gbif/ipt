<%@ taglib uri="/struts-tags" prefix="s" %>
<table class="table">
	<thead>
		<tr>
			<s:iterator value="viewColumnHeaders" id="header" status="headStat">
			<th><s:property value="header"/></th>
			</s:iterator>
		</tr>
	</thead>
	<tbody>
	<s:iterator value="preview" status="rowStat">
		<tr>
			<s:iterator value="preview[#rowStat.count]" status="colStat">
				<td><s:property value="preview[#rowStat.count][#colStat.count-1]"/></td>
			</s:iterator>
		</tr>
	</s:iterator>
	</tbody>
</table>
