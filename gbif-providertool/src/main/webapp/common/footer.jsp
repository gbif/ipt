<%@ include file="/common/taglibs.jsp" %>

    <div id="divider"><div></div></div>
    <span class="left"><s:text name="webapp.version"/> |
        <a href="http://code.google.com/p/gbif-providertoolkit/">IPT Home</a> |
        <a href="http://code.google.com/p/gbif-providertoolkit/issues/entry">Bug Report</a>
        <c:if test="${pageContext.request.remoteUser != null}">
        | <s:text name="user.status"/> ${pageContext.request.remoteUser}
        </c:if>
    </span>
    <span class="right">
        &copy; <s:text name="copyright.year"/> <a href="<s:text name="company.url"/>"><s:text name="company.name"/></a>
    </span>
