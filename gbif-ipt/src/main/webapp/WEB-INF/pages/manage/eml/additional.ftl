<#escape x as x?html>
<#setting number_format="#####.##">
<#include "/WEB-INF/pages/inc/header.ftl">
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<script type="text/javascript" src="${baseURL}/js/ajaxfileupload.js"></script>
<script type="text/javascript" src="${baseURL}/js/jconfirmation.jquery.js"></script>
<title><@s.text name='manage.metadata.basic.title'/></title>
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();
				
		var value=$("#eml\\.intellectualRights").text();
		if(value==""){
			$("select#eml\\.intellectualRights\\.license\\.name").val('1'); 
			$("#eml\\.intellectualRights").html('<@s.text name="eml.intellectualRights.license.cczero.text"/>');
		}		
		$('.confirm').jConfirmAction({question : "<@s.text name="eml.intellectualRights.licenses.confirmation"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});		
		$("#eml\\.intellectualRights\\.license\\.name").change(function(){	
			$("#disclaimerRigths").css('display', '');
			var nameRights=$("#eml\\.intellectualRights\\.license\\.name").val();
			switch(nameRights)
	        {
	            case '1':
	            	$("#eml\\.intellectualRights").html('<@s.text name="eml.intellectualRights.license.cczero.text"/>');
	            break;
	            case '2':
	            	$("#eml\\.intellectualRights").html('<@s.text name="eml.intellectualRights.license.pddl.text"/>');
	            break;
	            case '3': 
	            	$("#eml\\.intellectualRights").html('<@s.text name="eml.intellectualRights.license.odcby.text"/>');
	            break;
	            case '4':
	            	$("#eml\\.intellectualRights").html('<@s.text name="eml.intellectualRights.license.odbl.text"/>');
	            break;
	            default: 
            		$("#eml\\.intellectualRights").html('');
	            	$("#disclaimerRigths").css('display', 'none');
	        }
		});			 
		
		$("#buttonUpload").click(function() {
  			return ajaxFileUpload();
		});
		
		function ajaxFileUpload()
    {
        var logourl=$("#resourcelogo img").attr("src");
        $.ajaxFileUpload
        (
            {
                url:'uploadlogo.do', 
                secureuri:false,
                fileElementId:'file',
                dataType: 'json',
                success: function (data, status)
                {
                    if(typeof(data.error) != 'undefined')
                    {
                        if(data.error != '')
                        {
                            alert(data.error);
                        }else
                        {
                            alert(data.msg);
                        }
                    }
                },
                error: function (data, status, e)
                {
                    alert(e);
                }
            }
        )
        if(logourl==undefined){
        	var baseimg=$('#baseimg').clone();
			baseimg.appendTo('#resourcelogo');
			logourl=$("#resourcelogo img").attr("src");
			$("#resourcelogo img").hide('slow').removeAttr("src");
			$("#resourcelogo img").show('slow', function() {
    			$("[id$='eml.logoUrl']").attr("value",logourl);
				$("#resourcelogo img").attr("src", logourl+"&t="+(new Date()).getTime());
  			});			
        }else{
        	$("#resourcelogo img").hide('slow').removeAttr("src");
         	logourl=$("#baseimg").attr("src");
         	 $("#resourcelogo img").show('slow', function() {
    			$("#resourcelogo img").attr("src", logourl+"&t="+(new Date()).getTime());
    			$("#logofields input[name$='logoUrl']").val( logourl);
  			});
        }
        return false;

    }    
        
	});	
	
	
</script>	
 <#assign sideMenuEml=true />
 <#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<h1><@s.text name='manage.metadata.additional.title'/>: <a href="resource.do?r=${resource.shortname}"><em>${resource.title!resource.shortname}</em></a> </h1>
<p><@s.text name='manage.metadata.additional.intro'/></p>
<#include "/WEB-INF/pages/macros/forms.ftl"/>  	 
<form class="topForm" action="metadata-${section}.do" method="post">
	<div class="halfcolumn">
	  	<@input name="eml.hierarchyLevel" i18nkey="eml.hierarchyLevel" help="i18n" disabled=true />
	</div>
	<div class="halfcolumn">
	  	<@input date=true name="eml.pubDate" i18nkey="eml.pubDate" help="i18n" helpOptions={"YYYY-MM-DD":"YYYY-MM-DD",  "MM/DD/YYYY":"MM/DD/YYYY"} />
	</div>
	<div class="newline"></div>
	<div id="logofields">
		<div class="halfcolumn">
			<@input name="eml.logoUrl" i18nkey="eml.logoUrl" help="i18n"/>
			<div style="padding-left: 20px;">
			<div class="newline"></div>
			<@s.file name="file"/>
			<div class="newline"></div>
			<button class="button" id="buttonUpload"><@s.text name="button.upload"/></button>
			</div>
		</div>
		<div class="halfcolumn">
			<div id="resourcelogo">
				<div class="newline"></div>
				<div class="newline"></div>
				<div class="newline"></div>
				<div class="newline"></div>
				<#if resource.eml.logoUrl?has_content>
					<img src="${resource.eml.logoUrl}" />
				</#if>
			</div>
		</div>
	</div>
  	<div class="newline"></div>  	
  	<@text name="eml.purpose" i18nkey="eml.purpose" help="i18n"/>
  	<div class="newline"></div>
  	<div class="newline"></div>
  	<div class="infos">	  
		<img class="infoImg" src="${baseURL}/images/info.gif" />
		<div class="info">		
		<span class="idSuffix">
			<@s.text name='eml.intellectualRights.license.help'/>            	
		</span>         		
		</div>	
		<select id="eml.intellectualRights.license.name">
	  	  <option value="0"><@s.text name='eml.intellectualRights.licenses'/></option>
		  <option value="1"><@s.text name='eml.intellectualRights.license.cczero'/></option>
		  <option value="2"><@s.text name='eml.intellectualRights.license.pddl'/></option>
		  <option value="3"><@s.text name='eml.intellectualRights.license.odcby'/></option>
		  <option value="4"><@s.text name='eml.intellectualRights.license.odbl'/></option>
	  	</select>
  	</div>  	
  	<div id='disclaimerRigths' style='display: none'><p><@s.text name='eml.intellectualRights.license.disclaimer'/></p></div>
  	<@text name="eml.intellectualRights"i18nkey="eml.intellectualRights" help="i18n" />  		
	<@text name="eml.additionalInfo" i18nkey="eml.additionalInfo" help="i18n"/>  
  	<div class="newline"></div>
  	<h2><@s.text name='manage.metadata.alternateIdentifiers.title'/></h2>
  	<p><@s.text name='manage.metadata.alternateIdentifiers.intro'/></p>
  	<div id="items">
		<#list eml.alternateIdentifiers as item>
			<div id="item-${item_index}" class="item">
			<div class="newline"></div>
			<div class="right">
				<a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/> ]</a>
		    </div>
		    <div class="newline"></div>
			<@input name="eml.alternateIdentifiers[${item_index}]" i18nkey="eml.alternateIdentifier" help="i18n"/>
			<div class="newline"></div>
			<div class="horizontal_dotted_line_large_foo" id="separator"></div>
			<div class="newline"></div>
		  	</div>
		</#list>
	</div>
  	<a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/></a>
	
  	<div class="buttons">
 		<@s.submit cssClass="button" name="save" key="button.save" cssClass="confirm" />
 		<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
  	</div>
  	<!-- internal parameter -->
	<input id="r" name="r" type="hidden" value="${resource.shortname}" />
	<input id="validate" name="validate" type="hidden" value="false" />
</form>
<div id="baseItem" class="item" style="display:none;">
	<div class="newline"></div>
	<div class="right">
      <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/> ]</a>
    </div>
	<@input name="alternateIdentifiers" i18nkey="eml.alternateIdentifier" help="i18n"/>
  	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
</div>
<img id="baseimg" src="${baseURL}/logo.do?r=${resource.shortname}" style="display:none;"/>
<img id="loadingimg" src="${baseURL}/images/loading_indicator_bar.gif" style="display:none;"/>
<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>