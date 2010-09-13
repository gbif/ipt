<#include "/WEB-INF/pages/inc/header.ftl">
<#include "/WEB-INF/pages/inc/header_ui.ftl">
	<title>Value Translation</title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmaction.jquery.js"></script>
	
<script type="text/javascript">
$(document).ready(function(){
	initHelp();
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
	$("table input").focus(function() {
		$(this).parent().parent().addClass("highlight");
	});
	$("table input").blur(function() {
		$(this).parent().parent().removeClass("highlight")
	});
	<#-- use vocabulary -->
	<#if (vocabTerms?size>0)>
	var vocab = [<#list vocabTerms?keys as code>{"value":"${code?replace('"','\"')}","label":"${vocabTerms[code]}"},</#list>];	
	$("#translation input").autocomplete({
	  source: vocab
	})
	</#if>
});  
</script>
<style>
	img.vocabImg {
		top: 2px !important;
		position: relative;
		padding: 0px 5px;
	}
	.highlight {
		background-color: #effad7 !important;
	}
	th {
		text-align: left;
	}
</style>
 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1>Value Translation</h1>
<p>You can define a translation for the values used in your source and the ones that should be used in the generated archive.
The list of distinct values found in your source is generated the first time for you, but can be manually reloaded at any time while keeping the current translation.
</p>

<h2>Property <em>${property.name}</em></h2>
<p>&quot;${property.description!}&quot;</p>

<#if property.vocabulary?exists>	  		
<p>
<strong>Vocabulary required</strong>:  
See indicator for each value if a corresponding term in the vocabulary exists.
</p>
<p>
<em>${property.vocabulary.title!property.vocabulary.uri}</em>:
<a href="vocabulary.do?id=${property.vocabulary.uri}" target="_blank"><img class="vocabImg" src="${baseURL}/images/vocabulary.png" /></a>
&quot;${property.vocabulary.description!}&quot;</p>
</#if>			


<form class="topForm" action="translation.do" method="post">
  	<input type="hidden" name="r" value="${resource.shortname}" />
  	<input type="hidden" name="term" value="${property.qualname}" />
  	<input type="hidden" name="mapping" value="${property.extension.rowType}" />

  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	<a href="translationReload.do?r=${resource.shortname}&mapping=${property.extension.rowType}&term=${property.qualname}"><button>Reload</button></a>
 	<#if property.vocabulary?exists>	  		
 	<a href="translationAutomap.do?r=${resource.shortname}&mapping=${property.extension.rowType}&term=${property.qualname}"><button>Automap</button></a>
 	</#if>			
 	<a href="mapping.do?r=${resource.shortname}&id=${property.extension.rowType}"><button><@s.text name="button.cancel"/></button></a>
  </div>
  
<table id="translation" class="simple">                               
  <colgroup>
    <col width="400">
    <col width="16">
    <col width="400">
  </colgroup>
<tr>
 <th>Source Value</th>
 <th></th>
 <th>Translated Value</th>
</tr>     
<#list tmap?keys as k>	
<tr<#if (k_index % 2) == 1> class="even"</#if>>
 <td>${k}</td>
 <td><img src="${baseURL}/images/<#if vocabTerms[tmap.get(k)!k]??>good<#else>bad</#if>.gif"/></td>
 <td><input type="text" name="tmap['${k}']" size="50" value="${tmap.get(k)!}"/></td>
</tr>     
</#list>
</table>

				
  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	<a href="translationReload.do?r=${resource.shortname}&mapping=${property.extension.rowType}&term=${property.qualname}"><button>Reload</button></a>
 	<a href="mapping.do?r=${resource.shortname}&id=${property.extension.rowType}"><button><@s.text name="button.cancel"/></button></a>
  </div>
</form>




<#include "/WEB-INF/pages/inc/footer.ftl">
