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
	.actions select{
		width: 200px;
	}
	div.definition div.title{
		width: 30%;
	}
	div.title input[type="submit"]{
		float: right;
		margin-right: 5px;
	}
	div.definition div.body{
		width: 68%;
	}
	div.details{
		padding-top: 20px;
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
  	<input type="hidden" name="id" value="${id}" />

<#if mapping.source?exists>
<h1><@s.text name='manage.mapping.title'/> <span class="small">${mapping.source.name}</span></h1>
<p><@s.text name='manage.mapping.intro'><@s.param name="source">${mapping.source.name}</@s.param></@s.text></p>

	<@select name="mapping.idColumn" options=columns value="${mapping.idColumn!}" i18nkey="manage.mapping.idColumn" help="i18n"/>

  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>

	<#assign group=""/>
	<#list mapping.extension.properties as p>
	
	<#if (p.group!"")!=group>
		<#assign group=p.group/>
		<h2>${p.group}</h2>
	</#if>
	<div class="definition">	
	  <div class="title">
	  	<div class="head">
			${p.name}
			<#if p.required>[required]</#if>
	  	</div>
	  	<div>
	  		<img class="infoImg" src="${baseURL}/images/info.gif" />
			<div class="info">
				<#if p.description?has_content>${p.description}<br/><br/></#if>              	
				<#if p.link?has_content><@s.text name="basic.seealso"/> <a href="${p.link}">${p.link}</a><br/><br/></#if>
				<#if p.examples?has_content>
				<em><@s.text name="basic.examples"/></em>: ${p.examples}
				</#if>              	
			</div>
	      	<#if p.vocabulary?exists>	  		
	      	<a href="vocabulary.do?id=${p.vocabulary.uri}"><img class="infoImg" src="${baseURL}/images/vocabulary.png" /></a>
	      	</#if>			
	  	</div>
	  </div>
	  <div class="body">
	      	<div>
	      		<em>The Mapping</em>:
	      	</div>
	      	<div>
	      		<em>Example data from source</em>:
	      	</div>
	  </div>
	</div>
	</#list>

  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>
<#else>

<h1><@s.text name='manage.mapping.source'/></h1>
<p><@s.text name='manage.mapping.source.help'/></p>

<@selectList name="source" options=ms.config.sources objValue="name" objTitle="name" i18nkey="manage.mapping.source" />

  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>
</#if>

</form>


<#include "/WEB-INF/pages/inc/footer.ftl">
