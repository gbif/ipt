<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.source.title'/></title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmaction.jquery.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	initHelp();
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
});   
</script>
<style>
	div.definition div.title{
		width: 26%;
	}
	div.definition div.body{
		width: 72%;
	}
	div.body select{
		width: 200px;
	}
	div.body input{
		width: 400px;
	}
	.required{
		color:#bc5e5b;
		font-weight: normal;
	}
	div.infos{
		float:left;
		width:40px;
	}
	div.infos img.vocabImg {
		top: 2px !important;
		position: relative;
	}
	div.buttons{
		margin-top: 1em !important;
		margin-bottom: 2em !important;
	}	
</style>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1>${mapping.extension.title}</h1>
<p>${mapping.extension.description}</p>
<#if mapping.extension.link?has_content>
<p><@s.text name="basic.link"/>: <a href="${mapping.extension.link}">${mapping.extension.link}</a></p>
</#if>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="mapping.do" method="post">
  	<input type="hidden" name="r" value="${resource.shortname}" />
  	<input type="hidden" name="id" value="${id}" />

<#if mapping.source?exists>
<h1><@s.text name='manage.mapping.title'/> <span class="small">${mapping.source.name}</span></h1>
<p><@s.text name='manage.mapping.idColumn' /></p>
<select name="mapping.idColumn">
  <option value="" <#if !mapping.idColumn??> selected="selected"</#if>><@s.text name="manage.mapping.lineNumber"/></option>
<#list columns as col>
  <option value="${col_index}" <#if (mapping.idColumn!-1)==col_index> selected="selected"</#if>>${col}</option>
</#list>
</select>

  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>

<p><@s.text name='manage.mapping.intro'><@s.param name="source">${mapping.source.name}</@s.param></@s.text></p>

	<#assign group=""/>
	<ul class="horizontal">
	<#list mapping.extension.properties as p>
	<#if (p.group!"")!="" && (p.group!"")!=group>
		<#assign group=p.group/>
		<li><a href="#${p.group?url}">${p.group}</a></li>
	</#if>
	</#list>
	</ul>

	<#assign group=""/>
	<#--list mapping.extension.properties as p-->
	<#list fields as field>
	<#assign p=field.term/>
	
	<#if p.group?exists && p.group!=group>
	  <#if group!="">
	  <div class="buttons">
	 	<@s.submit name="save" key="button.save"/>
	 	<@s.submit name="cancel" key="button.cancel"/>
	  </div>
	  </#if>
		<#assign group=p.group/>
		<a name="${p.group?url}"></a>
		<h2>${p.group}</h2>
	</#if>
	<div class="definition">	
	  <div class="title">
	  	<div class="head">
			${p.name}
			<#if p.required><span class="required">***</span></#if>
	  	</div>
	  </div>
	  <div class="body">
	  	<div class="infos">
	  		<img class="infoImg" src="${baseURL}/images/info.gif" />
			<div class="info">
				<#if p.description?has_content>${p.description}<br/><br/></#if>              	
				<#if p.link?has_content><@s.text name="basic.seealso"/> <a href="${p.link}">${p.link}</a><br/><br/></#if>
				<#if p.examples?has_content>
				<em><@s.text name="basic.examples"/></em>: ${p.examples}
				</#if>              	
			</div>
	      	<#if p.vocabulary?exists>	  		
	      	<a href="vocabulary.do?id=${p.vocabulary.uri}" target="_blank"><img class="vocabImg" src="${baseURL}/images/vocabulary.png" /></a>
	      	</#if>			
	  	</div>
		<div>
				<select id="fIdx${field_index}" name="fields[${field_index}].index">
				  <option value="" <#if !field.index??> selected="selected"</#if>></option>
				<#list columns as col>
				  <option value="${col_index}" <#if (field.index!-1)==col_index> selected="selected"</#if>>${col}</option>
				</#list>
				</select>
				<input id="fVal${field_index}" name="fields[${field_index}].defaultValue" value="${field.defaultValue!}"/>  
	      	</div>
	      	<#if field.index?exists>
	      	<div>
	      		<em>Source Sample</em>:
	      		<#assign first=true/>
	      		<#list peek as row><#if row[field.index]?has_content><#if !first> | </#if><#assign first=false/>${row[field.index]}</#if></#list>
	      	</div>
	      	</#if>
	  </div>
	</div>

	<#if !field_has_next>
	  <div class="buttons">
	 	<@s.submit name="save" key="button.save"/>
	 	<@s.submit name="cancel" key="button.cancel"/>
	  </div>
	</#if>

	</#list>
	
	
<#else>

<h1><@s.text name='manage.mapping.source'/></h1>
<p><@s.text name='manage.mapping.source.help'/></p>

<@selectList name="source" options=resource.sources objValue="name" objTitle="name" i18nkey="manage.mapping.source" />

  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>
</#if>

</form>


<#include "/WEB-INF/pages/inc/footer.ftl">
