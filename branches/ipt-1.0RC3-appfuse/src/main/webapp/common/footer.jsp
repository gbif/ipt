<%@ include file="/common/taglibs.jsp" %>

    <ul class="sepmenu">
      <li><s:property value="%{cfg.version}"/></li>
      <li><a href="http://code.google.com/p/gbif-providertoolkit/"><s:text name="footer.ipthome"/></a></li>
      <li><a href="http://code.google.com/p/gbif-providertoolkit/issues/entry"><s:text name="footer.bugreport"/></a></li>
      <li>&copy; <s:property value="%{cfg.copyrightYear}"/> <a href="<s:text name="company.url"/>"><s:text name="company.name"/></a></li>
    </ul>        
    <ul class="sepmenu">
      <s:if test="%{cfg.ipt.contactEmail!=null}">
      <li><s:text name="footer.contact"/>: <s:property value="%{cfg.ipt.contactName}"/> &lt;<s:property value="%{cfg.ipt.contactEmail}"/>&gt;</li>
      </s:if>
    </ul>
        