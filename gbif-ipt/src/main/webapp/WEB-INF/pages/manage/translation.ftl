<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="manage.translation.title"/></title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmation.jquery.js"></script>
	
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
	//Hack needed for Internet Explorer X.*x
	$('.reload').each(function() {
		$(this).click(function() { 
 				window.location = $(this).parent('a').attr('href');
		});
	});
	$('.automap').each(function() {
		$(this).click(function() { 
 				window.location = $(this).parent('a').attr('href');
 		});
	});	
	$('.cancelb').each(function() {
		$(this).click(function() { 
 				window.location = $(this).parent('a').attr('href');
 		});
	});	
	// end hack
	<#-- use vocabulary -->
	<#if (vocabTerms?size>0)>
	var vocab = [<#list vocabTerms?keys as code>{"value":"${code?replace('"','\"')}","label":"${vocabTerms[code]}"},</#list>];	
	$("#translation input").autocomplete({
	  source: vocab
	})
	</#if>
});  
</script>
 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/manage/translation_buttons.ftl"/>

<h1><@s.text name="manage.translation.title"/></h1>
<p><@s.text name="manage.translation.intro"/></p>

<h2><@s.text name="manage.translation.property"/> <em>${property.name}</em></h2>
<p>&quot;${property.description!}&quot;</p>

<#if property.vocabulary?exists>	  		
<p>
<strong><@s.text name="manage.translation.vocabulary.required"/></strong>:  
<@s.text name="manage.translation.vocabulary.required.intro"/>
</p>
<p>
<em>${property.vocabulary.title!property.vocabulary.uri}</em>:
<a href="vocabulary.do?id=${property.vocabulary.uri}" target="_blank"><img class="vocabImg" src="${baseURL}/images/vocabulary.png" /></a>
&quot;${property.vocabulary.description!}&quot;</p>
</#if>

<form class="topForm" action="translation.do" method="post">
  <input type="hidden" name="r" value="${resource.shortname}"/>
  <input type="hidden" name="rowtype" value="${property.extension.rowType}"/>
  <input type="hidden" name="mid" value="${mid}"/>
  <input type="hidden" name="term" value="${property.qualname}"/>
  <!-- buttons loaded from macro -->
  <@buttons "top"/>
  <table id="translation" class="simple">
    <colgroup>
      <col width="400">
      <col width="16">
      <col width="400">
    </colgroup>
    <tr>
      <th><@s.text name="manage.translation.source.value"/></th>
      <th></th>
      <th><@s.text name="manage.translation.translated.value"/></th>
    </tr>
    <#list tmap?keys as k>
      <tr<#if (k_index % 2) == 1> class="even"</#if>>
        <td>${k}</td>
        <td><img src="${baseURL}/images/<#if vocabTerms[tmap.get(k)!k]??>good<#else>bad</#if>.gif"/></td>
        <td><input type="text" name="tmap['${k}']" size="50" value="${tmap.get(k)!}"/></td>
      </tr>
    </#list>
  </table>
  <!-- buttons loaded from macro -->
  <@buttons "bottom"/>
</form>

  <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
