<%@ include file="/common/taglibs.jsp"%>
<tr>
    <td>
        <select name="<c:out value="${param.leftId}"/>" multiple="multiple"
            onDblClick="Selectbox.moveSelectedOptions(this, this.form.<c:out value="${param.rightId}"/>,true)"
            id="<c:out value="${param.leftId}"/>" size="5">
    <c:if test="${leftList != null}">
        <c:forEach var="list" items="${leftList}" varStatus="status">
            <option value="<c:out value="${list.value}"/>">
                <c:out value="${list.label}" escapeXml="false" />
            </option>
        </c:forEach>
    </c:if>
        </select>
    </td>
    <td class="moveOptions">
        <button name="moveRight" id="moveRight<c:out value="${param.listCount}"/>" type="button" 
            onclick="Selectbox.moveSelectedOptions(this.form.<c:out value="${param.leftId}"/>,this.form.<c:out value="${param.rightId}"/>,true)">
            &gt;&gt;</button><br />
        <button name="moveAllRight" id="moveAllRight<c:out value="${param.listCount}"/>" type="button"
            onclick="Selectbox.moveAllOptions(this.form.<c:out value="${param.leftId}"/>, this.form.<c:out value="${param.rightId}"/>, true)">
            All &gt;&gt;</button><br />
        <button name="moveLeft" id="moveLeft<c:out value="${param.listCount}"/>" type="button"
            onclick="Selectbox.moveSelectedOptions(this.form.<c:out value="${param.rightId}"/>, this.form.<c:out value="${param.leftId}"/>, true)">
            &lt;&lt;</button><br />
        <button name="moveAllLeft" id="moveAllLeft<c:out value="${param.listCount}"/>" type="button"
            onclick="Selectbox.moveAllOptions(this.form.<c:out value="${param.rightId}"/>, this.form.<c:out value="${param.leftId}"/>, true)">
            All &lt;&lt;</button>
    </td>
    <td>
        <select name="<c:out value="${param.rightId}"/>" multiple="multiple"
            id="<c:out value="${param.rightId}"/>" size="5">
    <c:if test="${rightList != null}">
        <c:forEach var="list" items="${rightList}" varStatus="status">
            <option value="<c:out value="${list.value}"/>">
                <c:out value="${list.label}" escapeXml="false"/>
            </option>
        </c:forEach>
    </c:if>
        </select>
    </td>
</tr>