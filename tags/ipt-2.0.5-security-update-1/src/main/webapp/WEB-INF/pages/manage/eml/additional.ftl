<#escape x as x?html>
<#setting number_format="#####.##">
<#include "/WEB-INF/pages/inc/header.ftl">
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<script type="text/javascript" src="${baseURL}/js/ajaxfileupload.js"></script>
<script type="text/javascript" src="${baseURL}/js/jconfirmation.jquery.js"></script>
<title><@s.text name='manage.metadata.additional.title'/></title>
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();

		$('.confirm').jConfirmAction({question : "<@s.text name="eml.intellectualRights.licenses.confirmation"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});

		var optionSelected=$(this).find("option:selected").text();
		if($.trim(optionSelected)=='<@s.text name="eml.intellectualRights.nolicenses"/>'){
			$('.confirm').unbind('click');
		}
		$("#licenseList").change(function(){
			$('.confirm').unbind('click');
			$("#disclaimerRigths").css('display', '');
			var nameRights=$("#licenseList").val();
			$("#eml\\.intellectualRights").val(nameRights);
			var optionSelected=$(this).find("option:selected").text();
			if($.trim(optionSelected)=='<@s.text name="eml.intellectualRights.nolicenses"/>'){
				$("#disclaimerRigths").css('display', 'none');
			}else{
				$('.confirm').jConfirmAction({question : "<@s.text name="eml.intellectualRights.licenses.confirmation"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
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
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><span class="superscript"><@s.text name='manage.overview.title.label'/></span>
    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
</h1>
<div class="grid_17 suffix_1">
<form class="topForm" action="metadata-${section}.do" method="post">
<h2 class="subTitle"><@s.text name='manage.metadata.additional.title'/></h2>
    <p><@s.text name='manage.metadata.additional.intro'/></p>
    <div id="Additional-Metadata">
    	<div class="halfcolumn">
    	  	<@input name="eml.hierarchyLevel" i18nkey="eml.hierarchyLevel" help="i18n" disabled=true />
    	</div>
    	<div class="halfcolumn">
    	  	<@input name="eml.pubDate" i18nkey="eml.pubDate" help="i18n" disabled=true />
    	</div>

    	<div id="logofields">
    		<div class="halfcolumn">
    			<@input name="eml.logoUrl" i18nkey="eml.logoUrl" help="i18n"/>
    			<div style="padding-left: 20px;">
    			<@s.file name="file"/>
    			<button class="button" id="buttonUpload"><@s.text name="button.upload"/></button>
    			</div>
    		</div>
    		<div class="halfcolumn">
    			<div id="resourcelogo">
    				<#if resource.eml.logoUrl?has_content>
    					<img src="${resource.eml.logoUrl}" />
    				</#if>
    			</div>
    		</div>
    		<div class="clearfix"></div>
    	</div>
  	  	<@text name="eml.purpose" i18nkey="eml.purpose" help="i18n"/>
      	<div class="infos">
    		<img class="infoImg" src="${baseURL}/images/info.gif" />
    		<div class="info">
    			<span class="idSuffix">
    				<@s.text name='eml.intellectualRights.license.help'/>
    			</span>
    		</div>
    		<select name="licenseList" id="licenseList">
    			<#list licenses?keys as licenseN>
    				<option value="${licenses[licenseN]}" <#if (licenseN!"")=="${licenseName!}"> selected="selected"</#if>>
    					${licenseN}
    				</option>
    			</#list>
    		</select>
    	</div>
      	<div id='disclaimerRigths' style='display: none'><p><@s.text name='eml.intellectualRights.license.disclaimer'/></p></div>
      	<@text name="eml.intellectualRights" i18nkey="eml.intellectualRights" help="i18n" />
    	<@text name="eml.additionalInfo" i18nkey="eml.additionalInfo" help="i18n"/>
  	</div>
    <div id="Alternative-Identifiers">
  	    <h2 class="subTitle"><@s.text name='manage.metadata.alternateIdentifiers.title'/></h2>
      	<p><@s.text name='manage.metadata.alternateIdentifiers.intro'/></p>
      	<div id="items">
    		<#list eml.alternateIdentifiers as item>
    			<div id="item-${item_index}" class="item">
    			<div class="right">
    				<a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/> ]</a>
    		    </div>
    			<@input name="eml.alternateIdentifiers[${item_index}]" i18nkey="eml.alternateIdentifier" help="i18n"/>
    		  	</div>
    		</#list>
    	</div>
      	<div class="addNew"><a id="plus" href="">[ <@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/> ]</a></div>

      	<div class="buttons">
     		<@s.submit cssClass="button" name="save" key="button.save" cssClass="confirm" />
     		<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
      	</div>
      	<div class="clearfix"></div>
  	</div>
    <!-- internal parameters needed by ajaxFileUpload.js - do not remove -->
	  <input id="r" name="r" type="hidden" value="${resource.shortname}" />
    <input id="validate" name="validate" type="hidden" value="false" />
</form>
</div>

<div id="baseItem" class="item clearfix" style="display:none;">
	<div class="right">
      <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/> ]</a>
    </div>
	<@input name="alternateIdentifiers" i18nkey="eml.alternateIdentifier" help="i18n"/>
</div>
<img id="baseimg" src="${baseURL}/logo.do?r=${resource.shortname}" style="display:none;"/>
<img id="loadingimg" src="${baseURL}/images/loading_indicator_bar.gif" style="display:none;"/>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
