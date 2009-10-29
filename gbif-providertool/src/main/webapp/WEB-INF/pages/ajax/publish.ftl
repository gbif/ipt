<#if resource.id??>
  	<div id="statusContainer" class="${resource.status}">
  	 <p>
  	 	<@s.text name="publish.status.${resource.status}"/>, 
        <@s.text name="publish.registered.${resource.isRegistered()?string}"/>
     </p>
	<#if resource.isRegistered()>
    <div class="arrow"><img src="<@s.url value='/images/arrow_right.png'/>" /></div>
    <div class="arrow" style="display:none"><img src="<@s.url value='/images/arrow_down.png'/>" /></div>
	</#if>	
	  <#if resource.isDirty()>
		<@s.form id="publishForm" action="publish" namespace="/ajax" method="post">
		    <@s.hidden name="resourceId" value="${resource.id}"/>
		    <@s.hidden name="resourceType" value="${resourceType}"/>
			<@s.submit id="btnPublish" cssClass="publishButton" key="button.publish" theme="simple"/>
		</@s.form>
	  </#if>
       <div id="registryDetails" style="display:none" class="${resource.status}">
       		<h2><@s.text name='publish.registered'/></h2>
       		<div id="registryData"><@s.text name='publish.none'/></div>
       </div>	     
	</div>
   <#if resource.isRegistered()>
   <script>
	$.getJSON("<@s.url value='/ajax/proxy.do?uri=${resource.registryUrl}.json'/>", function(data){
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