<#if resource.id??>
  <div id="statusContainer" class="${resource.status}">
    <b><@s.text name="publish.status.heading"/></b><p></p>

    <#if resource.isRegistered()>
      <div class="arrow"><img src="<@s.url value='/images/arrow_right.png'/>" /></div>
      <div class="arrow" style="display:none"><img src="<@s.url value='/images/arrow_down.png'/>" /></div>
      <#if resource.isDirty()>
        <@s.text name="publish.status.dirty"/><p></p>
  	  <#else>
        <@s.text name="publish.status.published"/><br>
      </#if>
    <#else>
      <@s.text name="publish.status.private"/><p></p>
    </#if>

    <#if resource.isRegistered()>
      <#if resource.isDirty()>
        <@s.form id="publishForm" action="publish" namespace="/ajax" method="post">
          <@s.hidden name="resourceId" value="${resource.id}"/>
          <@s.hidden name="resourceType" value="${resourceType}"/>
          <@s.submit id="btnPublish" cssClass="publishButton" key="publish.button.dirty" theme="simple"/>
        </@s.form>
	  </#if>
    <#else>
      <@s.form id="publishForm" action="publish" namespace="/ajax" method="post">
        <@s.hidden name="resourceId" value="${resource.id}"/>
        <@s.hidden name="resourceType" value="${resourceType}"/>
        <@s.submit id="btnPublish" cssClass="publishButton" key="publish.button.makepublic" theme="simple"/>
      </@s.form>
    </#if>

    <@s.text name="dataResource.lastModified"/><br>
    ${resource.modified?datetime?string("yyyy-MM-dd'T'HH:mm:ss")} 
    <#if resource.modifier??>
      <br><@s.text name="dataResource.lastModifiedBy"/> ${resource.modifier.getFullName()}
    </#if>
    <p></p>
    <@s.text name="dataResource.createdDate"/><br>
    ${resource.created?datetime?string("yyyy-MM-dd'T'HH:mm:ss")}
    <#if resource.creator??>
      <br><@s.text name="dataResource.createdBy"/> ${resource.creator.getFullName()}
    </#if>

    <div id="registryDetails" style="display:none" class="${resource.status}">
      <h2><@s.text name='publish.registered'/></h2>
      <div id="registryData"><@s.text name='publish.none'/></div>
    </div>	     
  </div>

  <#if resource.isRegistered()>
    <script>
      $.getJSON('${cfg.getBaseUrlContextPath()}/ajax/proxy.do?uri=${resource.registryUrl}.json'", function(data){
        //console.debug(data);
        $table='<table>';	
        $.each(data,function(x,y) {
          $table+='<tr><th>'+x+'</th></tr>';	
          $table+='<tr><td>'+y+'</td></tr>';	
        });
        $table+='</table>';
        $("#registryData").html($table);	
      });
    </script>
  </#if>	     
</#if>