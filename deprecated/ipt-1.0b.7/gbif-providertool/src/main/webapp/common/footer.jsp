<%@ include file="/common/taglibs.jsp" %>

  <div id="divider"></div>
    <ul class="sepmenu">
      <li><s:text name="webapp.version"/></li>
      <li><a href="http://code.google.com/p/gbif-providertoolkit/">IPT Home</a></li>
      <li><a href="http://code.google.com/p/gbif-providertoolkit/issues/entry">Bug Report</a></li>
      <li>&copy; <s:text name="copyright.year"/> <a href="<s:text name="company.url"/>"><s:text name="company.name"/></a></li>
    </ul>        
    <ul class="sepmenu">
      <li><a href='<s:url value="/about.html"/>'><s:property value="%{cfg.title}"/></a></li>
    </ul>
        